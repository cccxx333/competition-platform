<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { createApplication } from "@/api/teamApplications"
import { getTeamDetail, type TeamDto } from "@/api/teams"
import { isDisbanded } from "@/utils/teamGuards"
import { getApiErrorMessage } from "@/utils/errorMessage"

const router = useRouter()
const loading = ref(false)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)
const form = reactive({
  competitionId: null as number | null,
  teamId: null as number | null
})
const teamInfo = ref<TeamDto | null>(null)
const isTeamDisbanded = computed(() => isDisbanded(teamInfo.value))

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

const submit = async () => {
  if (!form.competitionId || !form.teamId) {
    errorDialogMessage.value = "请填写竞赛 ID 与队伍 ID"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    try {
      teamInfo.value = await getTeamDetail(form.teamId)
      if (isTeamDisbanded.value) {
        errorDialogMessage.value = "队伍已解散，操作已禁止"
        redirectAfterError.value = "/teams/my"
        errorDialogVisible.value = true
        return
      }
    } catch (error: any) {
      showRequestError(error, "加载队伍信息失败")
      return
    }
    await createApplication({
      competitionId: form.competitionId,
      teamId: form.teamId
    })
    ElMessage.success("已提交申请")
    router.push("/teams/my-applications")
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      errorDialogMessage.value = "队伍已解散，操作已禁止"
      redirectAfterError.value = "/teams/my"
      errorDialogVisible.value = true
    } else {
      showRequestError(error, "请求失败")
    }
  } finally {
    loading.value = false
  }
}

const onCloseErrorDialog = () => {
  errorDialogVisible.value = false
  redirectAfterError.value = null
}
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>加入队伍</h2>
    </div>

    <el-form label-width="140px" class="join-form">
      <el-form-item label="竞赛 ID">
        <el-input-number v-model="form.competitionId" :min="1" :controls="false" style="width: 240px" />
      </el-form-item>
      <el-form-item label="队伍 ID">
        <el-input-number v-model="form.teamId" :min="1" :controls="false" style="width: 240px" />
      </el-form-item>
      <el-form-item v-if="teamInfo?.status">
        <el-tag :type="isTeamDisbanded ? 'danger' : 'info'">队伍状态：{{ teamInfo.status }}</el-tag>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" :disabled="isTeamDisbanded" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="操作失败" :close-on-click-modal="true" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button v-if="redirectAfterError" @click="router.push(redirectAfterError)">返回队伍页</el-button>
      <el-button type="primary" @click="onCloseErrorDialog">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.join-form {
  max-width: 520px;
}
</style>
