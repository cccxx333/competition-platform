<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { createTeamPost, listTeamPosts, type TeamDiscussionPost } from "@/api/discussions"
import { getApiErrorMessage } from "@/utils/errorMessage"
import { getTeamWriteBlockReason } from "@/utils/teamGuards"

const route = useRoute()
const router = useRouter()

const posts = ref<TeamDiscussionPost[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const content = ref("")
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)
const isTeamDisbanded = ref(false)

const teamId = computed(() => {
  const raw = Number(route.params.teamId)
  return Number.isNaN(raw) ? null : raw
})

const returnPath = computed(() => (teamId.value ? `/teams/${teamId.value}` : "/teams/lookup"))
const rootPosts = computed(() => posts.value.filter((post) => !post.parentPostId))
const canWrite = computed(() => !isTeamDisbanded.value)
const writeBlockReason = computed(() =>
  getTeamWriteBlockReason(isTeamDisbanded.value ? { status: "DISBANDED" } : null)
)

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
  if (status === 403) return "无权访问"
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
  isTeamDisbanded.value = true
  errorDialogMessage.value = "队伍已解散，操作已禁止"
  errorDialogVisible.value = true
  redirectAfterError.value = returnPath.value
}

const loadPosts = async () => {
  if (!teamId.value) {
    errorDialogMessage.value = "缺少 teamId"
    errorDialogVisible.value = true
    return
  }
  loading.value = true
  try {
    posts.value = await listTeamPosts(teamId.value)
  } catch (error: any) {
    posts.value = []
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to load posts")
    }
  } finally {
    loading.value = false
  }
}

const openThread = (postId?: number) => {
  if (!teamId.value || !postId) return
  router.push(`/teams/${teamId.value}/posts/${postId}`)
}

const submitPost = async () => {
  if (!teamId.value) return
  const trimmed = content.value.trim()
  if (!trimmed) {
    ElMessage.warning("请输入内容")
    return
  }
  if (trimmed.length > 2000) {
    ElMessage.warning("内容最多 2000 字")
    return
  }
  submitLoading.value = true
  try {
    await createTeamPost(teamId.value, { content: trimmed })
    ElMessage.success("已发布")
    content.value = ""
    await loadPosts()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to create post")
    }
  } finally {
    submitLoading.value = false
  }
}

const onCloseErrorDialog = () => {
  errorDialogVisible.value = false
  redirectAfterError.value = null
}

onMounted(loadPosts)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>队伍讨论区</h2>
        <div class="page-subtitle">Team ID: {{ teamId ?? "-" }}</div>
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

    <div class="post-form">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        maxlength="2000"
        show-word-limit
        placeholder="发布新的讨论内容"
        :disabled="!canWrite"
      />
      <div class="post-form__actions">
        <el-button type="primary" :loading="submitLoading" :disabled="!canWrite" @click="submitPost">
          发布
        </el-button>
      </div>
    </div>

    <el-table v-if="rootPosts.length" :data="rootPosts" style="width: 100%">
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="内容">
        <template #default="{ row }">
          <span class="post-content">{{ row.content || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="openThread(row.id)">查看线程</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="!loading" description="暂无帖子" />
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

.post-form {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.post-form__actions {
  display: flex;
  justify-content: flex-end;
}

.post-content {
  display: inline-block;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
