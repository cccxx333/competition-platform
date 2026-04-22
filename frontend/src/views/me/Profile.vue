<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { Avatar, Edit, Message, Postcard, PriceTag, User } from "@element-plus/icons-vue"
import { getMyProfile, updateMyProfile, type UserProfile } from "@/api/profile"

const loading = ref(false)
const submitting = ref(false)
const profile = ref<UserProfile | null>(null)
const editDialogVisible = ref(false)
const editForm = reactive({
  username: "",
  displayName: "",
  email: ""
})

const roleUpper = computed(() => String(profile.value?.role ?? "").toUpperCase())
const accountLabel = computed(() => (roleUpper.value === "STUDENT" ? "学号" : "工号"))
const accountNo = computed(() => profile.value?.accountNo ?? profile.value?.username ?? "-")

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return
  }
  if (status === 400) {
    ElMessage.error("请求参数错误")
    return
  }
  if (status === 403) {
    ElMessage.error("无权限访问")
    return
  }
  if (status === 404) {
    ElMessage.error("资源不存在")
    return
  }
  if (status === 409) {
    ElMessage.error("用户名或邮箱已存在")
    return
  }
  ElMessage.error(fallback)
}

const infoRows = computed(() => {
  const data = profile.value
  return [
    { label: accountLabel.value, value: accountNo.value, icon: Postcard },
    { label: "用户名", value: data?.username ?? "-", icon: User },
    { label: "展示名", value: data?.displayName ?? "-", icon: Avatar },
    { label: "邮箱", value: data?.email ?? "-", icon: Message },
    { label: "角色", value: data?.role ?? "-", icon: PriceTag }
  ]
})

const openEditDialog = () => {
  editForm.username = String(profile.value?.username ?? "")
  editForm.displayName = String(profile.value?.displayName ?? "")
  editForm.email = String(profile.value?.email ?? "")
  editDialogVisible.value = true
}

const reload = async () => {
  loading.value = true
  try {
    profile.value = await getMyProfile()
  } catch (error: any) {
    showRequestError(error, "获取个人信息失败")
  } finally {
    loading.value = false
  }
}

const submitEdit = async () => {
  if (!editForm.username.trim()) {
    ElMessage.warning("用户名不能为空")
    return
  }
  if (!editForm.email.trim()) {
    ElMessage.warning("邮箱不能为空")
    return
  }
  submitting.value = true
  try {
    profile.value = await updateMyProfile({
      username: editForm.username.trim(),
      displayName: editForm.displayName.trim() || undefined,
      email: editForm.email.trim()
    })
    ElMessage.success("个人信息已更新")
    editDialogVisible.value = false
  } catch (error: any) {
    showRequestError(error, "修改个人信息失败")
  } finally {
    submitting.value = false
  }
}

onMounted(reload)
</script>

<template>
  <div class="profile-page">
    <div class="page-head">
      <h2 class="page-title">个人信息</h2>
      <el-button type="primary" :icon="Edit" @click="openEditDialog">修改</el-button>
    </div>
    <el-card class="profile-card" shadow="never" v-loading="loading">
      <div class="card-header">
        <div class="user-block">
          <div class="user-meta">
            <div class="user-name">{{ profile?.displayName || profile?.username || "-" }}</div>
          </div>
        </div>
      </div>
      <div class="info-list">
        <div v-for="row in infoRows" :key="row.label" class="info-row">
          <div class="info-icon">
            <el-icon :size="18">
              <component :is="row.icon" />
            </el-icon>
          </div>
          <div class="info-text">
            <div class="info-label">{{ row.label }}</div>
            <div class="info-value">{{ row.value }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改个人信息" width="420px">
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="展示名">
          <el-input v-model="editForm.displayName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.profile-card {
  min-height: 520px;
  background: var(--cp-card-bg);
  border: var(--cp-card-border-width) solid var(--cp-card-border-color);
  border-radius: var(--cp-radius-card);
  box-shadow: var(--cp-card-shadow);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.user-block {
  display: flex;
  align-items: center;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.info-list {
  padding: 8px 24px 12px;
}

.info-row {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.info-row:last-child {
  border-bottom: 0;
}

.info-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.04);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: #6b7280;
}

.info-value {
  font-size: 17px;
  color: #111827;
}
</style>
