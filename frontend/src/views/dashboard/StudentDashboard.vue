<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { Clock, Medal, Promotion, Trophy } from "@element-plus/icons-vue"
import { listCompetitions } from "@/api/competitions"
import { getStudentDashboardStats } from "@/api/dashboard"
import { getMyHonors } from "@/api/honors"
import { listMyApplications } from "@/api/teamApplications"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)

const pendingTeacherReviewCount = ref(0)
const ongoingCompetitionCount = ref(0)
const participationCount = ref(0)
const awardCount = ref(0)

const username = computed(() => authStore.user?.username?.trim() || "同学")

const fetchCompetitionIdsByStatus = async (status: "UPCOMING" | "ONGOING" | "FINISHED") => {
  const pageSize = 100
  let page = 0
  let totalPages = 1
  const ids = new Set<number>()

  while (page < totalPages) {
    const { items, total } = await listCompetitions({ status, page, size: pageSize })
    items.forEach((item) => {
      if (typeof item.id === "number") ids.add(item.id)
    })
    totalPages = typeof total === "number" ? Math.max(1, Math.ceil(total / pageSize)) : page + 1
    page += 1
  }

  return ids
}

const loadCountsFallback = async () => {
  const [honorsResult, applicationsResult, ongoingIdsResult] = await Promise.allSettled([
    getMyHonors(),
    listMyApplications(),
    fetchCompetitionIdsByStatus("ONGOING")
  ])

  if (honorsResult.status === "fulfilled") {
    participationCount.value = Number(honorsResult.value?.participationCount ?? 0) || 0
    awardCount.value = Number(honorsResult.value?.awardCount ?? 0) || 0
  } else {
    participationCount.value = 0
    awardCount.value = 0
  }

  if (applicationsResult.status === "fulfilled") {
    const list = Array.isArray(applicationsResult.value) ? applicationsResult.value : []
    pendingTeacherReviewCount.value = list.filter((item) => item.status === "PENDING" && item.isActive !== false).length

    if (ongoingIdsResult.status === "fulfilled") {
      const approvedCompetitionIds = new Set<number>()
      list.forEach((item) => {
        if (item.status === "APPROVED" && item.isActive !== false && typeof item.competitionId === "number") {
          approvedCompetitionIds.add(item.competitionId)
        }
      })
      ongoingCompetitionCount.value = Array.from(approvedCompetitionIds).filter((id) => ongoingIdsResult.value.has(id)).length
    } else {
      ongoingCompetitionCount.value = 0
    }
  } else {
    pendingTeacherReviewCount.value = 0
    ongoingCompetitionCount.value = 0
  }
}

const loadCounts = async () => {
  loading.value = true
  try {
    const data = await getStudentDashboardStats()
    pendingTeacherReviewCount.value = Number(data?.pendingTeacherReviewCount ?? 0) || 0
    ongoingCompetitionCount.value = Number(data?.ongoingCompetitionCount ?? 0) || 0
    participationCount.value = Number(data?.participationCount ?? 0) || 0
    awardCount.value = Number(data?.awardCount ?? 0) || 0

    const allZero =
      pendingTeacherReviewCount.value === 0 &&
      ongoingCompetitionCount.value === 0 &&
      participationCount.value === 0 &&
      awardCount.value === 0
    if (allZero) {
      await loadCountsFallback()
    }
  } catch (error) {
    try {
      await loadCountsFallback()
    } finally {
      ElMessage.warning("学生概览统计接口不可用，已切换为降级统计")
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
            <el-icon :size="20"><Clock /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingTeacherReviewCount }}</div>
            <div class="stat-label">待教师审核申请</div>
          </div>
        </div>
      </template>

      <template #topMidLeft>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Promotion /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ ongoingCompetitionCount }}</div>
            <div class="stat-label">正在进行比赛</div>
          </div>
        </div>
      </template>

      <template #topCenter>
        <div class="stat-card" v-loading="loading">
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
        <div class="stat-card" v-loading="loading">
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
