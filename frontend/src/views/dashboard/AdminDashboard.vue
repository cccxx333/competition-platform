<script lang="ts" setup>
import { Document, Trophy } from "@element-plus/icons-vue"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { listCompetitions } from "@/api/competitions"
import { adminListTeacherApplicationPage } from "@/api/teacherApplications"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)
const competitionCount = ref(0)
const pendingTeacherCount = ref(0)
const username = computed(() => authStore.user?.username?.trim() || "管理员")

const loadCounts = async () => {
  loading.value = true
  try {
    const [competitionsResult, pendingResult] = await Promise.allSettled([
      listCompetitions({ page: 0, size: 1 }),
      adminListTeacherApplicationPage({ status: "PENDING", page: 0, size: 1 })
    ])

    if (competitionsResult.status === "fulfilled") {
      const total = competitionsResult.value?.total
      competitionCount.value = typeof total === "number" ? total : competitionsResult.value?.items?.length ?? 0
    } else {
      competitionCount.value = 0
    }

    if (pendingResult.status === "fulfilled") {
      const total = pendingResult.value?.total
      pendingTeacherCount.value = typeof total === "number" ? total : pendingResult.value?.items?.length ?? 0
    } else {
      pendingTeacherCount.value = 0
    }
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
            <el-icon :size="20"><Trophy /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ competitionCount }}</div>
            <div class="stat-label">竞赛总数</div>
          </div>
        </div>
      </template>
      <template #topRight>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingTeacherCount }}</div>
            <div class="stat-label">待审核建队申请</div>
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

