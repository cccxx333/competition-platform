<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem } from "@/api/competitions"
import StatusPill from "@@/components/StatusPill/index.vue"

const router = useRouter()

const items = ref<CompetitionListItem[]>([])
const loading = ref(false)
const errorMessage = ref("")
const topK = ref(10)

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
  }
  if (status === 400) {
    ElMessage.error("Invalid request parameters.")
    return "Invalid request parameters."
  }
  if (status === 403) {
    ElMessage.error("Access denied.")
    return "Access denied."
  }
  if (status === 404) {
    ElMessage.error("Resource not found.")
    return "Resource not found."
  }
  if (status === 409) {
    ElMessage.error("Request conflict.")
    return "Request conflict."
  }
  ElMessage.error("Service unavailable. Please try again later.")
  return "Service unavailable. Please try again later."
}

const fetchList = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const { items: data } = await listCompetitions({
      recommend: true,
      topK: topK.value,
      page: 0,
      size: topK.value
    })
    items.value = data
  } catch (error: any) {
    items.value = []
    errorMessage.value = showRequestError(error, "Failed to load recommendations")
  } finally {
    loading.value = false
  }
}

const formatDate = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    return value.split("T")[0]
  }
  return value
}

const formatDateRange = (row: CompetitionListItem) => {
  const start = formatDate(row.startDate)
  const end = formatDate(row.endDate)
  if (!start && !end) return ""
  return [start, end].filter(Boolean).join(" ~ ")
}

const formatScore = (score?: number | null) => {
  if (typeof score !== "number") return ""
  return score.toFixed(3)
}

const goDetail = (row: CompetitionListItem) => {
  if (!row?.id) return
  router.push(`/competitions/${row.id}`)
}

watch(
  () => topK.value,
  () => {
    fetchList()
  }
)

onMounted(() => {
  fetchList()
})
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>Competition Recommendations</h2>
        <p class="page-subtitle">Recommendations based on your skill profile.</p>
      </div>
      <el-button @click="router.push('/competitions')">Go to Competitions</el-button>
    </div>

    <el-form class="control-bar" label-position="top" label-width="80px">
      <el-row :gutter="12" align="bottom">
        <el-col :span="6">
          <el-form-item label="TopK">
            <el-input-number v-model="topK" :min="1" :max="50" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      style="margin-bottom: 12px"
    />

    <el-table
      :data="items"
      style="width: 100%"
      @row-click="goDetail"
      v-loading="loading"
      highlight-current-row
    >
      <el-table-column prop="name" label="Name" min-width="180" />
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <StatusPill :value="row.status" kind="competition" />
        </template>
      </el-table-column>
      <el-table-column label="Date Range" min-width="200">
        <template #default="{ row }">
          {{ formatDateRange(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="organizer" label="Organizer" min-width="180" />
      <el-table-column label="Recommendation" min-width="220">
        <template #default="{ row }">
          <div v-if="row.matchScore !== undefined || row.recommendReason">
            <div v-if="formatScore(row.matchScore)">Score: {{ formatScore(row.matchScore) }}</div>
            <div v-if="row.recommendReason">{{ row.recommendReason }}</div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="Action" width="120">
        <template #default="{ row }">
          <el-button size="small" @click.stop="goDetail(row)">Detail</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && !errorMessage && items.length === 0" class="empty-state">No recommendations yet.</div>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
}

.control-bar {
  margin-bottom: 8px;
}

.empty-state {
  padding: 16px 0;
  color: #6b7280;
}
</style>
