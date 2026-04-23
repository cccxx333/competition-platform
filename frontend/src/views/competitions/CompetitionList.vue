<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { Document } from "@element-plus/icons-vue"
import { createCompetition, listCompetitions, updateCompetitionAdmin, deleteCompetition, type CompetitionCreatePayload, type CompetitionAdminUpdatePayload, type CompetitionListItem, type CompetitionListParams } from "@/api/competitions"
import { listSkills, type Skill } from "@/api/skills"
import { createTeacherApplication, type TeacherApplicationCreatePayload } from "@/api/teacherApplications"
import { listAdminUsersPage, type AdminUserProfile } from "@/api/adminUsers"
import StatusTag from "@@/components/StatusTag/index.vue"
import { useAuthStore } from "@/stores/auth"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isStudent = computed(() => roleUpper.value === "STUDENT")
const isTeacher = computed(() => roleUpper.value === "TEACHER")
const isAdmin = computed(() => roleUpper.value === "ADMIN")

type RecommendationRow = {
  id?: number
  name?: string
  status?: CompetitionListItem["status"] | string
  startDate?: string
  endDate?: string
  registrationDeadline?: string
  organizer?: string
  score?: number
  reason?: string
  source: "list" | "algorithm"
  managerId?: number
  managerName?: string
  minTeamSize?: number
  maxTeamSize?: number
  description?: string
}

const rows = ref<RecommendationRow[]>([])
const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editDialogError = ref("")
const editForm = ref<CompetitionAdminUpdatePayload & { id?: number }>({
  id: undefined,
  name: "",
  status: "UPCOMING",
  startDate: "",
  endDate: "",
  registrationDeadline: "",
  minTeamSize: 1,
  maxTeamSize: 1,
  description: "",
  managerId: null,
  requiredSkills: []
})
const editRequiredSkillRows = ref<Array<{ skillId: number | null; importance: number }>>([{ skillId: null, importance: 3 }])
const loading = ref(false)
const errorMessage = ref("")
const total = ref<number | null>(null)
const initialized = ref(false)
const isApplying = ref(false)
const createDialogVisible = ref(false)
const createDialogLoading = ref(false)
const createDialogError = ref("")
const teacherApplyDialogVisible = ref(false)
const teacherApplyDialogLoading = ref(false)
const teacherApplyDialogError = ref("")
const teacherApplyCompetitionId = ref<number | null>(null)
const teacherApplyCompetitionName = ref("")
const teacherApplyDescription = ref("")
const skillsLoading = ref(false)
const allSkills = ref<Skill[]>([])
const requiredSkillRows = ref<Array<{ skillId: number | null; importance: number }>>([{ skillId: null, importance: 3 }])
const teacherApplySkillRows = ref<Array<{ skillId: number | null; weight: number }>>([{ skillId: null, weight: 3 }])
const teachersLoading = ref(false)
const allTeachers = ref<AdminUserProfile[]>([])
const createForm = ref({
  name: "",
  organizer: "",
  level: "",
  startDate: "",
  endDate: "",
  registrationDeadline: "",
  minTeamSize: 1,
  maxTeamSize: 1,
  description: "",
  managerId: null as number | null
})
const selectedRequiredSkillIds = computed(
  () => new Set(requiredSkillRows.value.map((row) => row.skillId).filter((id): id is number => typeof id === "number"))
)
const selectedTeacherApplySkillIds = computed(
  () => new Set(teacherApplySkillRows.value.map((row) => row.skillId).filter((id): id is number => typeof id === "number"))
)

type StatusFilterValue = "" | "UPCOMING" | "ONGOING" | "FINISHED"

const filters = reactive({
  keyword: "",
  status: "" as StatusFilterValue
})

const sourceMode = ref<"list" | "algorithm">("list")
const isAlgorithmMode = computed(() => sourceMode.value === "algorithm")
const topK = ref(10)
const fallbackNotice = ref("")
const FALLBACK_NOTICE_TEXT = "当前推荐基于通用策略，补全技能信息后将优先展示更匹配的竞赛。"

const pagination = reactive({
  page: 0,
  size: 10
})

const statusOptions: Array<{ label: string; value: StatusFilterValue }> = [
  { label: "全部", value: "" },
  { label: "未开始", value: "UPCOMING" },
  { label: "进行中", value: "ONGOING" },
  { label: "已结束", value: "FINISHED" }
]

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
  }
  if (status === 403) {
    ElMessage.error("无权限")
    return "无权限"
  }
  if (status === 404) {
    ElMessage.error("竞赛不存在或已删除")
    return "竞赛不存在或已删除"
  }
  if (status === 409) {
    ElMessage.error("业务冲突")
    return "业务冲突"
  }
  ElMessage.error("服务异常，请稍后重试")
  return "服务异常，请稍后重试"
}

const buildQuery = () => {
  const query: Record<string, string> = {
    page: String(pagination.page),
    size: String(pagination.size)
  }
  if (sourceMode.value === "list" && filters.keyword) {
    query.keyword = filters.keyword
  }
  if (sourceMode.value === "list" && filters.status) {
    query.status = filters.status
  }
  if (sourceMode.value) {
    query.source = sourceMode.value
  }
  if (sourceMode.value === "algorithm") {
    query.topK = String(topK.value)
  }
  return query
}

const syncUrl = () => {
  const next = buildQuery()
  const current = route.query
  const same =
    String(current.page ?? "") === String(next.page ?? "") &&
    String(current.size ?? "") === String(next.size ?? "") &&
    String(current.keyword ?? "") === String(next.keyword ?? "") &&
    String(current.status ?? "") === String(next.status ?? "") &&
    String(current.source ?? "") === String(next.source ?? "") &&
    String(current.topK ?? "") === String(next.topK ?? "")
  if (!same) {
    router.replace({ query: next })
  }
}

const sortRows = (data: RecommendationRow[]) => {
  return data.slice().sort((a, b) => {
    const scoreA = typeof a.score === "number" ? a.score : -1
    const scoreB = typeof b.score === "number" ? b.score : -1
    return scoreB - scoreA
  })
}

const fetchList = async () => {
  loading.value = true
  errorMessage.value = ""
  fallbackNotice.value = ""
  try {
    if (sourceMode.value === "algorithm") {
      const { items: data } = await listCompetitions({
        recommend: true,
        status: "UPCOMING",
        topK: topK.value,
        page: 0,
        size: topK.value
      })
      if (data.length > 0) {
        const hasFallbackFlag = data.some((item) => item.fallbackApplied === true)
        rows.value = sortRows(
          data.map((item) => ({
            id: item.id,
            name: item.name,
            status: item.status,
            startDate: item.startDate,
            endDate: item.endDate,
            registrationDeadline: item.registrationDeadline,
            organizer: item.organizer,
            score: typeof item.matchScore === "number" ? item.matchScore : undefined,
            reason: item.recommendReason,
            source: "algorithm"
          }))
        )
        if (hasFallbackFlag) {
          fallbackNotice.value = FALLBACK_NOTICE_TEXT
        }
      } else {
        rows.value = []
      }
      total.value = null
    } else {
      const params: CompetitionListParams = {
        keyword: filters.keyword || undefined,
        page: pagination.page,
        size: pagination.size
      }
      if (filters.status) {
        params.status = filters.status
      }
      const { items: data, total: totalElements, page, size } = await listCompetitions(params)
      rows.value = data.map((item) => ({
        id: item.id,
        name: item.name,
        status: item.status,
        startDate: item.startDate,
        endDate: item.endDate,
        registrationDeadline: item.registrationDeadline,
        organizer: item.organizer,
        source: "list",
        managerId: item.managerId,
        managerName: item.managerName,
        minTeamSize: item.minTeamSize,
        maxTeamSize: item.maxTeamSize,
        description: item.description,
        requiredSkills: item.requiredSkills
      }))
      total.value = typeof totalElements === "number" ? totalElements : null
      if (typeof page === "number" && page !== pagination.page) {
        pagination.page = page
      }
      if (typeof size === "number" && size !== pagination.size) {
        pagination.size = size
      }
      fallbackNotice.value = ""
    }
  } catch (error: any) {
    rows.value = []
    total.value = null
    fallbackNotice.value = ""
    errorMessage.value = showRequestError(error, "加载竞赛失败")
  } finally {
    loading.value = false
  }
}

let keywordTimer: number | undefined

const resetFilters = () => {
  isApplying.value = true
  filters.keyword = ""
  filters.status = ""
  pagination.page = 0
  syncUrl()
  fetchList()
  window.setTimeout(() => {
    isApplying.value = false
  }, 0)
}

const handlePageChange = (page: number) => {
  pagination.page = page - 1
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 0
}

const goDetail = (row: RecommendationRow) => {
  if (!row?.id) {
    router.push("/competitions")
    return
  }
  router.push({
    path: `/competitions/${row.id}`,
    query: {
      back: encodeURIComponent(route.fullPath)
    }
  })
}

const handleEnrollAction = (row: RecommendationRow) => {
  if (!row?.id) {
    ElMessage.warning("竞赛信息异常")
    return
  }
  if (isStudent.value) {
    if (!canApplyBeforeDeadline(row)) {
      ElMessage.warning("当前竞赛已超过报名截止时间，无法报名")
      return
    }
    router.push({
      path: "/competitions/apply",
      query: {
        competitionId: String(row.id)
      }
    })
    return
  }
  if (isTeacher.value) {
    if (!canApplyBeforeDeadline(row)) {
      ElMessage.warning("当前竞赛已超过报名截止时间，无法发起创建队伍申请")
      return
    }
    openTeacherApplyDialog(row)
  }
}

const canApplyBeforeDeadline = (row: RecommendationRow) => {
  const raw = row.registrationDeadline
  if (!raw) return false
  const parsed = parseDeadline(raw)
  if (!parsed) return false
  return Date.now() <= parsed.getTime()
}

const parseDeadline = (value: string) => {
  const dateOnlyPattern = /^\d{4}-\d{2}-\d{2}$/
  const date = dateOnlyPattern.test(value) ? new Date(`${value}T23:59:59`) : new Date(value)
  if (Number.isNaN(date.getTime())) return null
  return date
}

const openTeacherApplyDialog = (row: RecommendationRow) => {
  if (!row?.id) {
    ElMessage.warning("竞赛信息异常")
    return
  }
  teacherApplyCompetitionId.value = row.id
  teacherApplyCompetitionName.value = row.name ?? `竞赛 ${row.id}`
  teacherApplyDescription.value = ""
  teacherApplyDialogError.value = ""
  teacherApplySkillRows.value = [{ skillId: null, weight: 3 }]
  teacherApplyDialogVisible.value = true
  loadSkillsForCreate()
}

const closeTeacherApplyDialog = () => {
  if (teacherApplyDialogLoading.value) return
  teacherApplyDialogVisible.value = false
  teacherApplyDialogError.value = ""
  teacherApplyCompetitionId.value = null
  teacherApplyCompetitionName.value = ""
  teacherApplyDescription.value = ""
}

const addTeacherApplySkillRow = () => {
  if (teacherApplySkillRows.value.length >= 8) return
  teacherApplySkillRows.value.push({ skillId: null, weight: 3 })
}

const removeTeacherApplySkillRow = (index: number) => {
  if (teacherApplySkillRows.value.length <= 1) return
  teacherApplySkillRows.value.splice(index, 1)
}

const submitTeacherApply = async () => {
  if (!teacherApplyCompetitionId.value) {
    teacherApplyDialogError.value = "竞赛信息异常，请关闭后重试"
    return
  }
  teacherApplyDialogLoading.value = true
  teacherApplyDialogError.value = ""
  try {
    const payload: TeacherApplicationCreatePayload = {}
    const description = teacherApplyDescription.value.trim()
    if (description) {
      payload.description = description
    }
    const skills = teacherApplySkillRows.value
      .filter((row) => typeof row.skillId === "number")
      .map((row) => ({
        skillId: row.skillId as number,
        weight: Number.isFinite(row.weight) && row.weight > 0 ? row.weight : 1
      }))
    if (skills.length > 0) {
      payload.skills = skills
    }
    await createTeacherApplication(teacherApplyCompetitionId.value, payload)
    ElMessage.success("申请已提交")
    closeTeacherApplyDialog()
  } catch (error: any) {
    teacherApplyDialogError.value = error?.message || "提交创建队伍申请失败"
  } finally {
    teacherApplyDialogLoading.value = false
  }
}

const formatDateRange = (row: RecommendationRow) => {
  const start = formatDate(row.startDate)
  const end = formatDate(row.endDate)
  if (!start && !end) return ""
  return [start, end].filter(Boolean).join(" ~ ")
}

const formatDate = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    return value.split("T")[0]
  }
  return value
}

const openCreateDialog = () => {
  createDialogError.value = ""
  requiredSkillRows.value = [{ skillId: null, importance: 3 }]
  createForm.value = {
    name: "",
    organizer: "",
    level: "",
    startDate: "",
    endDate: "",
    registrationDeadline: "",
    minTeamSize: 1,
    maxTeamSize: 1,
    description: ""
  }
  createDialogVisible.value = true
  loadSkillsForCreate()
}

const closeCreateDialog = () => {
  if (createDialogLoading.value) return
  createDialogVisible.value = false
  createDialogError.value = ""
}

const loadSkillsForCreate = async () => {
  if (skillsLoading.value || (allSkills.value.length > 0 && allTeachers.value.length > 0)) return
  skillsLoading.value = true
  try {
    allSkills.value = await listSkills()
    if (isAdmin.value && allTeachers.value.length === 0) {
      const res = await listAdminUsersPage({ role: 'TEACHER', size: 1000 })
      allTeachers.value = res.items || []
    }
  } catch (error: any) {
    createDialogError.value = error?.message || "加载技能列表失败"
  } finally {
    skillsLoading.value = false
  }
}

const addRequiredSkillRow = () => {
  if (requiredSkillRows.value.length >= 8) return
  requiredSkillRows.value.push({ skillId: null, importance: 3 })
}

const removeRequiredSkillRow = (index: number) => {
  if (requiredSkillRows.value.length <= 1) return
  requiredSkillRows.value.splice(index, 1)
}

const submitCreate = async () => {
  const form = createForm.value
  const name = form.name.trim()
  if (!name) {
    createDialogError.value = "请填写竞赛名称"
    return
  }
  if (!form.startDate || !form.endDate || !form.registrationDeadline) {
    createDialogError.value = "请完整填写日期"
    return
  }
  if (form.registrationDeadline > form.startDate) {
    createDialogError.value = "报名截止时间必须早于或等于比赛开始时间"
    return
  }
  if (form.startDate >= form.endDate) {
    createDialogError.value = "比赛开始时间必须早于比赛结束时间"
    return
  }
  if (!Number.isFinite(form.minTeamSize) || form.minTeamSize < 1) {
    createDialogError.value = "最小队伍人数需大于等于 1"
    return
  }
  if (!Number.isFinite(form.maxTeamSize) || form.maxTeamSize < form.minTeamSize) {
    createDialogError.value = "最大队伍人数需大于等于最小队伍人数"
    return
  }
  const requiredSkills = requiredSkillRows.value
    .filter((row) => typeof row.skillId === "number")
    .map((row) => ({
      skillId: row.skillId as number,
      importance: Number.isFinite(row.importance) && row.importance > 0 ? row.importance : 1
    }))
  if (!requiredSkills.length) {
    createDialogError.value = "请至少添加一个竞赛技能需求"
    return
  }

  const payload: CompetitionCreatePayload = {
    name,
    organizer: form.organizer.trim() || undefined,
    level: form.level.trim() || undefined,
    startDate: form.startDate,
    endDate: form.endDate,
    registrationDeadline: form.registrationDeadline,
    minTeamSize: form.minTeamSize,
    maxTeamSize: form.maxTeamSize,
    description: form.description.trim() || undefined,
    managerId: form.managerId || null,
    requiredSkills
  }
  createDialogLoading.value = true
  createDialogError.value = ""
  try {
    await createCompetition(payload)
    ElMessage.success("发布成功")
    createDialogVisible.value = false
    fetchList()
  } catch (error: any) {
    createDialogError.value = error?.message || "发布竞赛失败"
  } finally {
    createDialogLoading.value = false
  }
}

const openEditDialog = (row: RecommendationRow) => {
  if (!row.id) return
  editForm.value = {
    id: row.id,
    name: row.name || "",
    status: (row.status as any) || "UPCOMING",
    startDate: formatDate(row.startDate),
    endDate: formatDate(row.endDate),
    registrationDeadline: formatDate(row.registrationDeadline),
    minTeamSize: row.minTeamSize || 1,
    maxTeamSize: row.maxTeamSize || 1,
    description: row.description || "",
    managerId: row.managerId || null
  }
  if (row.requiredSkills && row.requiredSkills.length > 0) {
    editRequiredSkillRows.value = row.requiredSkills.map(s => ({
      skillId: s.skillId,
      importance: s.importance || 3
    }))
  } else {
    editRequiredSkillRows.value = [{ skillId: null, importance: 3 }]
  }
  editDialogError.value = ""
  editDialogVisible.value = true
  loadSkillsForCreate() // Reusing to load teachers
}

const submitEdit = async () => {
  if (!editForm.value.id) return
  const form = editForm.value
  if (!form.name?.trim()) {
    editDialogError.value = "请填写竞赛名称"
    return
  }
  if (!form.startDate || !form.endDate || !form.registrationDeadline) {
    editDialogError.value = "请完整填写日期"
    return
  }
  const requiredSkills = editRequiredSkillRows.value
    .filter((row) => typeof row.skillId === "number")
    .map((row) => ({
      skillId: row.skillId as number,
      importance: Number.isFinite(row.importance) && row.importance > 0 ? row.importance : 1
    }))
  if (!requiredSkills.length) {
    editDialogError.value = "请至少添加一个竞赛技能需求"
    return
  }
  
  editDialogLoading.value = true
  editDialogError.value = ""
  try {
    const payload: CompetitionAdminUpdatePayload = {
      name: form.name.trim(),
      status: form.status,
      startDate: form.startDate,
      endDate: form.endDate,
      registrationDeadline: form.registrationDeadline,
      minTeamSize: form.minTeamSize,
      maxTeamSize: form.maxTeamSize,
      description: form.description?.trim(),
      managerId: form.managerId,
      requiredSkills
    }
    await updateCompetitionAdmin(editForm.value.id, payload)
    ElMessage.success("修改成功")
    editDialogVisible.value = false
    fetchList()
  } catch (error: any) {
    editDialogError.value = error?.message || "修改失败"
  } finally {
    editDialogLoading.value = false
  }
}

const handleDelete = async (row: RecommendationRow | { id?: number }) => {
  if (!row.id) return
  try {
    await deleteCompetition(row.id)
    ElMessage.success("删除成功")
    editDialogVisible.value = false
    fetchList()
  } catch (error: any) {
    showRequestError(error, "删除失败")
  }
}

const addEditRequiredSkillRow = () => {
  if (editRequiredSkillRows.value.length >= 8) return
  editRequiredSkillRows.value.push({ skillId: null, importance: 3 })
}

const removeEditRequiredSkillRow = (index: number) => {
  if (editRequiredSkillRows.value.length <= 1) return
  editRequiredSkillRows.value.splice(index, 1)
}

const parseNumber = (value: unknown, fallback: number) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) return fallback
  if (parsed < 0) return fallback
  return parsed
}

const clampTopK = (value: number) => {
  if (!Number.isFinite(value)) return 10
  if (value < 1) return 1
  if (value > 50) return 50
  return value
}

const readQuery = () => {
  const keyword = typeof route.query.keyword === "string" ? route.query.keyword : ""
  const status = typeof route.query.status === "string" ? route.query.status : ""
  const source =
    route.query.source === "algorithm" || route.query.source === "list"
      ? (route.query.source as "list" | "algorithm")
      : "list"
  const topKValue = clampTopK(parseNumber(route.query.topK, 10))
  const page = parseNumber(route.query.page, 0)
  const size = parseNumber(route.query.size, 10)
  return {
    keyword,
    status: status as StatusFilterValue,
    source,
    topK: topKValue,
    page,
    size: size > 0 ? size : 10
  }
}

const applyQueryFromRoute = () => {
  const next = readQuery()
  const same =
    filters.keyword === next.keyword &&
    filters.status === next.status &&
    sourceMode.value === next.source &&
    topK.value === next.topK &&
    pagination.page === next.page &&
    pagination.size === next.size
  if (same) return false
  isApplying.value = true
  filters.keyword = next.keyword
  filters.status = next.status
  sourceMode.value = next.source
  topK.value = next.topK
  if (isTeacher.value && sourceMode.value === "algorithm") {
    sourceMode.value = "list"
  }
  pagination.page = next.page
  pagination.size = next.size
  return true
}

watch(
  () => filters.keyword,
  () => {
    if (!initialized.value || isApplying.value) return
    if (keywordTimer) {
      window.clearTimeout(keywordTimer)
    }
    keywordTimer = window.setTimeout(() => {
      pagination.page = 0
      syncUrl()
      fetchList()
    }, 400)
  }
)

watch(
  () => filters.status,
  () => {
    if (!initialized.value || isApplying.value) return
    pagination.page = 0
    syncUrl()
    fetchList()
  }
)

watch(
  () => topK.value,
  () => {
    if (!initialized.value || isApplying.value) return
    if (sourceMode.value !== "algorithm") return
    pagination.page = 0
    syncUrl()
    fetchList()
  }
)

watch(
  () => sourceMode.value,
  () => {
    if (!initialized.value || isApplying.value) return
    rows.value = []
    total.value = null
    pagination.page = 0
    syncUrl()
    fetchList()
  }
)

watch(
  () => [pagination.page, pagination.size],
  () => {
    if (!initialized.value || isApplying.value) return
    if (sourceMode.value === "algorithm") return
    syncUrl()
    fetchList()
  }
)

onMounted(() => {
  const applied = applyQueryFromRoute()
  initialized.value = true
  if (isTeacher.value && sourceMode.value === "algorithm") {
    sourceMode.value = "list"
  }
  fetchList()
  if (applied) {
    window.setTimeout(() => {
      isApplying.value = false
    }, 0)
  }
})

watch(
  () => route.query,
  () => {
    if (!initialized.value) return
    const applied = applyQueryFromRoute()
    if (applied) {
      syncUrl()
      fetchList()
      window.setTimeout(() => {
        isApplying.value = false
      }, 0)
    }
  }
)

onBeforeUnmount(() => {
  if (keywordTimer) {
    window.clearTimeout(keywordTimer)
  }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>竞赛列表</h2>
      <el-button v-if="isAdmin" type="primary" size="default" @click="openCreateDialog">发布竞赛</el-button>
    </div>

    <el-card shadow="never" v-loading="loading" class="competition-list-page">
      <el-form class="filter-bar" label-position="top" label-width="120px">
        <el-row :gutter="12">
          <el-col :span="isTeacher ? 24 : 16">
            <div v-if="!isAlgorithmMode" class="competition-filter-group">
              <el-form-item label="关键词">
                <el-input
                  v-model="filters.keyword"
                  class="competition-search-input"
                  clearable
                  placeholder="关键词"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="filters.status"
                  class="competition-status-select"
                  clearable
                  placeholder="状态"
                >
                  <el-option
                    v-for="item in statusOptions"
                    :key="`status-${item.value}`"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label=" " label-width="80px">
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </div>
            <div v-else class="algorithm-topk-placeholder">
              <el-form-item label="推荐数量">
                <el-input-number v-model="topK" :min="1" :max="50" :disabled="sourceMode !== 'algorithm'" />
              </el-form-item>
            </div>
          </el-col>

          <el-col v-if="!isTeacher && !isAdmin" :span="8" class="filter-right">
            <div class="filter-right__inner">
              <el-form-item label="来源">
                <el-radio-group v-model="sourceMode">
                  <el-radio-button label="list">列表</el-radio-button>
                  <el-radio-button label="algorithm">算法</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <el-alert
        v-if="fallbackNotice"
        type="info"
        :closable="false"
        :title="fallbackNotice"
        style="margin-bottom: 12px"
      />

      <el-alert
        v-if="errorMessage"
        type="error"
        :closable="false"
        :title="errorMessage"
        style="margin-bottom: 12px"
      />

      <el-table
        :data="rows"
        style="width: 100%"
        @row-click="goDetail"
        v-loading="loading"
        highlight-current-row
      >
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            <span v-if="row.managerName">{{ row.managerName }}</span>
            <span v-else style="color: #999">未指定</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="competition" />
          </template>
        </el-table-column>
        <el-table-column label="日期范围" min-width="200">
          <template #default="{ row }">
            {{ formatDateRange(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="organizer" label="主办方" min-width="120" />
        <el-table-column
          v-if="isAlgorithmMode"
          label="推荐分数"
          width="96"
          align="left"
          class-name="recommend-score-col"
          header-cell-class-name="recommend-score-col-header"
        >
          <template #default="{ row }">
            <span v-if="typeof row.score === 'number'">{{ row.score.toFixed(3) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isStudent || isTeacher || isAdmin" label="操作" width="180" align="center">
          <template #default="{ row }">
            <div @click.stop>
              <template v-if="isAdmin">
                <el-button type="primary" link @click="goDetail(row)">详情</el-button>
                <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
              </template>
              <template v-else>
                <el-button type="primary" link @click="handleEnrollAction(row)">
                  申请
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && !errorMessage && rows.length === 0" class="empty-state">
        <span v-if="sourceMode === 'algorithm'">暂无推荐，请完善技能。</span>
        <span v-else>暂无竞赛</span>
      </div>

      <div v-if="total !== null && sourceMode === 'list'" class="pagination">
        <el-pagination
          :current-page="pagination.page + 1"
          :page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="createDialogVisible"
      title="发布竞赛"
      width="760px"
      class="create-competition-dialog"
      append-to-body
      top="6vh"
      :close-on-click-modal="false"
      :before-close="closeCreateDialog"
    >
      <el-form label-position="top" class="create-form-grid">
        <el-form-item label="竞赛名称">
          <el-input v-model="createForm.name" placeholder="请输入竞赛名称" />
        </el-form-item>
        <el-form-item label="竞赛负责人">
          <el-select v-model="createForm.managerId" placeholder="请选择竞赛负责人（可选）" clearable :loading="teachersLoading">
            <el-option
              v-for="teacher in allTeachers"
              :key="teacher.id"
              :label="teacher.realName || teacher.username"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方">
          <el-input v-model="createForm.organizer" placeholder="请输入主办方（可选）" />
        </el-form-item>
        <el-form-item label="级别">
          <el-input v-model="createForm.level" placeholder="请输入级别（可选）" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="createForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="createForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" />
        </el-form-item>
        <el-form-item label="报名截止">
          <el-date-picker
            v-model="createForm.registrationDeadline"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择报名截止日期"
          />
        </el-form-item>
        <el-form-item label="最小队伍人数">
          <el-input-number v-model="createForm.minTeamSize" :min="1" :controls="true" />
        </el-form-item>
        <el-form-item label="最大队伍人数">
          <el-input-number v-model="createForm.maxTeamSize" :min="1" :controls="true" />
        </el-form-item>
        <el-form-item label="竞赛技能需求" class="required-skills-item">
          <div class="required-skills-editor">
            <div
              v-for="(row, index) in requiredSkillRows"
              :key="`required-skill-${index}`"
              class="required-skill-row"
            >
              <el-select
                v-model="row.skillId"
                filterable
                clearable
                placeholder="请选择技能"
                :loading="skillsLoading"
                :disabled="skillsLoading"
                class="required-skill-select"
              >
                <el-option
                  v-for="s in allSkills"
                  :key="`skill-${s.id}`"
                  :label="s.name || `ID:${s.id}`"
                  :value="s.id"
                  :disabled="typeof s.id === 'number' && row.skillId !== s.id && selectedRequiredSkillIds.has(s.id)"
                />
              </el-select>
              <el-input-number v-model="row.importance" :min="1" :max="10" class="required-skill-weight" />
              <el-button :disabled="requiredSkillRows.length <= 1" @click="removeRequiredSkillRow(index)">删除</el-button>
            </div>
            <div class="required-skill-actions">
              <el-button type="primary" plain :disabled="requiredSkillRows.length >= 8" @click="addRequiredSkillRow">
                添加技能
              </el-button>
              <span class="skills-inline-hint">至少 1 项，最多 8 项</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="竞赛说明">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入竞赛说明（可选）" />
        </el-form-item>
      </el-form>
      <el-alert v-if="createDialogError" type="error" :closable="false" :title="createDialogError" />
      <template #footer>
        <el-button :disabled="createDialogLoading" @click="closeCreateDialog">取消</el-button>
        <el-button type="primary" :loading="createDialogLoading" @click="submitCreate">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="teacherApplyDialogVisible"
      width="700px"
      class="teacher-apply-dialog"
      append-to-body
      top="10vh"
      :close-on-click-modal="false"
      :before-close="closeTeacherApplyDialog"
    >
      <template #header>
        <div class="teacher-apply-dialog__header">
          <div class="teacher-apply-dialog__title-row">
            <el-icon class="teacher-apply-dialog__title-icon"><Document /></el-icon>
            <span class="teacher-apply-dialog__title-text">创建队伍申请</span>
          </div>
          <div class="teacher-apply-dialog__header-tip">提交后进入管理员审核流程</div>
        </div>
      </template>
      <div class="teacher-apply-dialog__summary">
        <div class="teacher-apply-dialog__summary-label">当前竞赛</div>
        <div class="teacher-apply-dialog__summary-value">{{ teacherApplyCompetitionName }}</div>
      </div>
      <el-form label-position="top" class="teacher-apply-dialog__form">
        <el-form-item label="队伍说明（可选）">
          <el-input
            v-model="teacherApplyDescription"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="例如：队伍方向、招募偏好、预期目标等"
          />
        </el-form-item>
        <el-form-item label="队伍青睐技能">
          <div class="teacher-apply-skills-editor">
            <div
              v-for="(row, index) in teacherApplySkillRows"
              :key="`teacher-apply-skill-${index}`"
              class="teacher-apply-skill-row"
            >
              <el-select
                v-model="row.skillId"
                filterable
                clearable
                placeholder="请选择技能"
                :loading="skillsLoading"
                :disabled="skillsLoading"
                class="teacher-apply-skill-select"
              >
                <el-option
                  v-for="s in allSkills"
                  :key="`teacher-apply-skill-option-${s.id}`"
                  :label="s.name || `ID:${s.id}`"
                  :value="s.id"
                  :disabled="typeof s.id === 'number' && row.skillId !== s.id && selectedTeacherApplySkillIds.has(s.id)"
                />
              </el-select>
              <el-input-number v-model="row.weight" :min="1" :max="10" class="teacher-apply-skill-weight" />
              <el-button type="danger" link :disabled="teacherApplySkillRows.length <= 1" @click="removeTeacherApplySkillRow(index)">
                删除
              </el-button>
            </div>
            <div class="teacher-apply-skill-actions">
              <el-button type="primary" plain :disabled="teacherApplySkillRows.length >= 8" @click="addTeacherApplySkillRow">
                添加技能
              </el-button>
              <span class="skills-inline-hint">可选，最多 8 项</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <el-alert v-if="teacherApplyDialogError" type="error" :closable="false" :title="teacherApplyDialogError" />
      <template #footer>
        <el-button :disabled="teacherApplyDialogLoading" @click="closeTeacherApplyDialog">取消</el-button>
        <el-button type="primary" :loading="teacherApplyDialogLoading" @click="submitTeacherApply">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑竞赛信息"
      width="760px"
      class="edit-competition-dialog"
      append-to-body
      top="6vh"
      :close-on-click-modal="false"
    >
      <el-form label-position="top" class="create-form-grid">
        <el-divider content-position="left">状态与负责人</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛状态">
              <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="未开始" value="UPCOMING" />
                <el-option label="进行中" value="ONGOING" />
                <el-option label="已结束" value="FINISHED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛负责人">
              <el-select v-model="editForm.managerId" placeholder="请选择竞赛负责人（可选）" clearable :loading="teachersLoading" style="width: 100%">
                <el-option
                  v-for="teacher in allTeachers"
                  :key="teacher.id"
                  :label="teacher.realName || teacher.username"
                  :value="teacher.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">详细信息</el-divider>
        <el-form-item label="竞赛名称">
          <el-input v-model="editForm.name" placeholder="请输入竞赛名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开始日期">
              <el-date-picker v-model="editForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束日期">
              <el-date-picker v-model="editForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报名截止">
              <el-date-picker
                v-model="editForm.registrationDeadline"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="截止日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小队伍人数">
              <el-input-number v-model="editForm.minTeamSize" :min="1" :controls="true" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大队伍人数">
              <el-input-number v-model="editForm.maxTeamSize" :min="1" :controls="true" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="竞赛技能需求" class="required-skills-item">
          <div class="required-skills-editor">
            <div
              v-for="(row, index) in editRequiredSkillRows"
              :key="`edit-required-skill-${index}`"
              class="required-skill-row"
            >
              <el-select
                v-model="row.skillId"
                filterable
                clearable
                placeholder="请选择技能"
                :loading="skillsLoading"
                :disabled="skillsLoading"
                class="required-skill-select"
              >
                <el-option
                  v-for="s in allSkills"
                  :key="`edit-skill-${s.id}`"
                  :label="s.name || `ID:${s.id}`"
                  :value="s.id"
                  :disabled="typeof s.id === 'number' && row.skillId !== s.id && editRequiredSkillRows.some(r => r.skillId === s.id)"
                />
              </el-select>
              <el-input-number v-model="row.importance" :min="1" :max="10" class="required-skill-weight" />
              <el-button :disabled="editRequiredSkillRows.length <= 1" @click="removeEditRequiredSkillRow(index)">删除</el-button>
            </div>
            <div class="required-skill-actions">
              <el-button type="primary" plain :disabled="editRequiredSkillRows.length >= 8" @click="addEditRequiredSkillRow">
                添加技能
              </el-button>
              <span class="skills-inline-hint">至少 1 项，最多 8 项</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="竞赛说明">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入竞赛说明（可选）" />
        </el-form-item>
      </el-form>
      <el-alert v-if="editDialogError" type="error" :closable="false" :title="editDialogError" />
      <template #footer>
        <div class="edit-dialog-footer">
          <el-popconfirm title="确定要删除该竞赛吗？此操作不可撤销。" @confirm="handleDelete(editForm)">
            <template #reference>
              <el-button type="danger" plain :loading="editDialogLoading">删除竞赛</el-button>
            </template>
          </el-popconfirm>
          <div class="footer-right">
            <el-button :disabled="editDialogLoading" @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="editDialogLoading" @click="submitEdit">保存修改</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filter-bar {
  margin-bottom: 8px;
}

.competition-filter-group {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.algorithm-topk-placeholder {
  display: flex;
  align-items: flex-end;
  min-height: 74px;
}

.edit-dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.footer-right {
  display: flex;
  gap: 12px;
}

.filter-right {
  display: flex;
  justify-content: flex-end;
}

.filter-right__inner {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.competition-search-input {
  width: 100%;
  max-width: 200px;
  flex-shrink: 0;
}

.competition-status-select {
  width: 180px;
  min-width: 180px;
  flex: 0 0 auto;
}

.competition-list-page :deep(.el-radio-button__inner) {
  color: #606266;
  background-color: #ffffff;
  border-color: #dcdfe6;
}

.competition-list-page :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  color: #303133;
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  box-shadow: none;
}

.competition-list-page :deep(.recommend-score-col .cell),
.competition-list-page :deep(.recommend-score-col-header .cell) {
  padding-left: 2px;
  padding-right: 2px;
}

.teacher-apply-dialog :deep(.el-dialog) {
  border: 1px solid #dbe3ef;
  border-radius: 12px;
  box-shadow: 0 22px 52px rgba(15, 23, 42, 0.14);
}

.teacher-apply-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 18px 20px 10px;
}

.teacher-apply-dialog :deep(.el-dialog__body) {
  padding: 12px 20px 10px;
}

.teacher-apply-dialog :deep(.el-dialog__footer) {
  padding: 12px 20px 18px;
  border-top: 1px solid #edf1f7;
}

.teacher-apply-dialog__header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.teacher-apply-dialog__title-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.teacher-apply-dialog__title-icon {
  color: #5f6b7a;
  font-size: 18px;
}

.teacher-apply-dialog__title-text {
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
}

.teacher-apply-dialog__header-tip {
  color: #8a94a3;
  font-size: 13px;
}

.teacher-apply-dialog__summary {
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #e8edf5;
  background: #f7faff;
}

.teacher-apply-dialog__summary-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.teacher-apply-dialog__summary-value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.teacher-apply-dialog__form {
  margin-top: 2px;
}

.teacher-apply-dialog__form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.teacher-apply-skills-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.teacher-apply-skill-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px 88px;
  gap: 10px;
  align-items: center;
}

.teacher-apply-skill-select,
.teacher-apply-skill-weight {
  width: 100%;
}

.teacher-apply-skill-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 2px;
}

.empty-state {
  padding: 16px 0;
  color: #6b7280;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.create-form-grid {
  max-height: calc(76vh - 120px);
  overflow: auto;
  padding-right: 4px;
}

.required-skills-item :deep(.el-form-item__content) {
  display: block;
}

.required-skills-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.required-skill-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px 88px;
  gap: 8px;
  align-items: center;
}

.required-skill-select,
.required-skill-weight {
  width: 100%;
}

.required-skill-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.skills-inline-hint {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 900px) {
  .teacher-apply-skill-row {
    grid-template-columns: 1fr;
  }

  .required-skill-row {
    grid-template-columns: 1fr;
  }
}
</style>




