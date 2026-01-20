<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listPendingApplications, reviewApplication, type ApplicationItem } from "@/api/teamApplications"

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const errorMessage = ref("")
const items = ref<ApplicationItem[]>([])
const current = ref<ApplicationItem | null>(null)
const reviewAction = ref<"approve" | "reject">("approve")
const reason = ref("")

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
  ElMessage.error("服务异常，请稍后重试")
  return fallback
}

const loadApplications = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    items.value = await listPendingApplications("PENDING")
  } catch (error: any) {
    items.value = []
    errorMessage.value = showRequestError(error, "Failed to load applications")
  } finally {
    loading.value = false
  }
}

const openDialog = (item: ApplicationItem, action: "approve" | "reject") => {
  current.value = item
  reviewAction.value = action
  reason.value = ""
  dialogVisible.value = true
}

const submitReview = async () => {
  if (!current.value?.id) return
  dialogLoading.value = true
  try {
    await reviewApplication(current.value.id, {
      approved: reviewAction.value === "approve",
      reason: reason.value
    })
    ElMessage.success(reviewAction.value === "approve" ? "Approved" : "Rejected")
    dialogVisible.value = false
    await loadApplications()
  } catch (error: any) {
    showRequestError(error, "Failed to review application")
  } finally {
    dialogLoading.value = false
  }
}

onMounted(loadApplications)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Teacher Review</h2>
      <el-button :loading="loading" @click="loadApplications">Refresh</el-button>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-table v-if="items.length" :data="items" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentId" label="Student ID" width="120" />
      <el-table-column prop="competitionId" label="Competition ID" width="150" />
      <el-table-column prop="teamId" label="Team ID" width="120" />
      <el-table-column label="Applied At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.appliedAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openDialog(row, 'approve')">Approve</el-button>
          <el-button size="small" type="danger" @click="openDialog(row, 'reject')">Reject</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!errorMessage && !loading" description="No applications yet" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="reviewAction === 'approve' ? 'Approve' : 'Reject'" width="420px">
    <el-form label-width="80px">
      <el-form-item label="Reason">
        <el-input v-model="reason" type="textarea" :rows="3" placeholder="Optional" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">Cancel</el-button>
      <el-button
        type="primary"
        :loading="dialogLoading"
        @click="submitReview"
      >
        Submit
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
</style>
