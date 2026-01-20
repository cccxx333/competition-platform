<script lang="ts" setup>
import { ElMessage } from "element-plus"
import {
  adminListTeacherApplicationPage,
  adminReviewTeacherApplication,
  type AdminTeacherApplicationListItem
} from "@/api/teacherApplications"
import StatusPill from "@@/components/StatusPill/index.vue"

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
    ElMessage.error("Invalid request")
    return "Invalid request"
  }
  if (status === 403) {
    ElMessage.error("No permission")
    return "No permission"
  }
  if (status === 404) {
    ElMessage.error("Application not found")
    return "Application not found"
  }
  if (status === 409) {
    ElMessage.error("Application already reviewed")
    return "Application already reviewed"
  }
  ElMessage.error("Request failed, please try again")
  return "Request failed, please try again"
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

const handleApprove = async (row: AdminTeacherApplicationListItem) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm("Approve this application?", "Confirm", {
      type: "warning",
      confirmButtonText: "Approve",
      cancelButtonText: "Cancel"
    })
  } catch {
    return
  }
  try {
    await adminReviewTeacherApplication(row.id, { approved: true })
    ElMessage.success("Approved")
    await fetchList()
  } catch (error: any) {
    showRequestError(error, "Failed to review application")
  }
}

const reviewDialogVisible = ref(false)
const reviewAction = ref<"approve" | "reject">("approve")
const reviewReason = ref("")
const reviewTargetId = ref<number | null>(null)
const reviewSubmitting = ref(false)

const openApproveDialog = (row: AdminTeacherApplicationListItem) => {
  if (!row.id) return
  reviewTargetId.value = row.id
  reviewAction.value = "approve"
  reviewReason.value = ""
  reviewDialogVisible.value = true
}

const openRejectDialog = (row: AdminTeacherApplicationListItem) => {
  if (!row.id) return
  reviewTargetId.value = row.id
  reviewAction.value = "reject"
  reviewReason.value = ""
  reviewDialogVisible.value = true
}

const closeReviewDialog = (force = false) => {
  if (reviewSubmitting.value && !force) return
  reviewDialogVisible.value = false
  reviewReason.value = ""
  reviewTargetId.value = null
}

const submitReview = async () => {
  if (!reviewTargetId.value || reviewSubmitting.value) return
  const reviewComment = reviewAction.value === "reject" ? reviewReason.value.trim() || undefined : undefined
  reviewSubmitting.value = true
  try {
    await adminReviewTeacherApplication(reviewTargetId.value, {
      approved: reviewAction.value === "approve",
      reviewComment
    })
    ElMessage.success(reviewAction.value === "approve" ? "Approved" : "Rejected")
    closeReviewDialog(true)
    await fetchList()
  } catch (error: any) {
    showRequestError(error, "Failed to review application")
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Teacher Applications Review</h2>
    </div>

    <el-table :data="items" style="width: 100%">
      <el-table-column prop="teacherName" label="Teacher" min-width="160" />
      <el-table-column prop="competitionName" label="Competition" min-width="200" />
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <StatusPill :value="row.status" kind="teacherApplication" />
        </template>
      </el-table-column>
      <el-table-column label="Created At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button
            size="small"
            type="primary"
            :disabled="row.status !== 'PENDING'"
            @click="openApproveDialog(row)"
          >
            Approve
          </el-button>
          <el-button
            size="small"
            type="danger"
            :disabled="row.status !== 'PENDING'"
            @click="openRejectDialog(row)"
          >
            Reject
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
      :title="reviewAction === 'approve' ? 'Approve' : 'Reject'"
      width="480px"
      center
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :before-close="closeReviewDialog"
    >
      <div v-if="reviewAction === 'approve'">Approve this application?</div>
      <el-input
        v-else
        v-model="reviewReason"
        type="textarea"
        :rows="4"
        placeholder="Provide a reject reason (optional)"
      />
      <template #footer>
        <el-button :disabled="reviewSubmitting" @click="closeReviewDialog">Cancel</el-button>
        <el-button
          v-if="reviewAction === 'approve'"
          type="primary"
          :loading="reviewSubmitting"
          @click="submitReview"
        >
          Approve
        </el-button>
        <el-button
          v-else
          type="danger"
          :loading="reviewSubmitting"
          @click="submitReview"
        >
          Reject
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
