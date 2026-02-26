<script lang="ts" setup>
import { Document, User } from "@element-plus/icons-vue"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { listPendingApplications } from "@/api/teamApplications"
import { listMyTeacherApplicationPage } from "@/api/teacherApplications"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)
const appliedCompetitionCount = ref(0)
const pendingTeamApplicationCount = ref(0)
const username = computed(() => authStore.user?.username?.trim() || "老师")

const loadCounts = async () => {
  loading.value = true
  try {
    const [applicationsResult, pendingResult] = await Promise.allSettled([
      listMyTeacherApplicationPage({ page: 0, size: 1 }),
      listPendingApplications({ status: "PENDING" })
    ])
    if (applicationsResult.status === "fulfilled") {
      const total = applicationsResult.value?.total
      appliedCompetitionCount.value =
        typeof total === "number" ? total : applicationsResult.value?.items?.length ?? 0
    } else {
      appliedCompetitionCount.value = 0
    }
    pendingTeamApplicationCount.value =
      pendingResult.status === "fulfilled" ? pendingResult.value?.length ?? 0 : 0
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
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ appliedCompetitionCount }}</div>
            <div class="stat-label">已申请比赛</div>
          </div>
        </div>
      </template>
      <template #topRight>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><User /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingTeamApplicationCount }}</div>
            <div class="stat-label">入队待审核</div>
          </div>
        </div>
      </template>
      <template #bottom>
        <OngoingCompetitionsPanel />
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
  font-size: 35px;
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
