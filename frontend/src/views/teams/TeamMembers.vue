<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getTeamDetail, listTeamMembers, removeMember, type TeamDto, type TeamMemberView } from "@/api/teams"
import { useAuthStore } from "@/stores/auth"
import { getTeamWriteBlockReason, isDisbanded } from "@/utils/teamGuards"
import { getApiErrorMessage } from "@/utils/errorMessage"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const team = ref<TeamDto | null>(null)
const members = ref<TeamMemberView[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const currentMember = ref<TeamMemberView | null>(null)
const removeReason = ref("")
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

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "参数错误"
  if (status === 401) return "登录已过期，请重新登录"
  if (status === 403) {
    return roleUpper.value === "TEACHER" ? "仅队长可查看/管理队伍成员" : "无权访问"
  }
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

const formatDateTime = (value?: string | null) => {
  if (!value) return "-"
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const loadMembers = async () => {
  if (!teamId.value) {
    errorDialogMessage.value = "缺少 teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    team.value = await getTeamDetail(teamId.value)
    members.value = await listTeamMembers(teamId.value)
  } catch (error: any) {
    members.value = []
    showRequestError(error, "Failed to load team members")
  } finally {
    loading.value = false
  }
}

const canShowRemove = computed(() => roleUpper.value === "ADMIN" || roleUpper.value === "TEACHER")

const isSelf = (member: TeamMemberView) => {
  const currentUserId = authStore.user?.id
  return Boolean(currentUserId && member.userId === currentUserId)
}

const canRemove = (member: TeamMemberView) => {
  if (!team.value) return false
  if (isTeamDisbanded.value) return false
  if (roleUpper.value === "ADMIN") return !isSelf(member)
  if (roleUpper.value === "TEACHER") {
    if (!isLeader.value) return false
    if (team.value.status === "CLOSED") return false
    return !isSelf(member)
  }
  return false
}

const removeDisabledReason = (member: TeamMemberView) => {
  if (isTeamDisbanded.value) return "队伍已解散"
  if (isSelf(member)) return "不可移除自己"
  if (roleUpper.value === "TEACHER" && !isLeader.value) return "仅队长可移除"
  if (team.value?.status === "CLOSED" && roleUpper.value === "TEACHER") return "关闭后仅管理员可移除"
  return "无权限"
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

const openRemoveDialog = (member: TeamMemberView) => {
  currentMember.value = member
  removeReason.value = ""
  dialogVisible.value = true
}

const submitRemove = async () => {
  if (!teamId.value || !currentMember.value?.userId) return
  dialogLoading.value = true
  try {
    await removeMember(teamId.value, currentMember.value.userId, removeReason.value)
    ElMessage.success("已移除成员")
    dialogVisible.value = false
    await loadMembers()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to remove member")
    }
  } finally {
    dialogLoading.value = false
  }
}

onMounted(loadMembers)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>成员列表</h2>
        <div class="page-subtitle">Team ID: {{ teamId ?? "-" }}</div>
      </div>
      <div class="page-actions">
        <el-button @click="router.push(returnPath)">{{ returnLabel }}</el-button>
      </div>
    </div>

    <el-alert
      v-if="writeBlockReason"
      type="warning"
      show-icon
      :title="writeBlockReason"
      class="status-alert"
    />

    <el-table v-if="members.length" :data="members" style="width: 100%">
      <el-table-column prop="userId" label="User ID" width="120" />
      <el-table-column prop="username" label="Username" width="160" />
      <el-table-column prop="realName" label="Real Name" width="160" />
      <el-table-column prop="role" label="Role" width="120" />
      <el-table-column label="Joined At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.joinedAt) }}
        </template>
      </el-table-column>
      <el-table-column v-if="canShowRemove" label="Actions" width="180">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            :disabled="!canRemove(row)"
            :title="!canRemove(row) ? removeDisabledReason(row) : ''"
            @click="openRemoveDialog(row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!loading" description="暂无成员或无权限查看" />
  </el-card>

  <el-dialog v-model="dialogVisible" title="移除成员" width="420px">
    <div class="dialog-body">
      <div>确认移除该成员？</div>
      <el-input v-model="removeReason" type="textarea" :rows="3" placeholder="移除原因（可选）" />
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="dialogLoading" @click="submitRemove">确认</el-button>
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

.dialog-body {
  display: grid;
  gap: 12px;
}
</style>
