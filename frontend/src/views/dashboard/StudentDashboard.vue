<script lang="ts" setup>
import { Trophy, Medal } from "@element-plus/icons-vue"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { getMyHonors } from "@/api/honors"

const loading = ref(false)
const participationCount = ref(0)
const awardCount = ref(0)

const loadCounts = async () => {
  loading.value = true
  try {
    const data = await getMyHonors()
    participationCount.value = data?.participationCount ?? 0
    awardCount.value = data?.awardCount ?? 0
  } finally {
    loading.value = false
  }
}

onMounted(loadCounts)
</script>

<template>
  <DashboardLayout>
    <template #topLeft>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="20"><Trophy /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ participationCount }}</div>
          <div class="stat-label">参赛次数</div>
        </div>
      </div>
    </template>
    <template #topRight>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="20"><Medal /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ awardCount }}</div>
          <div class="stat-label">获奖次数</div>
        </div>
      </div>
    </template>
    <template #bottom>
      <OngoingCompetitionsPanel />
    </template>
  </DashboardLayout>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 30px;
  width: 100%;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.04);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.1;
  color: #111827;
}

.stat-label {
  font-size: 15px;
  color: #6b7280;
  margin-top: 8px;
}
</style>
