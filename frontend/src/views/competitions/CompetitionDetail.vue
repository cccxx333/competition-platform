<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { ArrowLeft } from "@element-plus/icons-vue"
import { getCompetitionDetail, updateCompetitionStatus, type CompetitionDetail } from "@/api/competitions"
import { useAuthStore } from "@/stores/auth"
import StatusTag from "@@/components/StatusTag/index.vue"
import { getApiErrorMessage } from "@/utils/errorMessage"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref("")
const detail = ref<CompetitionDetail | null>(null)
const editDialogVisible = ref(false)
const editDialogLoading = ref(false)
const editDialogError = ref("")
const editForm = ref({
  status: "" as CompetitionDetail["status"] | ""
})

const competitionId = computed(() => Number(route.params.id))
const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
const isAdmin = computed(() => roleUpper.value === "ADMIN")

const statusOptions = [
  { label: "未开始", value: "UPCOMING" },
  { label: "进行中", value: "ONGOING" },
  { label: "已结束", value: "FINISHED" }
]

const formatDate = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) return value.split("T")[0]
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

const registrationDeadlineText = computed(() => {
  const data = detail.value
  if (!data) return ""
  return formatDate(data.registrationDeadline)
})

const timeFields = computed(() => {
  const data = detail.value
  if (!data) return []
  return [
    { label: "开始日期", value: formatDate(data.startDate) },
    { label: "结束日期", value: formatDate(data.endDate) }
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

const backTarget = computed(() => {
  const raw = route.query.back
  if (typeof raw !== "string" || !raw.trim()) return "/competitions"
  try {
    const decoded = decodeURIComponent(raw)
    return decoded.startsWith("/competitions") ? decoded : "/competitions"
  } catch {
    return "/competitions"
  }
})

const handleBack = () => {
  router.push(backTarget.value)
}

const openEditDialog = () => {
  if (!detail.value) return
  editDialogError.value = ""
  editForm.value = {
    status: detail.value.status ?? ""
  }
  editDialogVisible.value = true
}

const closeEditDialog = () => {
  if (editDialogLoading.value) return
  editDialogVisible.value = false
  editDialogError.value = ""
}

const submitEdit = async () => {
  if (!detail.value?.id) return
  editDialogError.value = ""
  if (!editForm.value.status) {
    editDialogError.value = "请选择竞赛状态"
    return
  }
  editDialogLoading.value = true
  try {
    await updateCompetitionStatus(detail.value.id, editForm.value.status)
    ElMessage.success("竞赛状态已更新")
    editDialogVisible.value = false
    await loadDetail()
  } catch (error: any) {
    editDialogError.value = getApiErrorMessage(error, "更新竞赛失败")
  } finally {
    editDialogLoading.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>竞赛详情</h2>
      <div class="header-actions">
        <el-button v-if="isAdmin" size="small" type="primary" @click="openEditDialog">修改竞赛状态</el-button>
        <el-button class="back-btn" type="default" plain :icon="ArrowLeft" @click="handleBack">返回</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
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
              <StatusTag :status="detail.status" kind="competition" />
            </el-descriptions-item>
            <el-descriptions-item v-if="registrationDeadlineText" label="报名截止时间">
              {{ registrationDeadlineText }}
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
        v-model="editDialogVisible"
        title="修改竞赛状态"
        width="520px"
        append-to-body
        top="12vh"
        :close-on-click-modal="false"
        :before-close="closeEditDialog"
      >
        <el-form label-position="top">
          <el-form-item label="状态">
            <el-select v-model="editForm.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-alert v-if="editDialogError" type="error" :closable="false" :title="editDialogError" />
        <template #footer>
          <el-button :disabled="editDialogLoading" @click="closeEditDialog">取消</el-button>
          <el-button type="primary" :loading="editDialogLoading" @click="submitEdit">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
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
  align-items: center;
}

.back-btn {
  height: 32px;
  padding: 0 10px;
  border-color: #d0d5dd;
  color: #344054;
}

.section {
  margin-bottom: 12px;
}
</style>

