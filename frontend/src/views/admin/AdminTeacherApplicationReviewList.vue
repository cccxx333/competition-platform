<script lang="ts" setup>
import { ElMessage } from "element-plus"
import {
  adminListTeacherApplicationPage,
  adminReviewTeacherApplication,
  type AdminTeacherApplicationListItem
} from "@/api/teacherApplications"
import StatusTag from "@@/components/StatusTag/index.vue"

const loading = ref(false)
const items = ref<AdminTeacherApplicationListItem[]>([])
const total = ref(0)

const pagination = reactive({
  page: 0,
  size: 10
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
  if (status === 400) {
    ElMessage.error("请求无效")
    return "请求无效"
  }
  if (status === 403) {
    ElMessage.error("无权限")
    return "无权限"
  }
  if (status === 404) {
    ElMessage.error("申请不存在")
    return "申请不存在"
  }
  if (status === 409) {
    ElMessage.error("申请已审核")
    return "申请已审核"
  }
  ElMessage.error("请求失败，请稍后重试")
  return "请求失败，请稍后重试"
}

const fetchList = async () => {
  loading.value = true
  try {
    const { items: data, total: totalElements, page, size } = await adminListTeacherApplicationPage({
      page: pagination.page,
      size: pagination.size
    })
    items.value = data
    total.value = typeof totalElements === "number" ? totalElements : 0
    if (typeof page === "number" && page !== pagination.page) {
      pagination.page = page
    }
    if (typeof size === "number" && size !== pagination.size) {
      pagination.size = size
    }
  } catch (error) {
    console.error(error)
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page - 1
  fetchList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 0
  fetchList()
}

const reviewDialogVisible = ref(false)
const reviewAction = ref<"approve" | "reject">("approve")
const reviewReason = ref("")
const reviewTargetId = ref<number | null>(null)
const reviewSubmitting = ref(false)
const submitError = ref("")
const approveButtonStyle = {
  "--el-button-bg-color": "#22c55e",
  "--el-button-border-color": "#22c55e",
  "--el-button-text-color": "#ffffff",
  "--el-button-hover-bg-color": "#16a34a",
  "--el-button-hover-border-color": "#16a34a",
  "--el-button-active-bg-color": "#15803d",
  "--el-button-active-border-color": "#15803d",
  "--el-button-disabled-bg-color": "#22c55e",
  "--el-button-disabled-border-color": "#22c55e",
  "--el-button-disabled-text-color": "#ffffff"
} as const

const openApproveDialog = (row: AdminTeacherApplicationListItem) => {
  if (!row.id) return
  reviewTargetId.value = row.id
  reviewAction.value = "approve"
  reviewReason.value = ""
  submitError.value = ""
  reviewDialogVisible.value = true
}

const openRejectDialog = (row: AdminTeacherApplicationListItem) => {
  if (!row.id) return
  reviewTargetId.value = row.id
  reviewAction.value = "reject"
  reviewReason.value = ""
  submitError.value = ""
  reviewDialogVisible.value = true
}

const closeReviewDialog = () => {
  if (reviewSubmitting.value) return
  reviewDialogVisible.value = false
  reviewReason.value = ""
  reviewTargetId.value = null
  submitError.value = ""
}

const forceCloseReviewDialog = () => {
  reviewDialogVisible.value = false
  reviewReason.value = ""
  reviewTargetId.value = null
  submitError.value = ""
}

const handleReviewDialogBeforeClose = (done: () => void) => {
  if (reviewSubmitting.value) return
  done()
  closeReviewDialog()
}

const submitReview = async () => {
  if (!reviewTargetId.value || reviewSubmitting.value) return
  const reviewComment = reviewReason.value.trim() || undefined
  reviewSubmitting.value = true
  try {
    await adminReviewTeacherApplication(reviewTargetId.value, {
      approved: reviewAction.value === "approve",
      reviewComment
    })
    ElMessage.success(reviewAction.value === "approve" ? "已通过" : "已拒绝")
    forceCloseReviewDialog()
    await fetchList()
  } catch (error: any) {
    showRequestError(error, "审核失败")
    submitError.value =
      error?.response?.data?.message ?? error?.message ?? "审核失败"
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page-container">

    <div class="page-header review-header">
      <h2>教师建队申请审核</h2>
    </div>

  <el-card shadow="never" v-loading="loading" class="review-card">
    

      <el-table :data="items" style="width: 100%">
        <el-table-column prop="teacherName" label="教师" min-width="100" />
        <el-table-column prop="teacherAccountNo" label="工号" width="180" />
        <el-table-column prop="competitionName" label="竞赛" min-width="160" />
        <el-table-column label="队伍名称" min-width="150">
          <template #default="{ row }">
            {{ row.teamName || "自动生成" }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="teacherApplication" />
        
      </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) || "-" }}
      </template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :style="{ ...approveButtonStyle, ...(row.status !== 'PENDING' ? { opacity: 0.6 } : {}) }"
              :disabled="row.status !== 'PENDING'"
              @click="openApproveDialog(row)"
            >
              通过
            </el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="row.status !== 'PENDING'"
              @click="openRejectDialog(row)"
            >
              拒绝
            </el-button>
      </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.page + 1"
          :page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>

      <el-dialog
        v-model="reviewDialogVisible"
        :title="reviewAction === 'approve' ? '通过' : '拒绝'"
        width="480px"
        center
        :close-on-click-modal="true"
        :close-on-press-escape="true"
        :before-close="handleReviewDialogBeforeClose"
      >
        <div class="review-note">原因（可选）</div>
        <el-alert
          v-if="submitError"
          :title="submitError"
          type="error"
          show-icon
          :closable="false"
          style="margin-bottom: 12px"
        />
        <el-input
          v-model="reviewReason"
          type="textarea"
          :rows="4"
          placeholder="请填写原因（可选）"
        />
        <template #footer>
          <el-button :disabled="reviewSubmitting" @click="closeReviewDialog">取消</el-button>
          <el-button
            v-if="reviewAction === 'approve'"
            type="primary"
            :loading="reviewSubmitting"
            @click="submitReview"
          >
            通过
          </el-button>
          <el-button
            v-else
            type="danger"
            :loading="reviewSubmitting"
            @click="submitReview"
          >
            拒绝
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
    </template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-header {
  max-width: 980px;
  margin: 0 auto 12px;
}

.review-card {
  max-width: 1040px;
  margin: 0 auto;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.review-note {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
