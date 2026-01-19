<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getCompetitionDetail, type CompetitionDetail } from "@/api/competitions"

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref("")
const detail = ref<CompetitionDetail | null>(null)

const competitionId = computed(() => Number(route.params.id))

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

const statusTagType = (status?: CompetitionDetail["status"]) => {
  if (status === "ONGOING") return "success"
  if (status === "FINISHED") return "info"
  return "warning"
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

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Competition Detail</h2>
      <el-button size="small" @click="handleBack">Back</el-button>
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
            <el-tag :type="statusTagType(detail.status)">{{ detail.status }}</el-tag>
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
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section {
  margin-bottom: 12px;
}
</style>
