<script lang="ts" setup>
import { ElMessage } from "element-plus"
import {
  closeTeam,
  disbandTeam,
  getTeamAwardSummary,
  getTeamDetail,
  listTeamMembers,
  reopenRecruiting,
  type TeamAwardSummary,
  type TeamDto,
  type TeamMemberView
} from "@/api/teams"
import { useAuthStore } from "@/stores/auth"
import { canToggleRecruiting, getTeamWriteBlockReason, isDisbanded } from "@/utils/teamGuards"
import { getApiErrorMessage } from "@/utils/errorMessage"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const team = ref<TeamDto | null>(null)
const members = ref<TeamMemberView[]>([])
const membersLoaded = ref(false)
const awardSummary = ref<TeamAwardSummary | null>(null)
const awardLoading = ref(false)
const awardError = ref("")
const loading = ref(false)
const actionLoading = ref(false)
const closeDialogVisible = ref(false)
const disbandDialogVisible = ref(false)
const disbandLoading = ref(false)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)

const teamId = computed(() => {
  const raw = Number(route.params.teamId)
  return Number.isNaN(raw) ? null : raw
})

const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")
const isLeader = computed(() => {
  const leaderId = team.value?.leader?.id
  const currentUserId = authStore.user?.id
  return Boolean(leaderId && currentUserId && leaderId === currentUserId)
})
const isTeamDisbanded = computed(() => isDisbanded(team.value))
const returnLabel = computed(() => (roleUpper.value === "STUDENT" ? "返回我的队伍" : "返回队伍查询"))
const returnPath = computed(() => (roleUpper.value === "STUDENT" ? "/teams/my" : "/teams/lookup"))
const writeBlockReason = computed(() => getTeamWriteBlockReason(team.value))
const currentCount = computed(() => {
  if (membersLoaded.value) return String(members.value.length)
  const fallback = team.value?.currentMembers
  return typeof fallback === "number" ? String(fallback) : "-"
})

const canShowClose = computed(() => roleUpper.value === "TEACHER")
const canClose = computed(() => canToggleRecruiting(team.value, roleUpper.value, isLeader.value))
const recruitingToggleLabel = computed(() => (team.value?.status === "CLOSED" ? "恢复招募" : "停止招募"))

const closeDisabledReason = computed(() => {
  if (!team.value) return "队伍不存在"
  if (isTeamDisbanded.value) return "队伍已解散"
  if (team.value.status !== "RECRUITING" && team.value.status !== "CLOSED") return "仅支持在招募中/已关闭间切换"
  if (roleUpper.value === "TEACHER" && !isLeader.value) return "仅队长可切换招募状态"
  if (roleUpper.value !== "TEACHER") return "无权限"
  return ""
})

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "参数错误"
  if (status === 401) return "登录已过期，请重新登录"
  if (status === 403) return "无权限访问"
  if (status === 404) return "队伍不存在或已删除"
  if (status === 409) return "操作冲突，请稍后重试"
  return "服务异常，请稍后重试"
}

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const fallbackMessage = status ? getFallbackMessage(status) : fallback
  const message = getApiErrorMessage(error, fallbackMessage)
  errorDialogMessage.value = message
  errorDialogVisible.value = true
  redirectAfterError.value = null
  return message
}

const toYmd = (value?: string | null) => {
  if (!value) return ""
  const text = String(value)
  return text.length >= 10 ? text.slice(0, 10) : text
}

const loadAward = async () => {
  if (!teamId.value) return
  awardLoading.value = true
  awardError.value = ""
  try {
    awardSummary.value = await getTeamAwardSummary(teamId.value)
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallbackMessage = status ? getFallbackMessage(status) : "加载队伍奖项失败"
    awardError.value = getApiErrorMessage(error, fallbackMessage)
    awardSummary.value = null
  } finally {
    awardLoading.value = false
  }
}

const loadTeam = async () => {
  if (!teamId.value) {
    errorDialogMessage.value = "缺少 teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    team.value = await getTeamDetail(teamId.value)
    await loadAward()
    membersLoaded.value = false
    try {
      members.value = await listTeamMembers(teamId.value)
      membersLoaded.value = true
    } catch (error: any) {
      const status = error?.status ?? error?.response?.status
      if (status !== 403) {
        showRequestError(error, "加载队伍成员失败")
      }
    }
  } catch (error: any) {
    team.value = null
    awardSummary.value = null
    showRequestError(error, "加载队伍详情失败")
  } finally {
    loading.value = false
  }
}

const handleDisbandedRedirect = () => {
  errorDialogMessage.value = "队伍已解散，操作已禁止"
  errorDialogVisible.value = true
  redirectAfterError.value = returnPath.value
}

const onCloseErrorDialog = () => {
  errorDialogVisible.value = false
  redirectAfterError.value = null
}

const submitToggleRecruiting = async () => {
  if (!teamId.value) return
  const isReopen = team.value?.status === "CLOSED"
  actionLoading.value = true
  try {
    if (isReopen) {
      await reopenRecruiting(teamId.value)
    } else {
      await closeTeam(teamId.value)
    }
    ElMessage.success(isReopen ? "已恢复招募" : "已停止招募")
    closeDialogVisible.value = false
    await loadTeam()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "切换招募状态失败")
    }
  } finally {
    actionLoading.value = false
  }
}

const submitDisbandTeam = async () => {
  if (!teamId.value) return
  disbandLoading.value = true
  try {
    await disbandTeam(teamId.value)
    ElMessage.success("队伍已解散")
    disbandDialogVisible.value = false
    await loadTeam()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "解散队伍失败")
    }
  } finally {
    disbandLoading.value = false
  }
}

onMounted(loadTeam)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>队伍详情</h2>
        <div class="page-subtitle">队伍 ID：{{ teamId ?? "-" }}</div>
      </div>
      <div class="page-actions">
        <el-button @click="router.push(returnPath)">{{ returnLabel }}</el-button>
        <el-button type="primary" :disabled="!teamId" @click="router.push(`/teams/${teamId}/members`)">
          成员信息
        </el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-alert
        v-if="writeBlockReason"
        type="warning"
        show-icon
        :title="writeBlockReason"
        class="status-alert"
      />

      <el-empty v-if="!team && !loading" description="暂无队伍或无访问权限" />

      <el-descriptions v-else-if="team" border :column="1">
        <el-descriptions-item label="名称">{{ team.name ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="竞赛">{{ team.competition?.name ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ team.status ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="最大人数">{{ team.maxMembers ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="当前人数">{{ currentCount }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">
          {{ team.leader?.realName || team.leader?.username || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="队伍所需技能">
          <span v-if="team.teamSkills && team.teamSkills.length">
            <el-tag v-for="skill in team.teamSkills" :key="skill.skillId" style="margin-right: 6px;">
              {{ skill.skillName || "-" }}
            </el-tag>
          </span>
          <span v-else>暂无</span>
        </el-descriptions-item>
        <el-descriptions-item label="获奖情况">
          <span v-if="awardLoading">加载中...</span>
          <span v-else-if="awardError">{{ awardError }}</span>
          <span v-else-if="awardSummary?.hasAward">
            {{ awardSummary.awardName || "-" }}
            <span v-if="awardSummary.publishedAt"> ({{ toYmd(awardSummary.publishedAt) }})</span>
          </span>
          <span v-else>暂无奖项</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ team.createdAt ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="队伍说明">{{ team.description || "无" }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="team" class="action-panel">
        <el-divider />
        <div class="action-panel__row">
          <el-button v-if="canShowClose" type="warning" :disabled="!canClose" @click="closeDialogVisible = true">
            {{ recruitingToggleLabel }}
          </el-button>
          <el-button
            v-if="isAdmin"
            type="danger"
            :disabled="isTeamDisbanded"
            @click="disbandDialogVisible = true"
          >
            解散队伍
          </el-button>
          <span v-if="canShowClose && !canClose" class="action-hint">{{ closeDisabledReason }}</span>
          <span v-if="isAdmin && isTeamDisbanded" class="action-hint">队伍已解散</span>
          <div v-if="teamId" class="card-actions">
            <el-button @click="router.push(`/teams/${teamId}/posts`)">讨论区</el-button>
            <el-button @click="router.push(`/teams/${teamId}/submissions`)">文件提交</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="closeDialogVisible" :title="recruitingToggleLabel" width="420px">
      <div>确认切换当前队伍的招募状态吗？</div>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitToggleRecruiting">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="disbandDialogVisible"
      title="解散队伍"
      width="420px"
      append-to-body
      top="12vh"
      :close-on-click-modal="false"
    >
      <div>确认解散该队伍吗？解散后不可报名、审核、提交与讨论。</div>
      <template #footer>
        <el-button :disabled="disbandLoading" @click="disbandDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="disbandLoading" @click="submitDisbandTeam">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="errorDialogVisible" title="提示" width="420px">
      <div>{{ errorDialogMessage }}</div>
      <template #footer>
        <el-button v-if="redirectAfterError" @click="router.push(redirectAfterError)">返回</el-button>
        <el-button type="primary" @click="onCloseErrorDialog">确定</el-button>
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

.page-subtitle {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.page-actions {
  display: inline-flex;
  gap: 8px;
}

.status-alert {
  margin-bottom: 12px;
}

.action-panel {
  margin-top: 12px;
}

.action-panel__row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.card-actions {
  margin-left: auto;
  display: inline-flex;
  gap: 8px;
}

.action-hint {
  color: #6b7280;
  font-size: 12px;
}
</style>
