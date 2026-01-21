<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { publishAward, type AwardPublishResponse } from "@/api/awards"
import { useAuthStore } from "@/stores/auth"
import { getApiErrorMessage } from "@/utils/errorMessage"

const authStore = useAuthStore()
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")

const submitting = ref(false)
const form = reactive({
  competitionId: "",
  teamId: "",
  awardName: ""
})

const lastResult = ref<AwardPublishResponse | null>(null)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")

const resetResult = () => {
  lastResult.value = null
}

const showError = (message: string) => {
  errorDialogMessage.value = message
  errorDialogVisible.value = true
}

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "Invalid request"
  if (status === 401) return "Session expired, please log in again"
  if (status === 403) return "No permission (ADMIN only)"
  if (status === 404) return "Competition or team not found"
  if (status === 409) return "Conflict"
  return "Failed to publish award"
}

const parseId = (value: string, label: string) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    showError(`${label} must be a positive number`)
    return null
  }
  return parsed
}

const handleSubmit = async () => {
  if (!isAdmin.value) {
    showError("No permission (ADMIN only)")
    return
  }
  const competitionId = parseId(form.competitionId, "Competition ID")
  if (!competitionId) return
  const teamId = parseId(form.teamId, "Team ID")
  if (!teamId) return
  const awardName = form.awardName.trim()
  if (!awardName) {
    showError("Award name is required")
    return
  }

  submitting.value = true
  resetResult()
  try {
    const result = await publishAward({ competitionId, teamId, awardName })
    lastResult.value = result
    ElMessage.success("Award published")
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    const message = getApiErrorMessage(error, fallback)
    showError(message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>Publish Award</h2>
    </div>

    <el-alert
      v-if="!isAdmin"
      type="warning"
      show-icon
      title="Only ADMIN can publish awards."
      class="status-alert"
    />

    <el-alert
      type="info"
      show-icon
      class="status-alert"
      title="Manual input is required. Use Competition list and Team lookup to find IDs."
    />

    <el-form label-position="top" class="publish-form">
      <el-form-item label="Competition ID">
        <el-input
          v-model="form.competitionId"
          placeholder="Enter competitionId (FINISHED)"
          :disabled="submitting"
        />
      </el-form-item>
      <el-form-item label="Team ID">
        <el-input
          v-model="form.teamId"
          placeholder="Enter teamId (CLOSED)"
          :disabled="submitting"
        />
      </el-form-item>
      <el-form-item label="Award Name">
        <el-input
          v-model="form.awardName"
          maxlength="64"
          show-word-limit
          placeholder="e.g. First Prize"
          :disabled="submitting"
        />
      </el-form-item>
      <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">
        Publish
      </el-button>
    </el-form>

    <el-card v-if="lastResult" shadow="never" class="result-card">
      <h3>Publish Result</h3>
      <el-descriptions :column="1">
        <el-descriptions-item label="Award ID">{{ lastResult.awardId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="Competition ID">{{ lastResult.competitionId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="Team ID">{{ lastResult.teamId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="Award Name">{{ lastResult.awardName ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="Recipient Count">{{ lastResult.recipientCount ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="Recipient User IDs">
          {{ lastResult.recipientUserIds?.length ? lastResult.recipientUserIds.join(", ") : "-" }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="result-hint">
        You can log in as a recipient to verify the honors page.
      </div>
    </el-card>
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="Publish Failed" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="errorDialogVisible = false">OK</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.status-alert {
  margin-bottom: 12px;
}

.publish-form {
  max-width: 520px;
}

.result-card {
  margin-top: 16px;
}

.result-hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}
</style>
