<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listPendingApplications, reviewApplication, type ApplicationItem } from "@/api/teamApplications"

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const items = ref<ApplicationItem[]>([])
const current = ref<ApplicationItem | null>(null)
const reviewAction = ref<"approve" | "reject">("approve")
const reason = ref("")
const teamIdFilter = ref<number | null>(null)

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
  if (status === 401) {
    ElMessage.error("登录已过期，请重新登录")
    return "登录已过期"
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

const loadApplications = async () => {
  loading.value = true
  try {
    items.value = await listPendingApplications({
      status: "PENDING",
      teamId: teamIdFilter.value ?? undefined
    })
  } catch (error: any) {
    items.value = []
    showRequestError(error, "Failed to load applications")
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
      if (current.value?.teamId) {
        router.push(`/teams/${current.value.teamId}`)
      }
    } else {
      showRequestError(error, "Failed to review application")
    }
  } finally {
    dialogLoading.value = false
  }
}

onMounted(loadApplications)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>教师审核</h2>
      <div class="page-header__filters">
        <el-input-number v-model="teamIdFilter" :min="1" :controls="false" placeholder="Team ID" />
        <el-button :loading="loading" @click="loadApplications">刷新</el-button>
      </div>
    </div>

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

    <el-empty v-else-if="!loading" description="暂无待审申请" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="reviewAction === 'approve' ? '通过审核' : '拒绝申请'" width="420px">
    <el-form label-width="80px">
      <el-form-item label="原因">
        <el-input v-model="reason" type="textarea" :rows="3" placeholder="拒绝原因（必填）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="dialogLoading" @click="submitReview">提交</el-button>
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

.page-header__filters {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}
</style>
