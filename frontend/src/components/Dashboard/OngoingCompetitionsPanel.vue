<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem } from "@/api/competitions"
import StatusTag from "@/common/components/StatusTag/index.vue"

const loading = ref(false)
const items = ref<CompetitionListItem[]>([])

const getSortTime = (item: CompetitionListItem) => {
  const value = item.startDate ?? item.createdAt ?? item.registrationDeadline ?? item.updatedAt ?? item.endDate
  if (!value) return 0
  const parsed = Date.parse(value)
  return Number.isNaN(parsed) ? 0 : parsed
}

const formatDate = (value?: string | null) => {
  if (!value) return ""
  return value.includes("T") ? value.split("T")[0] : value
}

const formatDateRange = (item: CompetitionListItem) => {
  const start = formatDate(item.startDate ?? null)
  const end = formatDate(item.endDate ?? null)
  if (!start && !end) return "-"
  return [start, end].filter(Boolean).join(" ~ ")
}

const loadData = async () => {
  loading.value = true
  try {
    const { items: list } = await listCompetitions({ page: 0, size: 50 })
    const sorted = (list ?? []).slice().sort((a, b) => getSortTime(b) - getSortTime(a))
    items.value = sorted.slice(0, 10)
  } catch (error: any) {
    items.value = []
    ElMessage.error("获取竞赛信息失败")
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="ongoing-panel">
    <div class="page-header">
      <h2>最近比赛</h2>
    </div>
    <el-card shadow="never" class="cp-card ongoing-card" v-loading="loading">
      <el-table v-if="items.length" :data="items" style="width: 100%">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="competition" />
          </template>
        </el-table-column>
        <el-table-column label="日期范围" min-width="200" class-name="date-range-column">
          <template #default="{ row }">
            {{ formatDateRange(row) }}
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="panel-empty">暂无竞赛信息</div>
    </el-card>
  </div>
</template>

<style scoped>
.ongoing-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-header {
  margin-bottom: 4px;
}

.page-header h2 {
  font-size: 25px;
}

.panel-empty {
  color: #6b7280;
  font-size: 14px;
  padding: 12px 0;
}

:deep(.date-range-column .cell) {
  padding-left: 18px;
}
</style>
