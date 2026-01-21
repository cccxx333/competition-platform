<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getMyTeam, type TeamDto } from "@/api/teams"
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const team = ref<TeamDto | null>(null)
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isStudent = computed(() => roleUpper.value === "STUDENT")

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
  if (status === 401) {
    ElMessage.error("登录已过期，请重新登录")
    return "登录已过期"
  }
  if (status === 404) {
    ElMessage.error("资源不存在")
    return "资源不存在"
  }
  if (status === 400) {
    ElMessage.error("参数错误")
    return "参数错误"
  }
  ElMessage.error("服务异常，请稍后重试")
  return fallback
}

const loadTeam = async () => {
  if (!isStudent.value) {
    team.value = null
    return
  }
  loading.value = true
  try {
    team.value = await getMyTeam()
  } catch (error: any) {
    team.value = null
    showRequestError(error, "加载队伍失败")
  } finally {
    loading.value = false
  }
}

onMounted(loadTeam)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>我的队伍</h2>
      <el-button v-if="isStudent" @click="router.push('/teams/my-applications')">返回我的申请</el-button>
    </div>

    <el-alert
      v-if="!isStudent"
      type="warning"
      show-icon
      title="此页面仅学生可用，请使用“队伍 → 队伍查询”"
      class="role-alert"
    />

    <el-empty v-if="isStudent && !team && !loading" description="暂无队伍（可能尚未通过审核或未加入任何队伍）" />

    <el-descriptions v-else-if="isStudent && team" border :column="1">
      <el-descriptions-item label="队伍 ID">{{ team.id ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="名称">{{ team.name ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ team.status ?? "-" }}</el-descriptions-item>
      <el-descriptions-item label="成员">
        {{ team.currentMembers ?? "-" }} / {{ team.maxMembers ?? "-" }}
      </el-descriptions-item>
      <el-descriptions-item v-if="team.description" label="说明">
        {{ team.description }}
      </el-descriptions-item>
    </el-descriptions>

    <div v-if="isStudent && team?.id" class="action-row">
      <el-button type="primary" @click="router.push(`/teams/${team.id}`)">查看队伍详情</el-button>
      <el-button @click="router.push(`/teams/${team.id}/members`)">查看成员</el-button>
    </div>

    <div v-if="!isStudent" class="action-row">
      <el-button type="primary" @click="router.push('/teams/lookup')">前往队伍查询</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-alert {
  margin-bottom: 12px;
}

.action-row {
  margin-top: 12px;
  display: inline-flex;
  gap: 8px;
}
</style>
