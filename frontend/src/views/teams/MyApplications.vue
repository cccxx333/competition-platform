<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listMyApplications, type ApplicationItem } from "@/api/teamApplications"

const router = useRouter()
const loading = ref(false)
const errorMessage = ref("")
const items = ref<ApplicationItem[]>([])

const formatDateTime = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

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
  if (status === 409) {
    ElMessage.error("业务冲突")
    return "业务冲突"
  }
  if (status === 400) {
    ElMessage.error("参数错误")
    return "参数错误"
  }
  ElMessage.error("服务异常，请稍后重试")
  return fallback
}

const loadApplications = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    items.value = await listMyApplications()
  } catch (error: any) {
    items.value = []
    errorMessage.value = showRequestError(error, "Failed to load applications")
  } finally {
    loading.value = false
  }
}

const hasApproved = computed(() => items.value.some((item) => item.status === "APPROVED"))

onMounted(loadApplications)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>My Applications</h2>
      <div class="page-header__actions">
        <el-button :loading="loading" @click="loadApplications">Refresh</el-button>
        <el-button v-if="hasApproved" type="primary" @click="router.push('/teams/my')">查看我的队伍</el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-table v-if="items.length" :data="items" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="competitionId" label="Competition ID" width="150" />
      <el-table-column prop="teamId" label="Team ID" width="120" />
      <el-table-column prop="status" label="Status" width="140" />
      <el-table-column label="Reason">
        <template #default="{ row }">
          {{ row.reason || "-" }}
        </template>
      </el-table-column>
      <el-table-column v-if="items.some((item) => item.appliedAt)" label="Applied At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.appliedAt) || "-" }}
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!errorMessage && !loading" description="暂无申请" />
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-header__actions {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}
</style>
