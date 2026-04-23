<script lang="ts" setup>
import { Trophy, Medal } from "@element-plus/icons-vue"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import QuickLinksPanel from "@/components/Dashboard/QuickLinksPanel.vue"
import { getMyHonors } from "@/api/honors"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)
const participationCount = ref(0)
const awardCount = ref(0)
const username = computed(() => authStore.user?.username?.trim() || "同学")
const quickLinks = [
  { label: "竞赛列表", path: "/competitions" },
  { label: "竞赛报名", path: "/competitions/apply" },
  { label: "我的申请", path: "/teams/my-applications" },
  { label: "荣誉", path: "/me/honors" }
]

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
  <div>
    <div class="page-header">
      <h2>欢迎回来，{{ username }}！</h2>
    </div>
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
        <div class="dashboard-bottom-stack">
          <QuickLinksPanel :items="quickLinks" />
          <OngoingCompetitionsPanel />
        </div>
      </template>
    </DashboardLayout>
  </div>
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

.page-header h2 {
  font-size: 35px; /* 你想要的大小 */
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

.dashboard-bottom-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>
