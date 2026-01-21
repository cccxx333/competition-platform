<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { client } from "@/api/client"
import { getTeamDetail, type TeamDto } from "@/api/teams"
import { getSubmissionDownloadUrl, listSubmissions, uploadSubmission, type TeamSubmission } from "@/api/submissions"
import { getApiErrorMessage } from "@/utils/errorMessage"
import { getTeamWriteBlockReason, isDisbanded } from "@/utils/teamGuards"

const route = useRoute()
const router = useRouter()

const team = ref<TeamDto | null>(null)
const submissions = ref<TeamSubmission[]>([])
const loading = ref(false)
const uploadLoading = ref(false)
const selectedFile = ref<File | null>(null)
const remark = ref("")
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)

const teamId = computed(() => {
  const raw = Number(route.params.teamId)
  return Number.isNaN(raw) ? null : raw
})
const returnPath = computed(() => (teamId.value ? `/teams/${teamId.value}` : "/teams/lookup"))
const isTeamDisbanded = computed(() => isDisbanded(team.value))
const writeBlockReason = computed(() => getTeamWriteBlockReason(team.value))

const currentSubmissionId = computed(() => {
  const explicit = submissions.value.find((item) => item.isCurrent)
  if (explicit?.id) return explicit.id
  return submissions.value[0]?.id ?? null
})

const hasCurrentMarker = computed(() => Boolean(submissions.value.find((item) => item.isCurrent)))

const formatDateTime = (value?: string | null) => {
  if (!value) return "-"
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "参数错误"
  if (status === 401) return "登录已过期，请重新登录"
  if (status === 403) return "无权限访问"
  if (status === 404) return "队伍不存在或已删除"
  if (status === 409) return "操作冲突，请稍后重试"
  return "服务异常，请稍后重试"
}

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const fallbackMessage = status ? getFallbackMessage(status) : fallback
  const message = getApiErrorMessage(error, fallbackMessage)
  errorDialogMessage.value = message
  errorDialogVisible.value = true
  redirectAfterError.value = null
  return message
}

const handleDisbandedRedirect = () => {
  errorDialogMessage.value = "队伍已解散，操作已禁止"
  errorDialogVisible.value = true
  redirectAfterError.value = returnPath.value
}

const loadSubmissions = async () => {
  if (!teamId.value) {
    errorDialogMessage.value = "缺少 teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    team.value = await getTeamDetail(teamId.value)
    submissions.value = await listSubmissions(teamId.value)
  } catch (error: any) {
    submissions.value = []
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "加载提交记录失败")
    }
  } finally {
    loading.value = false
  }
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const files = input?.files
  selectedFile.value = files && files.length ? files[0] : null
}

const clearFile = () => {
  selectedFile.value = null
}

const copyLink = async (item: TeamSubmission) => {
  const url = getSubmissionDownloadUrl(item)
  if (!url) {
    ElMessage.warning("文件链接不可用")
    return
  }
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success("链接已复制")
  } catch (error) {
    ElMessage.error("复制失败，请手动复制")
  }
}

const downloadFile = async (item: TeamSubmission) => {
  if (!item.fileUrl) {
    ElMessage.warning("文件链接不可用")
    return
  }
  try {
    const response = await client.get(item.fileUrl, { baseURL: "", responseType: "blob" })
    const blob = response?.data ? new Blob([response.data]) : null
    if (!blob) {
      throw new Error("empty response")
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = item.fileName || "download"
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (error: any) {
    const status = error?.response?.status
    if (status === 401 || status === 403) {
      errorDialogMessage.value = "无权限或登录已过期，无法下载文件"
    } else if (status === 404) {
      errorDialogMessage.value = "文件不存在或已被删除"
    } else {
      const message = getApiErrorMessage(error, "下载失败")
      errorDialogMessage.value = `下载失败：${message}`
    }
    errorDialogVisible.value = true
    redirectAfterError.value = null
  }
}

const submitUpload = async () => {
  if (!teamId.value) return
  if (!selectedFile.value) {
    ElMessage.warning("请先选择文件")
    return
  }
  if (isTeamDisbanded.value) {
    handleDisbandedRedirect()
    return
  }
  uploadLoading.value = true
  try {
    await uploadSubmission({ teamId: teamId.value, file: selectedFile.value, remark: remark.value })
    ElMessage.success("提交成功")
    clearFile()
    remark.value = ""
    await loadSubmissions()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "上传文件失败")
    }
  } finally {
    uploadLoading.value = false
  }
}

const onCloseErrorDialog = () => {
  errorDialogVisible.value = false
  redirectAfterError.value = null
}

onMounted(loadSubmissions)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>文件提交</h2>
        <div class="page-subtitle">队伍 ID：{{ teamId ?? "-" }}</div>
      </div>
      <div class="page-actions">
        <el-button @click="router.push(returnPath)">返回队伍详情</el-button>
      </div>
    </div>

    <el-alert
      v-if="writeBlockReason"
      type="warning"
      show-icon
      :title="writeBlockReason"
      class="status-alert"
    />

    <div class="upload-panel">
      <div class="upload-title">上传新文件</div>
      <div class="upload-row">
        <input type="file" @change="handleFileChange" :disabled="isTeamDisbanded" />
        <span v-if="selectedFile" class="file-name">{{ selectedFile.name }}</span>
      </div>
      <el-input
        v-model="remark"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="备注（可选）"
        :disabled="isTeamDisbanded"
      />
      <div class="upload-actions">
        <el-button type="primary" :loading="uploadLoading" :disabled="!selectedFile || isTeamDisbanded" @click="submitUpload">
          提交
        </el-button>
        <el-button :disabled="!selectedFile || isTeamDisbanded" @click="clearFile">清空</el-button>
      </div>
    </div>

    <el-divider />

    <div class="history-title">历史记录</div>
    <el-table v-if="submissions.length" :data="submissions" style="width: 100%">
      <el-table-column label="当前版本" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.id && row.id === currentSubmissionId" type="success">
            {{ hasCurrentMarker ? "当前" : "最新" }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="文件名">
        <template #default="{ row }">
          {{ row.fileName || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="备注" width="200">
        <template #default="{ row }">
          {{ row.remark || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="提交人" width="120">
        <template #default="{ row }">
          {{ row.submitterUsername || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.submittedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="downloadFile(row)">下载</el-button>
          <el-button size="small" title="需登录态访问" @click="copyLink(row)">复制链接</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!loading" description="暂无提交记录" />
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="提示" width="420px">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button v-if="redirectAfterError" @click="router.push(redirectAfterError)">返回</el-button>
      <el-button type="primary" @click="onCloseErrorDialog">确定</el-button>
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

.page-subtitle {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.page-actions {
  display: inline-flex;
  gap: 8px;
}

.status-alert {
  margin-bottom: 12px;
}

.upload-panel {
  display: grid;
  gap: 12px;
}

.upload-title {
  font-weight: 600;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-name {
  color: #6b7280;
  font-size: 12px;
}

.upload-actions {
  display: inline-flex;
  gap: 8px;
}

.history-title {
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
