<script lang="ts" setup>
import { Document, Trophy, User } from "@element-plus/icons-vue"
import { listCompetitions } from "@/api/competitions"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { listPendingApplications } from "@/api/teamApplications"
import { adminListTeacherApplicationPage, listMyTeacherApplicationPage } from "@/api/teacherApplications"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)
const appliedCompetitionCount = ref(0)
const managedCompetitionCount = ref(0)
const pendingBuildApplicationCount = ref(0)
const pendingJoinApplicationCount = ref(0)
const username = computed(() => authStore.user?.username?.trim() || "老师")

const countManagedCompetitions = async () => {
  const { total, items } = await listCompetitions({ managedOnly: true, page: 0, size: 1 })
  if (typeof total === "number") return total
  return items.length
}

const loadCounts = async () => {
  loading.value = true
  try {
    const [applicationsResult, managedCompetitionResult, pendingBuildResult, pendingJoinResult] = await Promise.allSettled([
      listMyTeacherApplicationPage({ page: 0, size: 1 }),
      countManagedCompetitions(),
      adminListTeacherApplicationPage({ status: "PENDING", page: 0, size: 1 }),
      listPendingApplications({ status: "PENDING" })
    ])
    if (applicationsResult.status === "fulfilled") {
      const total = applicationsResult.value?.total
      appliedCompetitionCount.value =
        typeof total === "number" ? total : applicationsResult.value?.items?.length ?? 0
    } else {
      appliedCompetitionCount.value = 0
    }

    managedCompetitionCount.value = managedCompetitionResult.status === "fulfilled" ? managedCompetitionResult.value : 0

    if (pendingBuildResult.status === "fulfilled") {
      const total = pendingBuildResult.value?.total
      pendingBuildApplicationCount.value =
        typeof total === "number" ? total : pendingBuildResult.value?.items?.length ?? 0
    } else {
      pendingBuildApplicationCount.value = 0
    }

    pendingJoinApplicationCount.value = pendingJoinResult.status === "fulfilled" ? pendingJoinResult.value?.length ?? 0 : 0
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
      <template #topMidLeft>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Trophy /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ managedCompetitionCount }}</div>
            <div class="stat-label">已负责竞赛</div>
          </div>
        </div>
      </template>
      <template #topCenter>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingBuildApplicationCount }}</div>
            <div class="stat-label">待审核建队申请</div>
          </div>
        </div>
      </template>
      <template #topRight>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><User /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingJoinApplicationCount }}</div>
            <div class="stat-label">待审核入队申请</div>
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
  gap: 20px;
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
