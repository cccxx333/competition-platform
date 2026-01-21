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
    ElMessage.error("无权限")
    return "无权限"
  }
  if (status === 404) {
    ElMessage.error("竞赛不存在")
    return "竞赛不存在"
  }
  if (status === 409) {
    ElMessage.error("申请已存在")
    return "申请已存在"
  }
  ElMessage.error("请求失败，请稍后重试")
  return "请求失败，请稍后重试"
}

const handleSubmit = async () => {
  errorMessage.value = ""
  if (!Number.isFinite(competitionId.value) || competitionId.value <= 0) {
    errorMessage.value = "竞赛不存在"
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
    ElMessage.success("申请已提交")
    router.push("/teacher/applications")
  } catch (error: any) {
    errorMessage.value = showRequestError(error, "提交教师申请失败")
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
      <h2>申请成为教师</h2>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-form label-width="120px">
      <el-form-item label="队伍名称">
        <el-input v-model="form.teamName" placeholder="可选队伍名称" />
      </el-form-item>
      <el-form-item label="说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="可选说明"
        />
      </el-form-item>
    </el-form>

    <div class="page-actions">
      <el-button :loading="loading" type="primary" @click="handleSubmit">提交</el-button>
      <el-button :disabled="loading" @click="handleCancel">取消</el-button>
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
