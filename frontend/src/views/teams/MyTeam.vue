<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getMyTeam, type TeamDto } from "@/api/teamApplications"

const router = useRouter()
const loading = ref(false)
const errorMessage = ref("")
const team = ref<TeamDto | null>(null)

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
    ElMessage.error("资源不存在")
    return "资源不存在"
  }
  ElMessage.error("服务异常，请稍后重试")
  return fallback
}

const loadTeam = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    team.value = await getMyTeam()
  } catch (error: any) {
    team.value = null
    errorMessage.value = showRequestError(error, "Failed to load team")
  } finally {
    loading.value = false
  }
}

onMounted(loadTeam)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>My Team</h2>
      <el-button @click="router.push('/teams/my-applications')">Back to Applications</el-button>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-empty v-else-if="!team && !loading" description="暂无队伍（可能尚未通过审核）" />

    <el-descriptions v-else-if="team" border :column="1">
      <el-descriptions-item label="Team ID">{{ team.id ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="Name">{{ team.name ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="Status">{{ team.status ?? "-" }}</el-descriptions-item>
      <el-descriptions-item v-if="team.description" label="Description">
        {{ team.description }}
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
</style>
