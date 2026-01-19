<script lang="ts" setup>
import { ElMessage, ElMessageBox } from "element-plus"
import {
  adminListTeacherApplications,
  adminReviewTeacherApplication,
  type TeacherApplicationItem,
  type TeacherApplicationStatus
} from "@/api/teacherApplications"

const loading = ref(false)
const errorMessage = ref("")
const items = ref<TeacherApplicationItem[]>([])
const statusFilter = ref<TeacherApplicationStatus | "">("")
const competitionIdFilter = ref("")
const reviewingId = ref<number | null>(null)

const formatDateTime = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const statusTagType = (status?: TeacherApplicationStatus) => {
  if (status === "APPROVED") return "success"
  if (status === "REJECTED") return "danger"
  return "warning"
}

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
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

const buildParams = () => {
  const params: { status?: TeacherApplicationStatus; competitionId?: number } = {}
  if (statusFilter.value) params.status = statusFilter.value
  const competitionId = Number(competitionIdFilter.value)
  if (Number.isFinite(competitionId) && competitionId > 0) {
    params.competitionId = competitionId
  }
  return params
}

const loadApplications = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    items.value = await adminListTeacherApplications(buildParams())
  } catch (error: any) {
    items.value = []
    errorMessage.value = showRequestError(error, "Failed to load admin applications")
  } finally {
    loading.value = false
  }
}

const handleReview = async (row: TeacherApplicationItem, approved: boolean, reviewComment?: string | null) => {
  if (!row.id) return
  reviewingId.value = row.id
  try {
    await adminReviewTeacherApplication(row.id, {
      approved,
      reviewComment: reviewComment ? reviewComment : null
    })
    ElMessage.success(approved ? "Approved" : "Rejected")
    await loadApplications()
  } catch (error: any) {
    showRequestError(error, "Failed to review application")
  } finally {
    reviewingId.value = null
  }
}

const promptReview = async (row: TeacherApplicationItem, approved: boolean) => {
  const title = approved ? "Approve Application" : "Reject Application"
  const confirmButtonText = approved ? "Approve" : "Reject"
  try {
    const result = await ElMessageBox.prompt("Enter review comment (optional)", title, {
      confirmButtonText,
      cancelButtonText: "Cancel",
      inputType: "textarea",
      inputPlaceholder: "Optional"
    })
    const comment = result.value?.trim()
    await handleReview(row, approved, comment ? comment : null)
  } catch {
    // canceled
  }
}

onMounted(loadApplications)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Teacher Applications Review</h2>
      <div class="page-header__filters">
        <el-input v-model="competitionIdFilter" placeholder="Competition ID" style="width: 160px" />
        <el-select v-model="statusFilter" clearable placeholder="Status" style="width: 160px">
          <el-option label="PENDING" value="PENDING" />
          <el-option label="APPROVED" value="APPROVED" />
          <el-option label="REJECTED" value="REJECTED" />
        </el-select>
        <el-button :loading="loading" @click="loadApplications">Refresh</el-button>
      </div>
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
      <el-table-column prop="competitionId" label="Competition ID" width="150" />
      <el-table-column prop="teacherId" label="Teacher ID" width="120" />
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.status ?? "-" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Applied At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.appliedAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Review Comment">
        <template #default="{ row }">
          {{ row.reviewComment || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="220">
        <template #default="{ row }">
          <el-button
            size="small"
            type="success"
            :loading="reviewingId === row.id"
            :disabled="row.status !== 'PENDING' || reviewingId === row.id"
            @click="promptReview(row, true)"
          >
            Approve
          </el-button>
          <el-button
            size="small"
            type="danger"
            :loading="reviewingId === row.id"
            :disabled="row.status !== 'PENDING' || reviewingId === row.id"
            @click="promptReview(row, false)"
          >
            Reject
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!errorMessage && !loading" description="No applications yet" />
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-header__filters {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}
</style>
