<script lang="ts" setup>
import { ElMessage } from "element-plus"
import {
  listMyTeacherApplications,
  type TeacherApplicationItem,
  type TeacherApplicationStatus
} from "@/api/teacherApplications"

const loading = ref(false)
const errorMessage = ref("")
const items = ref<TeacherApplicationItem[]>([])
const statusFilter = ref<TeacherApplicationStatus | "">("")

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
  ElMessage.error("Request failed, please try again")
  return "Request failed, please try again"
}

const loadApplications = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const params: { status?: TeacherApplicationStatus } = {}
    if (statusFilter.value) params.status = statusFilter.value
    items.value = await listMyTeacherApplications(params)
  } catch (error: any) {
    items.value = []
    errorMessage.value = showRequestError(error, "Failed to load teacher applications")
  } finally {
    loading.value = false
  }
}

onMounted(loadApplications)
watch(statusFilter, loadApplications)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>My Teacher Applications</h2>
      <div class="page-header__filters">
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
