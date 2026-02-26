<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listMyTeams, type TeamDto } from "@/api/teams"
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const teams = ref<TeamDto[]>([])
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
    teams.value = []
    return
  }
  loading.value = true
  try {
    const response = await listMyTeams()
    teams.value = response.filter((item) => String(item.status ?? "").toUpperCase() !== "DISBANDED")
  } catch (error: any) {
    teams.value = []
    showRequestError(error, "加载队伍失败")
  } finally {
    loading.value = false
  }
}

onMounted(loadTeam)
</script>

<template>
  <div class="my-team-page" v-loading="loading">
    <div class="page-header">
      <h2>我的队伍</h2>
    </div>

    <el-alert
      v-if="!isStudent"
      type="warning"
      show-icon
      title="此页面仅学生可用，请使用“队伍 → 队伍查询”"
      class="role-alert"
    />

    <el-empty v-if="isStudent && !teams.length && !loading" description="暂无未解散队伍" />

    <div v-else-if="isStudent" class="my-team-list">
      <el-card v-for="item in teams" :key="item.id" class="cp-card my-team-card" shadow="never">
        <el-descriptions border :column="1">
          <el-descriptions-item label="队伍 ID">{{ item.id ?? "-" }}</el-descriptions-item>
          <el-descriptions-item label="竞赛">{{ item.competition?.name ?? "-" }}</el-descriptions-item>
          <el-descriptions-item label="名称">{{ item.name ?? "-" }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ item.status ?? "-" }}</el-descriptions-item>
          <el-descriptions-item label="成员">
            {{ item.currentMembers ?? "-" }} / {{ item.maxMembers ?? "-" }}
          </el-descriptions-item>
          <el-descriptions-item v-if="item.description" label="说明">
            {{ item.description }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="item.id" class="action-row">
          <el-button type="primary" @click="router.push(`/teams/${item.id}`)">查看队伍详情</el-button>
        </div>
      </el-card>
    </div>

    <div v-if="!isStudent" class="action-row">
      <el-button type="primary" @click="router.push('/teams/lookup')">前往队伍查询</el-button>
    </div>
  </div>
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

.my-team-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
