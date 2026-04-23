<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { Clock, Document, Promotion, Trophy } from "@element-plus/icons-vue"
import { listAdminUsersPage } from "@/api/adminUsers"
import { listAwardRecords } from "@/api/awards"
import { listCompetitions } from "@/api/competitions"
import { getAdminDashboardStats } from "@/api/dashboard"
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const loading = ref(false)

const pendingTeacherUserCount = ref(0)
const finishedPendingAwardCompetitionCount = ref(0)
const ongoingCompetitionCount = ref(0)
const competitionTotalCount = ref(0)

const username = computed(() => authStore.user?.username?.trim() || "管理员")

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
  const [pendingTeacherResult, ongoingResult, totalResult, finishedIdsResult, awardRecordsResult] = await Promise.allSettled([
    listAdminUsersPage({ role: "TEACHER", approvalStatus: "PENDING", page: 0, size: 1 }),
    listCompetitions({ status: "ONGOING", page: 0, size: 1 }),
    listCompetitions({ page: 0, size: 1 }),
    fetchCompetitionIdsByStatus("FINISHED"),
    listAwardRecords({ size: 200 })
  ])

  if (pendingTeacherResult.status === "fulfilled") {
    const total = pendingTeacherResult.value?.total
    pendingTeacherUserCount.value = typeof total === "number" ? total : pendingTeacherResult.value?.items?.length ?? 0
  } else {
    pendingTeacherUserCount.value = 0
  }

  if (ongoingResult.status === "fulfilled") {
    const total = ongoingResult.value?.total
    ongoingCompetitionCount.value = typeof total === "number" ? total : ongoingResult.value?.items?.length ?? 0
  } else {
    ongoingCompetitionCount.value = 0
  }

  if (totalResult.status === "fulfilled") {
    const total = totalResult.value?.total
    competitionTotalCount.value = typeof total === "number" ? total : totalResult.value?.items?.length ?? 0
  } else {
    competitionTotalCount.value = 0
  }

  if (finishedIdsResult.status === "fulfilled" && awardRecordsResult.status === "fulfilled") {
    const records = Array.isArray(awardRecordsResult.value) ? awardRecordsResult.value : []
    const awardedCompetitionIds = new Set<number>()
    records.forEach((record) => {
      if (typeof record.competitionId === "number") {
        awardedCompetitionIds.add(record.competitionId)
      }
    })
    finishedPendingAwardCompetitionCount.value = Array.from(finishedIdsResult.value).filter(
      (id) => !awardedCompetitionIds.has(id)
    ).length
  } else {
    finishedPendingAwardCompetitionCount.value = 0
  }
}

const loadCounts = async () => {
  loading.value = true
  try {
    const data = await getAdminDashboardStats()
    pendingTeacherUserCount.value = Number(data?.pendingTeacherUserCount ?? 0) || 0
    finishedPendingAwardCompetitionCount.value = Number(data?.finishedPendingAwardCompetitionCount ?? 0) || 0
    ongoingCompetitionCount.value = Number(data?.ongoingCompetitionCount ?? 0) || 0
    competitionTotalCount.value = Number(data?.competitionTotalCount ?? 0) || 0

    const allZero =
      pendingTeacherUserCount.value === 0 &&
      finishedPendingAwardCompetitionCount.value === 0 &&
      ongoingCompetitionCount.value === 0 &&
      competitionTotalCount.value === 0
    if (allZero) {
      await loadCountsFallback()
    }
  } catch (error) {
    try {
      await loadCountsFallback()
    } finally {
      ElMessage.warning("管理员概览统计接口不可用，已切换为降级统计")
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
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ pendingTeacherUserCount }}</div>
            <div class="stat-label">教师用户申请待审核</div>
          </div>
        </div>
      </template>

      <template #topMidLeft>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Clock /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ finishedPendingAwardCompetitionCount }}</div>
            <div class="stat-label">结束竞赛待颁奖</div>
          </div>
        </div>
      </template>

      <template #topCenter>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Promotion /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ ongoingCompetitionCount }}</div>
            <div class="stat-label">正在进行竞赛</div>
          </div>
        </div>
      </template>

      <template #topRight>
        <div class="stat-card" v-loading="loading">
          <div class="stat-icon">
            <el-icon :size="20"><Trophy /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ competitionTotalCount }}</div>
            <div class="stat-label">竞赛总数</div>
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
