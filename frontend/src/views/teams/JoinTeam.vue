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
    showUiError("No permission")
    return "No permission"
  }
  if (status === 404) {
    showUiError("Resource not found")
    return "Resource not found"
  }
  if (status === 409) {
    showUiError("Business conflict")
    return "Business conflict"
  }
  if (status === 400) {
    showUiError("Invalid request")
    return "Invalid request"
  }
  showUiError("Service error, please try again")
  return fallback
}

const submit = async () => {
  if (!form.competitionId || !form.teamId) {
    errorDialogMessage.value = "Please fill competitionId and teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    await createApplication({
      competitionId: form.competitionId,
      teamId: form.teamId
    })
    ElMessage.success("Application submitted")
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

  <el-dialog v-model="errorDialogVisible" title="Error" :close-on-click-modal="true" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="errorDialogVisible = false">OK</el-button>
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
