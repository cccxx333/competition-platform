<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem } from "@/api/competitions"
import StatusTag from "@/common/components/StatusTag/index.vue"

const loading = ref(false)
const items = ref<CompetitionListItem[]>([])

const formatDate = (value?: string) => {
  if (!value) return "-"
  return value.includes("T") ? value.split("T")[0] : value
}

const buildMeta = (item: CompetitionListItem) => {
  const parts = [
    item.category,
    item.level,
    item.registrationDeadline ? formatDate(item.registrationDeadline) : undefined
  ].filter(Boolean)
  return parts.join(" / ")
}

const loadData = async () => {
  loading.value = true
  try {
    const { items: list } = await listCompetitions({ status: "ONGOING", page: 0, size: 3 })
    items.value = (list ?? []).slice(0, 3)
  } catch (error: any) {
    items.value = []
    ElMessage.error("获取进行中竞赛失败")
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="ongoing-panel">
    <div class="panel-title">进行中的竞赛</div>
    <div v-if="loading" class="panel-empty">加载中...</div>
    <div v-else-if="items.length === 0" class="panel-empty">暂无进行中的竞赛</div>
    <div v-else class="panel-list">
      <div v-for="item in items" :key="item.id ?? item.name" class="panel-item">
        <div class="panel-main">
          <div class="panel-name">{{ item.name ?? "-" }}</div>
          <div class="panel-meta">{{ buildMeta(item) || "-" }}</div>
        </div>
        <StatusTag :status="item.status" kind="competition" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.ongoing-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.panel-empty {
  color: #6b7280;
  font-size: 14px;
}

.panel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.panel-item:last-child {
  border-bottom: 0;
}

.panel-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.panel-name {
  font-weight: 600;
  color: #111827;
}

.panel-meta {
  font-size: 13px;
  color: #6b7280;
}
</style>
