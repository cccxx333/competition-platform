<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { closeTeam, getTeamDetail, listTeamMembers, type TeamDto, type TeamMemberView } from "@/api/teams"
import { useAuthStore } from "@/stores/auth"
import { canCloseRecruiting, getTeamWriteBlockReason, isDisbanded } from "@/utils/teamGuards"
import { getApiErrorMessage } from "@/utils/errorMessage"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const team = ref<TeamDto | null>(null)
const members = ref<TeamMemberView[]>([])
const membersLoaded = ref(false)
const loading = ref(false)
const actionLoading = ref(false)
const closeDialogVisible = ref(false)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)

const teamId = computed(() => {
  const raw = Number(route.params.teamId)
  return Number.isNaN(raw) ? null : raw
})

const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
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

const canShowClose = computed(() => roleUpper.value === "ADMIN" || roleUpper.value === "TEACHER")
const canClose = computed(() => canCloseRecruiting(team.value, roleUpper.value, isLeader.value))

const closeDisabledReason = computed(() => {
  if (!team.value) return "队伍不存在"
  if (isTeamDisbanded.value) return "队伍已解散"
  if (team.value.status !== "RECRUITING") return "仅招募中可关闭"
  if (roleUpper.value === "TEACHER" && !isLeader.value) return "仅队长可关闭"
  if (roleUpper.value !== "ADMIN" && roleUpper.value !== "TEACHER") return "无权限"
  return ""
})

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "参数错误"
  if (status === 401) return "登录已过期，请重新登录"
  if (status === 403) return "无权访问"
  if (status === 404) return "队伍不存在或已删除"
  if (status === 409) return "操作冲突，请稍后重试"
  return "服务异常，请稍后重试"
}

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const fallbackMessage = status ? getFallbackMessage(status) : fallback
  const message = getApiErrorMessage(error, fallbackMessage)
  const showUiError = (value: string) => {
    errorDialogMessage.value = value
    errorDialogVisible.value = true
    redirectAfterError.value = null
  }
  showUiError(message)
  return message
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
    membersLoaded.value = false
    try {
      members.value = await listTeamMembers(teamId.value)
      membersLoaded.value = true
    } catch (error: any) {
      const status = error?.status ?? error?.response?.status
      if (status !== 403) {
        showRequestError(error, "Failed to load team members")
      }
    }
  } catch (error: any) {
    team.value = null
    showRequestError(error, "Failed to load team detail")
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

const submitCloseTeam = async () => {
  if (!teamId.value) return
  actionLoading.value = true
  try {
    await closeTeam(teamId.value)
    ElMessage.success("已停止招人")
    closeDialogVisible.value = false
    await loadTeam()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to close team")
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadTeam)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>队伍详情</h2>
        <div class="page-subtitle">Team ID: {{ teamId ?? "-" }}</div>
      </div>
      <div class="page-actions">
        <el-button @click="router.push(returnPath)">{{ returnLabel }}</el-button>
        <el-button type="primary" :disabled="!teamId" @click="router.push(`/teams/${teamId}/members`)">
          成员管理
        </el-button>
      </div>
    </div>

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
      <el-descriptions-item label="状态">{{ team.status ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="最大人数">{{ team.maxMembers ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="当前人数">{{ currentCount }}</el-descriptions-item>
      <el-descriptions-item label="队长">
        {{ team.leader?.realName || team.leader?.username || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ team.createdAt ?? "-" }}</el-descriptions-item>
      <el-descriptions-item v-if="team.description" label="说明">{{ team.description }}</el-descriptions-item>
    </el-descriptions>

    <div v-if="team" class="action-panel">
      <el-divider />
      <div class="action-panel__row">
        <el-button v-if="canShowClose" type="warning" :disabled="!canClose" @click="closeDialogVisible = true">
          停止招人
        </el-button>
        <span v-if="canShowClose && !canClose" class="action-hint">{{ closeDisabledReason }}</span>
      </div>
    </div>
  </el-card>

  <el-dialog v-model="closeDialogVisible" title="停止招人" width="420px">
    <div>确认将队伍状态变更为 CLOSED？</div>
    <template #footer>
      <el-button @click="closeDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="actionLoading" @click="submitCloseTeam">确认</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="errorDialogVisible" title="提示" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button v-if="redirectAfterError" @click="router.push(redirectAfterError)">返回</el-button>
      <el-button type="primary" @click="onCloseErrorDialog">确定</el-button>
    </template>
  </el-dialog>
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
  display: inline-flex;
  gap: 12px;
  align-items: center;
}

.action-hint {
  color: #6b7280;
  font-size: 12px;
}
</style>
