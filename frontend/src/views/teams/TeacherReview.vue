<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getCompetitionDetail } from "@/api/competitions"
import { listPendingApplications, reviewApplication, type ApplicationItem, type ApplicationStatus } from "@/api/teamApplications"
import { getApiErrorMessage } from "@/utils/errorMessage"
import StatusTag from "@/common/components/StatusTag/index.vue"

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const items = ref<ApplicationItem[]>([])
const current = ref<ApplicationItem | null>(null)
const reviewAction = ref<"approve" | "reject">("approve")
const reason = ref("")
const competitionStatusMap = ref<Record<number, string>>({})
const competitionKeywordFilter = ref("")
const reviewStatusFilter = ref<"" | ApplicationStatus>("")

const statusOptions: Array<{ label: string; value: "" | ApplicationStatus }> = [
  { label: "全部状态", value: "" },
  { label: "待审核", value: "PENDING" },
  { label: "已通过", value: "APPROVED" },
  { label: "已拒绝", value: "REJECTED" }
]

const filteredItems = computed(() => {
  const keyword = competitionKeywordFilter.value.trim().toLowerCase()
  const selectedStatus = reviewStatusFilter.value
  return items.value.filter((item) => {
    const matchKeyword = !keyword || String(item.competitionName ?? "").toLowerCase().includes(keyword)
    const matchStatus = !selectedStatus || item.status === selectedStatus
    return matchKeyword && matchStatus
  })
})

const formatDateTime = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "参数错误"
  if (status === 401) return "登录已过期，请重新登录"
  if (status === 403) return "无权访问"
  if (status === 404) return "队伍不存在或已删除"
  if (status === 409) return "操作冲突，请稍后重试"
  return "服务异常，请稍后重试"
}

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const fallbackMessage = status ? getFallbackMessage(status) : fallback
  const message = getApiErrorMessage(error, fallbackMessage)
  ElMessage.error(message)
  return message
}

const loadApplications = async () => {
  loading.value = true
  try {
    const list = await listPendingApplications({ status: undefined })
    items.value = (list ?? []).slice().sort((a, b) => {
      const timeA = a.appliedAt ? Date.parse(a.appliedAt) : 0
      const timeB = b.appliedAt ? Date.parse(b.appliedAt) : 0
      return timeB - timeA
    })
    await loadCompetitionStatuses(items.value)
  } catch (error: any) {
    items.value = []
    competitionStatusMap.value = {}
    showRequestError(error, "加载申请失败")
  } finally {
    loading.value = false
  }
}

const loadCompetitionStatuses = async (list: ApplicationItem[]) => {
  const ids = Array.from(new Set(
    list
      .map(item => item.competitionId)
      .filter((id): id is number => typeof id === "number" && id > 0)
  ))
  if (!ids.length) {
    competitionStatusMap.value = {}
    return
  }
  try {
    const pairs = await Promise.all(ids.map(async (id) => {
      const detail = await getCompetitionDetail(id)
      return [id, detail?.status] as const
    }))
    const next: Record<number, string> = {}
    for (const [id, status] of pairs) {
      if (status) {
        next[id] = status
      }
    }
    competitionStatusMap.value = next
  } catch (error) {
    competitionStatusMap.value = {}
  }
}

const getCompetitionStatus = (item: ApplicationItem) => {
  const id = item?.competitionId
  if (typeof id !== "number") return ""
  return competitionStatusMap.value[id] ?? ""
}

const isApproveDisabled = (item: ApplicationItem) => {
  return item?.status === "PENDING" && getCompetitionStatus(item) === "FINISHED"
}

const openDialog = (item: ApplicationItem, action: "approve" | "reject") => {
  if (action === "approve" && isApproveDisabled(item)) {
    ElMessage.warning("竞赛已结束，无法通过")
    return
  }
  current.value = item
  reviewAction.value = action
  reason.value = ""
  dialogVisible.value = true
}

const submitReview = async () => {
  if (!current.value?.id) return
  if (reviewAction.value === "reject" && !reason.value.trim()) {
    ElMessage.warning("请填写拒绝原因")
    return
  }
  dialogLoading.value = true
  try {
    await reviewApplication(current.value.id, {
      approved: reviewAction.value === "approve",
      reason: reason.value
    })
    ElMessage.success(reviewAction.value === "approve" ? "已通过" : "已拒绝")
    dialogVisible.value = false
    await loadApplications()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && message.includes("disbanded")) {
      ElMessage.error("队伍已解散，操作已禁止")
      router.push("/teams/lookup")
    } else {
      showRequestError(error, "审核失败")
    }
  } finally {
    dialogLoading.value = false
  }
}

onMounted(loadApplications)

const resetFilters = () => {
  competitionKeywordFilter.value = ""
  reviewStatusFilter.value = ""
}
</script>

<template>
  <div class="page-container">

    <div class="page-header review-header">
      <h2>学生入队申请审核</h2>
    </div>

  <el-card shadow="never" v-loading="loading" class="review-card">
      <div class="filter-row">
        <el-input
          v-model="competitionKeywordFilter"
          placeholder="竞赛名称"
          clearable
          style="width: 220px"
        />
        <el-select v-model="reviewStatusFilter" style="width: 160px">
          <el-option v-for="option in statusOptions" :key="option.value || 'ALL'" :label="option.label" :value="option.value" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table v-if="filteredItems.length" :data="filteredItems" style="width: 100%">
        <el-table-column label="学生" width="120">
          <template #default="{ row }">
            {{ row.studentName || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="学号" width="110">
          <template #default="{ row }">
            {{ row.studentAccountNo || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="竞赛" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.competitionName || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="teamId" label="队伍 ID" width="120" />
        <el-table-column label="审核状态" width="140">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="teamApplication" />
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.appliedAt) || "-" }}
          </template>
        
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :disabled="row.status !== 'PENDING' || isApproveDisabled(row)"
              :style="row.status !== 'PENDING' ? { opacity: 0.6 } : undefined"
              :title="isApproveDisabled(row) ? '竞赛已结束，无法通过' : ''"
              @click="openDialog(row, 'approve')"
            >
              通过
            </el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="row.status !== 'PENDING'"
              :style="row.status !== 'PENDING' ? { opacity: 0.6 } : undefined"
              @click="openDialog(row, 'reject')"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else-if="!loading" description="暂无匹配申请" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="reviewAction === 'approve' ? '通过审核' : '拒绝申请'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="原因">
          <el-input v-model="reason" type="textarea" :rows="3" placeholder="（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div></template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-header {
  max-width: 1080px;
  margin: 0 auto 12px;
}

.review-card {
  max-width: 1080px;
  margin: 0 auto;
}

</style>

