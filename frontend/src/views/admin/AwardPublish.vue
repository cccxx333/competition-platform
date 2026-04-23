<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getCompetitionDetail, listCompetitions, type CompetitionListItem } from "@/api/competitions"
import { listAwardRecords, publishAward, type AwardPublishResponse, type AwardRecordItem } from "@/api/awards"
import { listTeams, type TeamDto } from "@/api/teams"
import { useAuthStore } from "@/stores/auth"
import { toYmd } from "@/common/utils/datetime"
import { getApiErrorMessage } from "@/utils/errorMessage"

type TeacherOption = {
  id: number
  name: string
}

const authStore = useAuthStore()
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")

const submitting = ref(false)
const loadingRecords = ref(false)
const loadingCompetitions = ref(false)
const loadingTeams = ref(false)

const form = reactive({
  competitionId: null as number | null,
  teacherId: null as number | null,
  teamId: null as number | null,
  awardName: ""
})

const lastResult = ref<AwardPublishResponse | null>(null)
const records = ref<AwardRecordItem[]>([])
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const competitionNameMap = ref<Record<number, string>>({})

const finishedCompetitions = ref<CompetitionListItem[]>([])
const allCompetitionTeams = ref<TeamDto[]>([])

const finishedCompetitionOptions = computed(
  () => finishedCompetitions.value.filter((item): item is CompetitionListItem & { id: number } => typeof item.id === "number")
)

const teacherOptions = computed<TeacherOption[]>(() => {
  const map = new Map<number, string>()
  allCompetitionTeams.value.forEach((team) => {
    const leaderId = team.leader?.id
    if (typeof leaderId !== "number") return
    const name = team.leader?.realName?.trim() || team.leader?.username?.trim() || `教师 ${leaderId}`
    if (!map.has(leaderId)) {
      map.set(leaderId, name)
    }
  })
  return Array.from(map.entries())
    .map(([id, name]) => ({ id, name }))
    .sort((a, b) => a.name.localeCompare(b.name, "zh-CN"))
})

const teamOptions = computed(() => {
  const teacherId = form.teacherId
  return allCompetitionTeams.value
    .filter((team): team is TeamDto & { id: number } => typeof team.id === "number")
    .filter((team) => {
      if (typeof teacherId !== "number") return false
      return team.leader?.id === teacherId
    })
})

const publishDisabled = computed(() => {
  if (!isAdmin.value || submitting.value) return true
  return !(form.competitionId && form.teacherId && form.teamId && form.awardName.trim())
})

const resetResult = () => {
  lastResult.value = null
}

const showError = (message: string) => {
  errorDialogMessage.value = message
  errorDialogVisible.value = true
}

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "请求参数无效"
  if (status === 401) return "会话已过期，请重新登录"
  if (status === 403) return "无权限（仅管理员）"
  if (status === 404) return "竞赛或队伍不存在"
  if (status === 409) return "业务冲突，请检查竞赛状态和队伍状态"
  return "发布奖项失败"
}

const getTeamStatusText = (value?: string) => {
  if (value === "RECRUITING") return "招募中"
  if (value === "CLOSED") return "停止招募"
  if (value === "DISBANDED") return "已解散"
  return value || "-"
}

const formatDate = (value?: string | null) => {
  if (!value) return ""
  return value.includes("T") ? value.split("T")[0] : value
}

const formatCompetitionOptionLabel = (item: CompetitionListItem) => {
  const name = item.name || `竞赛 ${item.id ?? "-"}`
  const start = formatDate(item.startDate)
  const end = formatDate(item.endDate)
  const range = [start, end].filter(Boolean).join(" ~ ")
  return range ? `${name}（${range}）` : name
}

const loadFinishedCompetitions = async () => {
  loadingCompetitions.value = true
  try {
    const { items } = await listCompetitions({
      status: "FINISHED",
      page: 0,
      size: 300
    })
    finishedCompetitions.value = (items ?? []).slice()
    if (!finishedCompetitions.value.some((item) => item.id === form.competitionId)) {
      form.competitionId = null
      form.teacherId = null
      form.teamId = null
      allCompetitionTeams.value = []
    }
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    showError(getApiErrorMessage(error, fallback))
  } finally {
    loadingCompetitions.value = false
  }
}

const loadTeamsForCompetition = async (competitionId: number) => {
  loadingTeams.value = true
  try {
    const { items } = await listTeams({ page: 0, size: 1000 })
    allCompetitionTeams.value = (items ?? []).filter((team) => team.competition?.id === competitionId)
    if (!teacherOptions.value.some((teacher) => teacher.id === form.teacherId)) {
      form.teacherId = null
    }
    if (!teamOptions.value.some((team) => team.id === form.teamId)) {
      form.teamId = null
    }
  } catch (error: any) {
    allCompetitionTeams.value = []
    form.teacherId = null
    form.teamId = null
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    showError(getApiErrorMessage(error, fallback))
  } finally {
    loadingTeams.value = false
  }
}

watch(
  () => form.competitionId,
  (competitionId) => {
    form.teacherId = null
    form.teamId = null
    allCompetitionTeams.value = []
    if (typeof competitionId === "number" && competitionId > 0) {
      void loadTeamsForCompetition(competitionId)
    }
  }
)

watch(
  () => form.teacherId,
  () => {
    if (!teamOptions.value.some((team) => team.id === form.teamId)) {
      form.teamId = null
    }
  }
)

const handleSubmit = async () => {
  if (!isAdmin.value) {
    showError("无权限（仅管理员）")
    return
  }
  if (!form.competitionId) {
    showError("请选择已结束竞赛")
    return
  }
  if (!form.teacherId) {
    showError("请选择教师")
    return
  }
  if (!form.teamId) {
    showError("请选择竞赛队伍")
    return
  }
  const awardName = form.awardName.trim()
  if (!awardName) {
    showError("奖项名称不能为空")
    return
  }

  submitting.value = true
  resetResult()
  try {
    const result = await publishAward({
      competitionId: form.competitionId,
      teamId: form.teamId,
      awardName
    })
    lastResult.value = result
    ElMessage.success("奖项已发布")
    await loadRecords(true)
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    const message = getApiErrorMessage(error, fallback)
    showError(message)
  } finally {
    submitting.value = false
  }
}

const loadCompetitionNames = async (list: AwardRecordItem[]) => {
  const ids = Array.from(
    new Set(list.map((item) => item.competitionId).filter((id): id is number => typeof id === "number" && id > 0))
  )
  if (!ids.length) {
    competitionNameMap.value = {}
    return
  }
  try {
    const pairs = await Promise.all(
      ids.map(async (id) => {
        const detail = await getCompetitionDetail(id)
        return [id, detail?.name ?? "-"] as const
      })
    )
    const next: Record<number, string> = {}
    for (const [id, name] of pairs) {
      next[id] = name
    }
    competitionNameMap.value = next
  } catch {
    competitionNameMap.value = {}
  }
}

const loadRecords = async (useFilters = false) => {
  loadingRecords.value = true
  try {
    const params = useFilters
      ? {
          competitionId: form.competitionId ?? undefined,
          teamId: form.teamId ?? undefined
        }
      : {}
    records.value = await listAwardRecords(params)
    await loadCompetitionNames(records.value)
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    const message = getApiErrorMessage(error, fallback)
    showError(message)
  } finally {
    loadingRecords.value = false
  }
}

const resetSelection = () => {
  form.competitionId = null
  form.teacherId = null
  form.teamId = null
  form.awardName = ""
  allCompetitionTeams.value = []
  resetResult()
}

onMounted(async () => {
  await loadFinishedCompetitions()
  await loadRecords(false)
})
</script>

<template>
  <div class="page-container">
    <div class="page-header admin-header">
      <h2>发布奖项</h2>
    </div>

    <div class="admin-content-shell">
      <el-card shadow="never" class="publish-card">
        <el-alert v-if="!isAdmin" type="warning" show-icon title="仅管理员可发布奖项" class="status-alert" />

        <el-form label-position="top" class="publish-form">
          <el-form-item label="已结束竞赛" required>
            <el-select
              v-model="form.competitionId"
              filterable
              clearable
              placeholder="请选择已结束竞赛"
              :loading="loadingCompetitions"
              :disabled="submitting"
            >
              <el-option
                v-for="item in finishedCompetitionOptions"
                :key="`competition-${item.id}`"
                :label="formatCompetitionOptionLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教师" required>
            <el-select
              v-model="form.teacherId"
              filterable
              clearable
              placeholder="请先选择竞赛"
              :disabled="submitting || !form.competitionId"
            >
              <el-option
                v-for="teacher in teacherOptions"
                :key="`teacher-${teacher.id}`"
                :label="teacher.name"
                :value="teacher.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="竞赛队伍" required>
            <el-select
              v-model="form.teamId"
              filterable
              clearable
              placeholder="请先选择教师"
              :loading="loadingTeams"
              :disabled="submitting || !form.competitionId || !form.teacherId"
            >
              <el-option
                v-for="team in teamOptions"
                :key="`team-${team.id}`"
                :label="`${team.name || `队伍 ${team.id ?? '-'}`}（${getTeamStatusText(team.status)}）`"
                :value="team.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="奖项名称" required>
            <el-input
              v-model="form.awardName"
              maxlength="64"
              show-word-limit
              placeholder="例如：一等奖"
              :disabled="submitting"
            />
          </el-form-item>

          <div class="publish-actions">
            <el-button type="primary" :loading="submitting" :disabled="publishDisabled" @click="handleSubmit">发布</el-button>
            <el-button :disabled="submitting" @click="resetSelection">重置</el-button>
          </div>
        </el-form>

        <el-card v-if="lastResult" shadow="never" class="result-card">
          <h3>发布结果</h3>
          <el-descriptions :column="1">
            <el-descriptions-item label="奖项 ID">{{ lastResult.awardId ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="竞赛 ID">{{ lastResult.competitionId ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="队伍 ID">{{ lastResult.teamId ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="奖项名称">{{ lastResult.awardName ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="获奖人数">{{ lastResult.recipientCount ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="获奖用户 ID">
              {{ lastResult.recipientUserIds?.length ? lastResult.recipientUserIds.join(", ") : "-" }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="result-hint">可使用获奖成员账号验证荣誉页面。</div>
        </el-card>
      </el-card>
    </div>

    <div class="page-header admin-header">
      <h2>奖项记录</h2>
    </div>

    <el-card shadow="never" class="records-card">
      <el-table v-if="records.length" :data="records" v-loading="loadingRecords" style="width: 100%">
        <el-table-column prop="awardId" label="奖项 ID" width="120" />
        <el-table-column label="竞赛" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ typeof row.competitionId === "number" ? competitionNameMap[row.competitionId] ?? "-" : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="teamId" label="队伍 ID" width="120" />
        <el-table-column prop="awardName" label="奖项名称" min-width="160" />
        <el-table-column prop="recipientCount" label="获奖人数" width="120" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ row.publishedAt ? toYmd(row.publishedAt) : "-" }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loadingRecords" description="暂无奖项记录" />
    </el-card>

    <el-dialog v-model="errorDialogVisible" title="发布失败" width="420px">
      <div>{{ errorDialogMessage }}</div>
      <template #footer>
        <el-button type="primary" @click="errorDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.status-alert {
  margin-bottom: 12px;
}

.publish-form {
  max-width: 560px;
}

.publish-actions {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.admin-content-shell {
  max-width: 980px;
  margin: 0 auto;
}

.publish-card {
  width: 620px;
  max-width: 100%;
  margin: 0;
}

.records-card {
  max-width: 980px;
  margin: 0 auto;
}

.admin-header {
  max-width: 980px;
  margin: 0 auto 12px;
}

.result-card {
  margin-top: 16px;
}

.records-card {
  margin-top: 0;
}

.result-hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}
</style>
