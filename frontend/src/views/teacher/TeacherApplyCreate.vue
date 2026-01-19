<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { createTeacherApplication, type TeacherApplicationCreatePayload } from "@/api/teacherApplications"

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref("")
const form = reactive<TeacherApplicationCreatePayload>({
  teamName: "",
  description: ""
})

const competitionId = computed(() => Number(route.params.competitionId))

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
  }
  if (status === 403) {
    ElMessage.error("No permission")
    return "No permission"
  }
  if (status === 404) {
    ElMessage.error("Competition not found")
    return "Competition not found"
  }
  if (status === 409) {
    ElMessage.error("Application already exists")
    return "Application already exists"
  }
  ElMessage.error("Request failed, please try again")
  return "Request failed, please try again"
}

const handleSubmit = async () => {
  errorMessage.value = ""
  if (!Number.isFinite(competitionId.value) || competitionId.value <= 0) {
    errorMessage.value = "Invalid competition"
    return
  }
  loading.value = true
  try {
    const payload: TeacherApplicationCreatePayload = {}
    if (form.teamName && form.teamName.trim()) {
      payload.teamName = form.teamName.trim()
    }
    if (form.description && form.description.trim()) {
      payload.description = form.description.trim()
    }
    await createTeacherApplication(competitionId.value, payload)
    ElMessage.success("Application submitted")
    router.push("/teacher/applications")
  } catch (error: any) {
    errorMessage.value = showRequestError(error, "Failed to submit teacher application")
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Apply as Teacher</h2>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-form label-width="120px">
      <el-form-item label="Team Name">
        <el-input v-model="form.teamName" placeholder="Optional team name" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="Optional description"
        />
      </el-form-item>
    </el-form>

    <div class="page-actions">
      <el-button :loading="loading" type="primary" @click="handleSubmit">Submit</el-button>
      <el-button :disabled="loading" @click="handleCancel">Cancel</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.page-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}
</style>
