<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { deleteTeamPost, listTeamPosts, replyTeamPost, type TeamDiscussionPost } from "@/api/discussions"
import { useAuthStore } from "@/stores/auth"
import { getApiErrorMessage } from "@/utils/errorMessage"
import { getTeamWriteBlockReason } from "@/utils/teamGuards"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const posts = ref<TeamDiscussionPost[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const deleteLoadingId = ref<number | null>(null)
const deleteDialogVisible = ref(false)
const pendingDeleteId = ref<number | null>(null)
const pendingDeleteType = ref<"root" | "reply" | null>(null)
const pendingDeleteLoading = ref(false)
const replyContent = ref("")
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const redirectAfterError = ref<string | null>(null)
const isTeamDisbanded = ref(false)

const teamId = computed(() => {
  const raw = Number(route.params.teamId)
  return Number.isNaN(raw) ? null : raw
})

const postId = computed(() => {
  const raw = Number(route.params.postId)
  return Number.isNaN(raw) ? null : raw
})

const currentUserId = computed(() => authStore.user?.id ?? null)
const returnPath = computed(() => (teamId.value ? `/teams/${teamId.value}/posts` : "/teams/lookup"))
const canWrite = computed(() => !isTeamDisbanded.value)
const writeBlockReason = computed(() =>
  getTeamWriteBlockReason(isTeamDisbanded.value ? { status: "DISBANDED" } : null)
)

const rootPost = computed(() => {
  if (!postId.value) return null
  return posts.value.find((post) => post.id === postId.value) ?? null
})

const replies = computed(() => {
  if (!postId.value) return []
  return posts.value.filter((post) => post.parentPostId === postId.value)
})

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
  if (status === 404) return "帖子不存在或已删除"
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
  redirectAfterError.value = teamId.value ? `/teams/${teamId.value}` : "/teams/lookup"
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
      showRequestError(error, "Failed to load post thread")
    }
  } finally {
    loading.value = false
  }
}

const submitReply = async () => {
  if (!teamId.value || !postId.value) return
  const trimmed = replyContent.value.trim()
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
    await replyTeamPost(teamId.value, { content: trimmed, parentPostId: postId.value })
    ElMessage.success("已回复")
    replyContent.value = ""
    await loadPosts()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to reply post")
    }
  } finally {
    submitLoading.value = false
  }
}

const canDeletePost = (post: TeamDiscussionPost) => {
  if (!currentUserId.value || !post.authorId) return false
  return post.authorId === currentUserId.value
}

const openDeleteDialog = (post: TeamDiscussionPost, type: "root" | "reply") => {
  if (!post.id) return
  pendingDeleteId.value = post.id
  pendingDeleteType.value = type
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!teamId.value || !pendingDeleteId.value) return
  pendingDeleteLoading.value = true
  deleteLoadingId.value = pendingDeleteId.value
  try {
    await deleteTeamPost(teamId.value, pendingDeleteId.value)
    ElMessage.success("???")
    deleteDialogVisible.value = false
    pendingDeleteId.value = null
    pendingDeleteType.value = null
    await loadPosts()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    const message = error?.message ?? ""
    if (status === 409 && (error?.isDisbanded || message.includes("disbanded"))) {
      deleteDialogVisible.value = false
      pendingDeleteId.value = null
      pendingDeleteType.value = null
      handleDisbandedRedirect()
    } else {
      showRequestError(error, "Failed to delete post")
    }
  } finally {
    pendingDeleteLoading.value = false
    deleteLoadingId.value = null
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
        <h2>帖子线程</h2>
        <div class="page-subtitle">Post ID: {{ postId ?? "-" }}</div>
      </div>
      <div class="page-actions">
        <el-button @click="router.push(returnPath)">返回帖子列表</el-button>
      </div>
    </div>

    <el-alert
      v-if="writeBlockReason"
      type="warning"
      show-icon
      :title="writeBlockReason"
      class="status-alert"
    />

    <template v-if="rootPost">
      <div class="post-block">
        <div class="post-meta">创建时间：{{ formatDateTime(rootPost.createdAt) }}</div>
        <div class="post-content">{{ rootPost.content || "-" }}</div>
        <div class="post-actions">
          <el-button
            v-if="canDeletePost(rootPost)"
            size="small"
            type="danger"
            :loading="deleteLoadingId === rootPost.id"
            :disabled="!canWrite"
            @click="openDeleteDialog(rootPost, 'root')"
          >
            删除主帖
          </el-button>
        </div>
      </div>

      <el-divider />

      <div class="section-title">回复列表</div>
      <el-table v-if="replies.length" :data="replies" style="width: 100%">
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
            <el-button
              v-if="canDeletePost(row)"
              size="small"
              type="danger"
              :loading="deleteLoadingId === row.id"
              :disabled="!canWrite"
              @click="openDeleteDialog(row, 'reply')"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="暂无回复" />

      <div class="reply-form">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          maxlength="2000"
          show-word-limit
          placeholder="回复该帖子"
          :disabled="!canWrite"
        />
        <div class="reply-form__actions">
          <el-button type="primary" :loading="submitLoading" :disabled="!canWrite" @click="submitReply">
            回复
          </el-button>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="帖子不存在或已删除">
      <el-button @click="router.push(returnPath)">返回帖子列表</el-button>
    </el-empty>
  </el-card>

    <el-dialog
    v-model="deleteDialogVisible"
    title="删除确认"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    destroy-on-close
    append-to-body
    teleported
  >
    <div>确认删除{{ pendingDeleteType === "root" ? "主贴" : pendingDeleteType === "reply" ? "回复" : "内容" }}吗？删除后不可恢复</div>
    <template #footer>
      <el-button :disabled="pendingDeleteLoading" @click="deleteDialogVisible = false; pendingDeleteId = null; pendingDeleteType = null">取消</el-button>
      <el-button type="danger" :loading="pendingDeleteLoading" @click="confirmDelete">删除</el-button>
    </template>
  </el-dialog>

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

.post-block {
  display: grid;
  gap: 8px;
}

.post-meta {
  color: #6b7280;
  font-size: 12px;
}

.post-actions {
  display: flex;
  justify-content: flex-end;
}

.post-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.section-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.reply-form {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.reply-form__actions {
  display: flex;
  justify-content: flex-end;
}
</style>
