<script lang="ts" setup>
import { listMyTeams, listTeams, type TeamDto } from "@/api/teams"
import { getApiErrorMessage } from "@/utils/errorMessage"
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const authStore = useAuthStore()

const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")
const isTeacher = computed(() => roleUpper.value === "TEACHER")
const isStudent = computed(() => roleUpper.value === "STUDENT")

const loading = ref(false)
const teams = ref<TeamDto[]>([])
const errorMessage = ref("")
const keyword = ref("")

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "请求参数错误"
  if (status === 401) return "登录状态失效，请重新登录"
  if (status === 403) return "无权限访问该资源"
  if (status === 404) return "未找到对应队伍"
  if (status === 409) return "请求冲突，请稍后重试"
  return "请求失败，请稍后再试"
}

const loadTeams = async () => {
  if (isStudent.value) {
    teams.value = []
    return
  }
  loading.value = true
  errorMessage.value = ""
  try {
    if (isAdmin.value) {
      const response = await listTeams({ keyword: keyword.value })
      teams.value = response.items
    } else if (isTeacher.value) {
      teams.value = await listMyTeams({ keyword: keyword.value })
    }
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    errorMessage.value = getApiErrorMessage(error, fallback)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  keyword.value = ""
  loadTeams()
}

const handleRowClick = (row: TeamDto) => {
  if (row.id) {
    router.push(`/teams/${row.id}`)
  }
}

onMounted(loadTeams)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>队伍查询</h2>
    </div>

    <el-alert
      v-if="isStudent"
      type="warning"
      show-icon
      title="当前页面仅管理员 / 教师可用，学生请前往「我的队伍」查看队伍信息"
      class="status-alert"
    />

    <el-alert v-else-if="errorMessage" type="error" show-icon :title="errorMessage" class="status-alert" />

    <el-form v-if="!isStudent" label-width="0" class="lookup-form">
      <el-form-item>
        <div class="lookup-row">
          <el-input
            v-model="keyword"
            placeholder="输入队伍 ID/名称/竞赛名称"
            style="max-width: 420px"
            @keyup.enter="loadTeams"
          />
          <div class="lookup-actions">
            <el-button type="primary" :loading="loading" @click="loadTeams">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <el-table v-if="teams.length && !isStudent" :data="teams" style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="id" label="队伍 ID" width="120" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="status" label="状态" width="140" />
      <el-table-column label="指导教师" min-width="160">
        <template #default="{ row }">
          {{ row.leader?.realName || row.leader?.username || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="竞赛" min-width="180">
        <template #default="{ row }">
          {{ row.competition?.name || "-" }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
    </el-table>

    <el-empty v-else-if="!loading && !isStudent" description="暂无符合条件的队伍" />
  </el-card>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.lookup-form {
  max-width: 720px;
}

.status-alert {
  margin-bottom: 12px;
}

.lookup-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lookup-actions {
  display: inline-flex;
  gap: 8px;
}
</style>
