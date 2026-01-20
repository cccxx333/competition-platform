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
    errorMessage.value = showRequestError(error, "Failed to load competition detail")
  } finally {
    loading.value = false
  }
}

const basicFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "Name", value: data.name },
    { label: "Organizer", value: data.organizer },
    { label: "Category", value: data.category },
    { label: "Level", value: data.level }
  ].filter(item => Boolean(item.value))
})

const timeFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "Start Date", value: formatDate(data.startDate) },
    { label: "End Date", value: formatDate(data.endDate) },
    { label: "Registration Deadline", value: formatDate(data.registrationDeadline) }
  ].filter(item => Boolean(item.value))
})

const ruleFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "Min Team Size", value: data.minTeamSize },
    { label: "Max Team Size", value: data.maxTeamSize }
  ].filter(item => item.value !== undefined && item.value !== null)
})

const metaFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "Created At", value: formatDateTime(data.createdAt) },
    { label: "Updated At", value: formatDateTime(data.updatedAt) }
  ].filter(item => Boolean(item.value))
})

const handleBack = () => {
  router.push("/competitions")
}

const openSubmitDialog = () => {
  submitError.value = ""
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
    ElMessage.error("Invalid competition")
    return
  }
  submitting.value = true
  try {
    await createTeacherApplication(competitionId.value, {})
    ElMessage.success("Application submitted")
    closeSubmitDialog(true)
    router.push("/teacher/applications")
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? error?.response?.data?.message
    const code = error?.response?.data?.code
    if (status === 409 || code === "BUSINESS_ERROR" || (typeof message === "string" && message.includes("already exists"))) {
      submitError.value = message || "Application already exists"
      return
    }
    submitError.value = message || "Failed to submit teacher application"
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
      <h2>Competition Detail</h2>
      <div class="header-actions">
        <el-button
          v-if="isTeacher"
          size="small"
          type="primary"
          :loading="submitting"
          :disabled="loading || submitting"
          @click="openSubmitDialog"
        >
          Submit Teacher Application
        </el-button>
        <el-button size="small" @click="handleBack">Back</el-button>
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
        <h3>Basic Info</h3>
        <el-descriptions v-if="basicFields.length || detail.status" :column="1">
          <el-descriptions-item v-for="item in basicFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.status" label="Status">
            <StatusPill :value="detail.status" kind="competition" />
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>Empty</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>Time Info</h3>
        <el-descriptions v-if="timeFields.length" :column="1">
          <el-descriptions-item v-for="item in timeFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>Empty</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>Rules</h3>
        <el-descriptions v-if="ruleFields.length" :column="1">
          <el-descriptions-item v-for="item in ruleFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>Empty</div>
      </el-card>

      <el-card shadow="never" class="section">
        <h3>Meta</h3>
        <el-descriptions v-if="metaFields.length" :column="1">
          <el-descriptions-item v-for="item in metaFields" :key="item.label" :label="item.label">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-else>Empty</div>
      </el-card>

      <el-card v-if="detail.description" shadow="never" class="section">
        <el-collapse>
          <el-collapse-item title="Description" name="description">
            <pre>{{ detail.description }}</pre>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>

    <el-dialog
      v-model="submitDialogVisible"
      title="Confirm"
      width="480px"
      center
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :before-close="closeSubmitDialog"
    >
      <div>Submit teacher application for this competition?</div>
      <div style="margin-top: 8px; color: #909399;">After submission, wait for admin review.</div>
      <el-alert
        v-if="submitError"
        :title="submitError"
        type="error"
        show-icon
        :closable="false"
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button :disabled="submitting" @click="closeSubmitDialog">Cancel</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleApply">
          Submit
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
  gap: 8px;
  align-items: center;
}

.section {
  margin-bottom: 12px;
}
</style>
