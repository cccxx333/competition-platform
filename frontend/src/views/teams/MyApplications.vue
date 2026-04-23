<script lang="ts" setup>
import { ElMessage } from "element-plus"
import StatusTag from "@@/components/StatusTag/index.vue"
import { getCompetitionDetail } from "@/api/competitions"
import { listMyApplications, type ApplicationItem } from "@/api/teamApplications"
import { getTeamDetail } from "@/api/teams"

const loading = ref(false)
const items = ref<ApplicationItem[]>([])
const detailVisible = ref(false)
const detailLoading = ref(false)

type ApplicationDetailData = {
  competitionName: string
  competitionLevel: string
  organizer: string
  advisorTeacher: string
  appliedAt: string
  reason: string
  awardName: string
}

const detailData = ref<ApplicationDetailData>({
  competitionName: "-",
  competitionLevel: "-",
  organizer: "-",
  advisorTeacher: "-",
  appliedAt: "-",
  reason: "-",
  awardName: "-"
})

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

const getAdvisorTeacher = (team: any) => {
  const leader = team?.leader
  const realName = typeof leader?.realName === "string" ? leader.realName.trim() : ""
  const username = typeof leader?.username === "string" ? leader.username.trim() : ""
  if (realName) return realName
  if (username) return username
  return "-"
}

const openDetailDialog = async (row: ApplicationItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = {
    competitionName: row.competitionName || "-",
    competitionLevel: "-",
    organizer: "-",
    advisorTeacher: "-",
    appliedAt: formatDateTime(row.appliedAt) || "-",
    reason: (row.reason ?? "").toString().trim() || "-",
    awardName: (row.awardName ?? "").toString().trim() || "-"
  }

  try {
    const [competition, team] = await Promise.all([
      typeof row.competitionId === "number" ? getCompetitionDetail(row.competitionId) : Promise.resolve(null),
      typeof row.teamId === "number" ? getTeamDetail(row.teamId) : Promise.resolve(null)
    ])

    detailData.value = {
      competitionName: competition?.name || row.competitionName || "-",
      competitionLevel: competition?.level || "-",
      organizer: competition?.organizer || "-",
      advisorTeacher: getAdvisorTeacher(team),
      appliedAt: formatDateTime(row.appliedAt) || "-",
      reason: (row.reason ?? "").toString().trim() || "-",
      awardName: (row.awardName ?? "").toString().trim() || "-"
    }
  } catch (error: any) {
    showRequestError(error, "加载详情失败")
  } finally {
    detailLoading.value = false
  }
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
  <div class="my-applications-page">
    <div class="page-header">
      <h2>我的申请</h2>
    </div>

    <div class="applications-card-wrap">
      <el-card shadow="never" class="applications-card" v-loading="loading">
        <el-table v-if="items.length" :data="items" :fit="false" table-layout="fixed" class="applications-table">
          <el-table-column prop="competitionName" label="竞赛名称" width="300" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.competitionName || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.appliedAt) || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <StatusTag :status="row.status" kind="teamApplication" />
            </template>
          </el-table-column>
          <el-table-column label="奖项" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.awardName || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center" header-align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetailDialog(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else-if="!loading" description="暂无申请" />
      </el-card>
    </div>

    <el-dialog v-model="detailVisible" title="申请详情" width="640px" destroy-on-close>
      <el-descriptions :column="1" border v-loading="detailLoading">
        <el-descriptions-item label="竞赛名称">{{ detailData.competitionName }}</el-descriptions-item>
        <el-descriptions-item label="竞赛级别">{{ detailData.competitionLevel }}</el-descriptions-item>
        <el-descriptions-item label="主办方">{{ detailData.organizer }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ detailData.advisorTeacher }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.appliedAt }}</el-descriptions-item>
        <el-descriptions-item label="原因说明">{{ detailData.reason }}</el-descriptions-item>
        <el-descriptions-item label="奖项">{{ detailData.awardName }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-applications-page {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.applications-card-wrap {
  width: 100%;
}

.applications-card {
  width: 100%;
}

.applications-table :deep(.cell) {
  padding-left: 10px;
  padding-right: 10px;
}

.applications-table {
  width: min(100%, 810px);
  margin: 0 auto;
}
</style>
