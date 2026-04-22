<script lang="ts" setup>
import { ElMessage } from "element-plus"
import StatusTag from "@@/components/StatusTag/index.vue"
import {
  getAdminUser,
  listAdminUsersPage,
  updateAdminUser,
  type AdminUserProfile,
  type ApprovalStatus,
  type UserRole
} from "@/api/adminUsers"

const loading = ref(false)
const items = ref<AdminUserProfile[]>([])
const total = ref(0)

const filters = reactive({
  keyword: "",
  role: "" as UserRole | "",
  approvalStatus: "" as string
})

const pagination = reactive({
  page: 0,
  size: 10
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailUser = ref<AdminUserProfile | null>(null)

const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editTargetUserId = ref<number | null>(null)
const editForm = reactive({
  username: "",
  displayName: "",
  email: "",
  realName: "",
  phone: "",
  school: "",
  major: "",
  grade: "",
  userStatus: "" as ApprovalStatus | ""
})

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
  }
  if (status === 400) return "请求无效"
  if (status === 403) return "无权限"
  if (status === 404) return "用户不存在"
  if (status === 409) return "用户名/邮箱冲突，或用户状态设置不允许"
  return fallback
}

const fetchList = async () => {
  loading.value = true
  try {
    const { items: data, total: totalElements, page, size } = await listAdminUsersPage({
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword.trim() || undefined,
      role: filters.role || undefined,
      approvalStatus: filters.approvalStatus || undefined
    })
    items.value = data
    total.value = typeof totalElements === "number" ? totalElements : 0
    if (typeof page === "number") pagination.page = page
    if (typeof size === "number") pagination.size = size
  } catch (error) {
    items.value = []
    total.value = 0
    ElMessage.error(showRequestError(error, "加载用户失败"))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 0
  fetchList()
}

const resetFilters = () => {
  filters.keyword = ""
  filters.role = ""
  filters.approvalStatus = ""
  pagination.page = 0
  fetchList()
}

const handlePageChange = (page: number) => {
  pagination.page = page - 1
  fetchList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 0
  fetchList()
}

const openDetail = async (row: AdminUserProfile) => {
  if (!row.id) return
  detailDialogVisible.value = true
  detailLoading.value = true
  detailUser.value = null
  try {
    detailUser.value = await getAdminUser(row.id)
  } catch (error) {
    ElMessage.error(showRequestError(error, "加载用户详情失败"))
  } finally {
    detailLoading.value = false
  }
}

const openEdit = async (row: AdminUserProfile) => {
  if (!row.id) return
  editDialogVisible.value = true
  editSubmitting.value = false
  editTargetUserId.value = row.id
  try {
    const data = await getAdminUser(row.id)
    editForm.username = String(data.username ?? "")
    editForm.displayName = String(data.displayName ?? "")
    editForm.email = String(data.email ?? "")
    editForm.realName = String(data.realName ?? "")
    editForm.phone = String(data.phone ?? "")
    editForm.school = String(data.school ?? "")
    editForm.major = String(data.major ?? "")
    editForm.grade = String(data.grade ?? "")
    editForm.userStatus =
      data.approvalStatus === "APPROVED" || data.approvalStatus === "REJECTED" ? data.approvalStatus : ""
  } catch (error) {
    ElMessage.error(showRequestError(error, "加载用户详情失败"))
    editDialogVisible.value = false
    editTargetUserId.value = null
  }
}

const submitEdit = async () => {
  if (!editTargetUserId.value || editSubmitting.value) return
  if (!editForm.username.trim()) {
    ElMessage.warning("用户名不能为空")
    return
  }
  if (!editForm.email.trim()) {
    ElMessage.warning("邮箱不能为空")
    return
  }

  editSubmitting.value = true
  try {
    await updateAdminUser(editTargetUserId.value, {
      username: editForm.username.trim(),
      displayName: editForm.displayName.trim() || undefined,
      email: editForm.email.trim(),
      realName: editForm.realName.trim() || undefined,
      phone: editForm.phone.trim() || undefined,
      school: editForm.school.trim() || undefined,
      major: editForm.major.trim() || undefined,
      grade: editForm.grade.trim() || undefined,
      approvalStatus: editForm.userStatus || undefined
    })
    ElMessage.success("用户信息已更新")
    editDialogVisible.value = false
    await fetchList()
  } catch (error) {
    ElMessage.error(showRequestError(error, "更新用户失败"))
  } finally {
    editSubmitting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <div class="page-header user-header">
      <h2>用户管理</h2>
    </div>

    <el-card shadow="never" class="user-card" v-loading="loading">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="按用户名/姓名/邮箱搜索"
          clearable
          style="width: 260px"
        />
        <el-select v-model="filters.role" clearable placeholder="角色" style="width: 140px">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
        <el-select v-model="filters.approvalStatus" clearable placeholder="用户状态" style="width: 140px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button class="filter-reset" @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="items" style="width: 100%; margin-top: 12px" table-layout="fixed">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="role" label="角色" width="110" />
        <el-table-column label="用户状态" width="130">
          <template #default="{ row }">
            <StatusTag :status="row.approvalStatus" kind="teacherApplication" />
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.page + 1"
          :page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="用户详情" width="640px">
      <el-skeleton :loading="detailLoading" animated>
        <template #template>
          <el-skeleton-item variant="text" style="width: 70%; margin-bottom: 10px" />
          <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 10px" />
          <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 10px" />
        </template>
        <template #default>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="ID">{{ detailUser?.id ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ detailUser?.username ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ detailUser?.role ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="用户状态">
              <StatusTag :status="detailUser?.approvalStatus" kind="teacherApplication" />
            </el-descriptions-item>
            <el-descriptions-item label="展示名">{{ detailUser?.displayName ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ detailUser?.realName ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detailUser?.email ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ detailUser?.phone ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ detailUser?.school ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ detailUser?.major ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ detailUser?.grade ?? "-" }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-skeleton>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="760px">
      <el-form label-position="top" class="edit-form-grid">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="展示名">
          <el-input v-model="editForm.displayName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="editForm.school" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editForm.grade" />
        </el-form-item>
        <el-form-item label="用户状态" class="edit-form-full">
          <el-select v-model="editForm.userStatus" clearable placeholder="保持不变" style="width: 100%">
            <el-option label="已通过（账号可用）" value="APPROVED" />
            <el-option label="已拒绝（账号不可用）" value="REJECTED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="editSubmitting" @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-header {
  max-width: 1000px;
  margin: 0 auto 12px;
}

.user-card {
  max-width: 1000px;
  margin: 0 auto;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-reset {
  margin-left: auto;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.edit-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 6px;
}

.edit-form-full {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .edit-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
