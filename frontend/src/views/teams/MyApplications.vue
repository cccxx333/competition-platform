<script lang="ts" setup>
import { ElMessage } from "element-plus"
import StatusTag from "@@/components/StatusTag/index.vue"
import { listMyApplications, type ApplicationItem } from "@/api/teamApplications"

const loading = ref(false)
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
  try {
    items.value = await listMyApplications()
  } catch (error: any) {
    items.value = []
    showRequestError(error, "加载申请失败")
  } finally {
    loading.value = false
  }
}

onMounted(loadApplications)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>我的申请</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table v-if="items.length" :data="items" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="competitionId" label="竞赛 ID" width="150" />
      <el-table-column prop="teamId" label="队伍 ID" width="120" />
      <el-table-column label="状态" width="160">
        <template #default="{ row }">
          <StatusTag :status="row.status" kind="teamApplication" />
        </template>
      </el-table-column>
      <el-table-column label="原因">
        <template #default="{ row }">
          <span v-if="row.status === 'REJECTED'" class="reason-text">
            {{ row.reason || "未提供原因" }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="items.some((item) => item.appliedAt)" label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.appliedAt) || "-" }}
        </template>
      </el-table-column>
    </el-table>

      <el-empty v-else-if="!loading" description="暂无申请" />
    </el-card>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reason-text {
  color: #dc2626;
}
</style>
