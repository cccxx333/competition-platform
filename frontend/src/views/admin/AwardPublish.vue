<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listAwardRecords, publishAward, type AwardPublishResponse, type AwardRecordItem } from "@/api/awards"
import { useAuthStore } from "@/stores/auth"
import { toYmd } from "@/common/utils/datetime"
import { getApiErrorMessage } from "@/utils/errorMessage"

const authStore = useAuthStore()
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")

const submitting = ref(false)
const loadingRecords = ref(false)
const form = reactive({
  competitionId: "",
  teamId: "",
  awardName: ""
})

const lastResult = ref<AwardPublishResponse | null>(null)
const records = ref<AwardRecordItem[]>([])
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
  if (status === 400) return "请求无效"
  if (status === 401) return "会话已过期，请重新登录"
  if (status === 403) return "无权限（仅管理员）"
  if (status === 404) return "竞赛或队伍不存在"
  if (status === 409) return "业务冲突"
  return "发布奖项失败"
}

const parseId = (value: string, label: string) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed <= 0) {
    showError(`${label} 必须为正数`)
    return null
  }
  return parsed
}

const handleSubmit = async () => {
  if (!isAdmin.value) {
    showError("无权限（仅管理员）")
    return
  }
  const competitionId = parseId(form.competitionId, "竞赛 ID")
  if (!competitionId) return
  const teamId = parseId(form.teamId, "队伍 ID")
  if (!teamId) return
  const awardName = form.awardName.trim()
  if (!awardName) {
    showError("奖项名称不能为空")
    return
  }

  submitting.value = true
  resetResult()
  try {
    const result = await publishAward({ competitionId, teamId, awardName })
    lastResult.value = result
    ElMessage.success("奖项已发布")
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    const message = getApiErrorMessage(error, fallback)
    showError(message)
  } finally {
    submitting.value = false
  }
}

const parseOptionalId = (value: string) => {
  const trimmed = value.trim()
  if (!trimmed) return undefined
  const parsed = Number(trimmed)
  if (!Number.isFinite(parsed) || parsed <= 0) return undefined
  return parsed
}

const loadRecords = async (useFilters = false) => {
  loadingRecords.value = true
  try {
    const params = useFilters
      ? {
          competitionId: parseOptionalId(form.competitionId),
          teamId: parseOptionalId(form.teamId)
        }
      : {}
    records.value = await listAwardRecords(params)
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const fallback = getFallbackMessage(status)
    const message = getApiErrorMessage(error, fallback)
    showError(message)
  } finally {
    loadingRecords.value = false
  }
}

onMounted(() => {
  loadRecords(false)
})
</script>

<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>发布奖项</h2>
    </div>

    <el-alert
      v-if="!isAdmin"
      type="warning"
      show-icon
      title="仅管理员可发布奖项。"
      class="status-alert"
    />

    <el-alert
      type="info"
      show-icon
      class="status-alert"
      title="需要手动输入，请通过竞赛列表和队伍查询获取 ID。"
    />

    <el-form label-position="top" class="publish-form">
      <el-form-item label="竞赛 ID">
        <el-input
          v-model="form.competitionId"
          placeholder="请输入竞赛 ID（已结束）"
          :disabled="submitting"
        />
      </el-form-item>
      <el-form-item label="队伍 ID">
        <el-input
          v-model="form.teamId"
          placeholder="请输入队伍 ID（已关闭）"
          :disabled="submitting"
        />
      </el-form-item>
      <el-form-item label="奖项名称">
        <el-input
          v-model="form.awardName"
          maxlength="64"
          show-word-limit
          placeholder="例如：一等奖"
          :disabled="submitting"
        />
      </el-form-item>
      <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">
        发布
      </el-button>
    </el-form>

    <el-card v-if="lastResult" shadow="never" class="result-card">
      <h3>发布结果</h3>
      <el-descriptions :column="1">
        <el-descriptions-item label="奖项 ID">{{ lastResult.awardId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="竞赛 ID">{{ lastResult.competitionId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="队伍 ID">{{ lastResult.teamId ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="奖项名称">{{ lastResult.awardName ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="获奖人数">{{ lastResult.recipientCount ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="获奖用户 ID">
          {{ lastResult.recipientUserIds?.length ? lastResult.recipientUserIds.join(", ") : "-" }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="result-hint">
        可使用获奖成员账号验证荣誉页。
      </div>
    </el-card>

    <el-card shadow="never" class="records-card">
      <div class="records-header">
        <h3>奖项记录</h3>
        <div class="records-actions">
          <el-button size="small" :loading="loadingRecords" @click="loadRecords(false)">刷新</el-button>
          <el-button size="small" :loading="loadingRecords" @click="loadRecords(true)">
            带条件刷新
          </el-button>
        </div>
      </div>
      <el-table v-if="records.length" :data="records" v-loading="loadingRecords" style="width: 100%">
        <el-table-column prop="awardId" label="奖项 ID" width="120" />
        <el-table-column prop="competitionId" label="竞赛 ID" width="140" />
        <el-table-column prop="teamId" label="队伍 ID" width="120" />
        <el-table-column prop="awardName" label="奖项名称" min-width="160" />
        <el-table-column prop="recipientCount" label="获奖人数" width="120" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ row.publishedAt ? toYmd(row.publishedAt) : "-" }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loadingRecords" description="暂无奖项记录" />
    </el-card>
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="发布失败" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="errorDialogVisible = false">确定</el-button>
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

.records-card {
  margin-top: 16px;
}

.records-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.records-actions {
  display: inline-flex;
  gap: 8px;
}

.result-hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}
</style>
