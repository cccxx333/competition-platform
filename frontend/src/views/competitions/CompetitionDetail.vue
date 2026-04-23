<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { ArrowLeft } from "@element-plus/icons-vue"
import { getCompetitionDetail, type CompetitionDetail } from "@/api/competitions"
import { listAdminUsersPage, type AdminUserProfile } from "@/api/adminUsers"
import { useAuthStore } from "@/stores/auth"
import StatusTag from "@@/components/StatusTag/index.vue"
import { getApiErrorMessage } from "@/utils/errorMessage"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref("")
const detail = ref<CompetitionDetail | null>(null)

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

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>竞赛详情</h2>
      <div class="header-actions">
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

        <el-card v-if="detail.requiredSkills && detail.requiredSkills.length" shadow="never" class="section">
          <h3>技能需求</h3>
          <div class="skills-container">
            <el-tag
              v-for="skill in detail.requiredSkills"
              :key="skill.skillId"
              :type="skill.importance && skill.importance >= 7 ? 'danger' : (skill.importance && skill.importance >= 4 ? 'warning' : 'info')"
              class="skill-tag"
              effect="light"
            >
              {{ skill.skillName }}
              <span class="skill-importance">(权重: {{ skill.importance }})</span>
            </el-tag>
          </div>
        </el-card>

        <el-card v-if="detail.description" shadow="never" class="section">
          <el-collapse>
            <el-collapse-item title="描述" name="description">
              <pre>{{ detail.description }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>


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

.skills-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.skill-tag {
  height: 32px;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.skill-importance {
  margin-left: 4px;
  font-size: 12px;
  opacity: 0.8;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  margin: 0;
  color: #606266;
}

</style>

