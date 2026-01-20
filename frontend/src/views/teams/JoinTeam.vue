<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { createApplication } from "@/api/teamApplications"

const router = useRouter()
const loading = ref(false)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const form = reactive({
  competitionId: null as number | null,
  teamId: null as number | null
})

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  const showUiError = (value: string) => {
    errorDialogMessage.value = value
    errorDialogVisible.value = true
  }
  if (message && message !== fallback) {
    showUiError(message)
    return message
  }
  if (status === 403) {
    showUiError("无权限")
    return "无权限"
  }
  if (status === 404) {
    showUiError("资源不存在")
    return "资源不存在"
  }
  if (status === 409) {
    showUiError("业务冲突")
    return "业务冲突"
  }
  if (status === 400) {
    showUiError("参数错误")
    return "参数错误"
  }
  showUiError("服务异常，请稍后重试")
  return fallback
}

const submit = async () => {
  if (!form.competitionId || !form.teamId) {
    errorDialogMessage.value = "请填写 competitionId 与 teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    await createApplication({
      competitionId: form.competitionId,
      teamId: form.teamId
    })
    ElMessage.success("已提交申请")
    router.push("/teams/my-applications")
  } catch (error: any) {
    showRequestError(error, "Request failed")
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Join Team</h2>
    </div>

    <el-form label-width="140px" class="join-form">
      <el-form-item label="Competition ID">
        <el-input-number v-model="form.competitionId" :min="1" :controls="false" style="width: 240px" />
      </el-form-item>
      <el-form-item label="Team ID">
        <el-input-number v-model="form.teamId" :min="1" :controls="false" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">Submit</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="操作失败" :close-on-click-modal="true" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="errorDialogVisible = false">确定</el-button>
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
