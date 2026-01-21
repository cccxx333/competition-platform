<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getCompetitionDetail, type CompetitionDetail } from "@/api/competitions"
import { createTeacherApplication } from "@/api/teacherApplications"
import { useAuthStore } from "@/stores/auth"
import StatusPill from "@@/components/StatusPill/index.vue"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)
const submitError = ref("")
const errorMessage = ref("")
const detail = ref<CompetitionDetail | null>(null)

const competitionId = computed(() => Number(route.params.id))
const isTeacher = computed(() => String(authStore.user?.role ?? "").toUpperCase() === "TEACHER")
const registrationDeadlineDate = computed(() => {
  const deadline = detail.value?.registrationDeadline
  if (!deadline) return null
  const raw = deadline.includes("T") ? deadline.split("T")[0] : deadline
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return null
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
})
const todayDate = computed(() => {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), now.getDate())
})
const canApplyTeacher = computed(() => {
  if (!isTeacher.value) return false
  if (!detail.value) return false
  if (detail.value.status !== "UPCOMING") return false
  if (!registrationDeadlineDate.value) return false
  return todayDate.value < registrationDeadlineDate.value
})
const applyDisabledReason = computed(() => {
  if (!isTeacher.value || !detail.value) return ""
  if (detail.value.status !== "UPCOMING") return "仅未开始的竞赛可申请"
  if (!registrationDeadlineDate.value) return "报名截止时间不可用"
  if (!(todayDate.value < registrationDeadlineDate.value)) return "报名截止时间已过"
  return ""
})

const formatDate = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    return value.split("T")[0]
  }
  return value
}

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
    ElMessage.error("竞赛不存在或已删除")
    return "竞赛不存在或已删除"
  }
  if (status === 409) {
    ElMessage.error("业务冲突")
    return "业务冲突"
  }
  ElMessage.error("服务异常，请稍后重试")
  return "服务异常，请稍后重试"
}

const loadDetail = async () => {
  errorMessage.value = ""
  if (!Number.isFinite(competitionId.value) || competitionId.value <= 0) {
    errorMessage.value = "竞赛不存在或已删除"
    return
  }
  loading.value = true
  try {
    detail.value = await getCompetitionDetail(competitionId.value)
  } catch (error: any) {
    detail.value = null
    errorMessage.value = showRequestError(error, "加载竞赛详情失败")
  } finally {
    loading.value = false
  }
}

const basicFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "名称", value: data.name },
    { label: "主办方", value: data.organizer },
    { label: "类别", value: data.category },
    { label: "级别", value: data.level }
  ].filter(item => Boolean(item.value))
})

const timeFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "开始日期", value: formatDate(data.startDate) },
    { label: "结束日期", value: formatDate(data.endDate) },
    { label: "报名截止", value: formatDate(data.registrationDeadline) }
  ].filter(item => Boolean(item.value))
})

const ruleFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "最小队伍人数", value: data.minTeamSize },
    { label: "最大队伍人数", value: data.maxTeamSize }
  ].filter(item => item.value !== undefined && item.value !== null)
})

const metaFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "创建时间", value: formatDateTime(data.createdAt) },
    { label: "更新时间", value: formatDateTime(data.updatedAt) }
  ].filter(item => Boolean(item.value))
})

const handleBack = () => {
  router.push("/competitions")
}

const openSubmitDialog = () => {
  submitError.value = ""
  if (!canApplyTeacher.value) {
    submitError.value = applyDisabledReason.value || "当前不可申请"
    submitDialogVisible.value = true
    return
  }
  submitDialogVisible.value = true
}

const closeSubmitDialog = (force = false) => {
  if (submitting.value && !force) return
  submitDialogVisible.value = false
  submitError.value = ""
}

const handleApply = async () => {
  if (submitting.value) return
  if (!Number.isFinite(competitionId.value) || competitionId.value <= 0) {
    ElMessage.error("竞赛不存在")
    return
  }
  if (!canApplyTeacher.value) {
    submitError.value = applyDisabledReason.value || "当前不可申请"
    return
  }
  submitting.value = true
  try {
    await createTeacherApplication(competitionId.value, {})
    ElMessage.success("申请已提交")
    closeSubmitDialog(true)
    router.push("/teacher/applications")
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? error?.response?.data?.message
    const code = error?.response?.data?.code
    if (status === 409 || code === "BUSINESS_ERROR" || (typeof message === "string" && message.includes("already exists"))) {
      submitError.value = message || "申请已存在"
      return
    }
    submitError.value = message || "提交教师申请失败"
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>竞赛详情</h2>
      <div class="header-actions">
        <div v-if="isTeacher" class="apply-action">
          <el-button
            size="small"
            type="primary"
            :loading="submitting"
            :disabled="loading || submitting || !canApplyTeacher"
            @click="openSubmitDialog"
          >
            提交教师申请
          </el-button>
          <div v-if="!canApplyTeacher && applyDisabledReason" class="apply-hint">
            {{ applyDisabledReason }}
          </div>
        </div>
        <el-button class="back-btn" size="small" @click="handleBack">返回</el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-skeleton v-if="loading" :rows="6" animated />

    <div v-else-if="!errorMessage && detail">
      <el-card shadow="never" class="section">
        <h3>基本信息</h3>
        <el-descriptions v-if="basicFields.length || detail.status" :column="1">
          <el-descriptions-item v-for="item in basicFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.status" label="状态">
            <StatusPill :value="detail.status" kind="competition" />
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>暂无信息</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>时间信息</h3>
        <el-descriptions v-if="timeFields.length" :column="1">
          <el-descriptions-item v-for="item in timeFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>暂无信息</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>规则</h3>
        <el-descriptions v-if="ruleFields.length" :column="1">
          <el-descriptions-item v-for="item in ruleFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>暂无信息</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>元信息</h3>
        <el-descriptions v-if="metaFields.length" :column="1">
          <el-descriptions-item v-for="item in metaFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>暂无信息</div>
      </el-card>

      <el-card v-if="detail.description" shadow="never" class="section">
        <el-collapse>
          <el-collapse-item title="描述" name="description">
            <pre>{{ detail.description }}</pre>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>

    <el-dialog
      v-model="submitDialogVisible"
      title="确认"
      width="480px"
      center
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :before-close="closeSubmitDialog"
    >
      <div>确认提交该竞赛的教师申请？</div>
      <div style="margin-top: 8px; color: #909399;">提交后请等待管理员审核。</div>
      <el-alert
        v-if="submitError"
        :title="submitError"
        type="error"
        show-icon
        :closable="false"
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button :disabled="submitting" @click="closeSubmitDialog">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleApply">
          提交
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.apply-action {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
}

.apply-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.back-btn {
  margin-top: 0;
}

.section {
  margin-bottom: 12px;
}
</style>
