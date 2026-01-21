<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem, type CompetitionListParams } from "@/api/competitions"
import StatusPill from "@@/components/StatusPill/index.vue"
import { useAuthStore } from "@/stores/auth"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isTeacher = computed(() => roleUpper.value === "TEACHER")
const isAdmin = computed(() => roleUpper.value === "ADMIN")

type RecommendationRow = {
  id?: number
  name?: string
  status?: CompetitionListItem["status"] | string
  startDate?: string
  endDate?: string
  organizer?: string
  score?: number
  reason?: string
  source: "list" | "algorithm"
}

const rows = ref<RecommendationRow[]>([])
const loading = ref(false)
const errorMessage = ref("")
const total = ref<number | null>(null)
const initialized = ref(false)
const isApplying = ref(false)

type StatusFilterValue = "" | "UPCOMING" | "ONGOING" | "FINISHED"

const filters = reactive({
  keyword: "",
  status: "" as StatusFilterValue
})

const sourceMode = ref<"list" | "algorithm">("list")
const topK = ref(10)

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
  try {
    if (sourceMode.value === "algorithm") {
      const { items: data } = await listCompetitions({
        recommend: true,
        topK: topK.value,
        page: 0,
        size: topK.value
      })
      rows.value = sortRows(
        data.map((item) => ({
          id: item.id,
          name: item.name,
          status: item.status,
          startDate: item.startDate,
          endDate: item.endDate,
          organizer: item.organizer,
          score: item.matchScore,
          reason: item.recommendReason,
          source: "algorithm"
        }))
      )
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
        organizer: item.organizer,
        source: "list"
      }))
      total.value = typeof totalElements === "number" ? totalElements : null
      if (typeof page === "number" && page !== pagination.page) {
        pagination.page = page
      }
      if (typeof size === "number" && size !== pagination.size) {
        pagination.size = size
      }
    }
  } catch (error: any) {
    rows.value = []
    total.value = null
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
  router.push(`/competitions/${row.id}`)
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
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>竞赛列表</h2>
    </div>

    <el-form class="filter-bar" label-position="top" label-width="120px">
      <el-row :gutter="12">
        <el-col :span="isTeacher ? 24 : 16">
          <div class="competition-filter-group">
            <el-form-item label="关键词">
              <el-input
                v-model="filters.keyword"
                class="competition-search-input"
                clearable
                placeholder="关键词"
                :disabled="sourceMode === 'algorithm'"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="filters.status"
                class="competition-status-select"
                clearable
                placeholder="状态"
                :disabled="sourceMode === 'algorithm'"
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
              <el-button type="primary" @click="resetFilters">重置</el-button>
            </el-form-item>
          </div>
        </el-col>
        <el-col v-if="!isTeacher && !isAdmin" :span="8" class="filter-right">
          <div class="filter-right__inner">
            <el-form-item v-if="sourceMode === 'algorithm'" label="推荐数量">
              <el-input-number v-model="topK" :min="1" :max="50" :disabled="sourceMode !== 'algorithm'" />
            </el-form-item>
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
      v-if="sourceMode === 'algorithm'"
      type="info"
      :closable="false"
      title="算法模式仅使用推荐数量，关键词和状态不可用。"
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
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <StatusPill :value="row.status" kind="competition" />
        </template>
      </el-table-column>
      <el-table-column label="日期范围" min-width="200">
        <template #default="{ row }">
          {{ formatDateRange(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="organizer" label="主办方" min-width="180" />
      <el-table-column label="推荐信息" min-width="220">
        <template #default="{ row }">
          <div v-if="row.score !== undefined || row.reason">
            <div v-if="row.score !== undefined">匹配分：{{ row.score.toFixed(3) }}</div>
            <div v-if="row.reason">原因：{{ row.reason }}</div>
          </div>
          <span v-else>-</span>
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

.empty-state {
  padding: 16px 0;
  color: #6b7280;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
