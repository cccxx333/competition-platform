<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem } from "@/api/competitions"

const router = useRouter()

const items = ref<CompetitionListItem[]>([])
const loading = ref(false)
const errorMessage = ref("")
const total = ref<number | null>(null)

const filters = reactive({
  keyword: "",
  status: "" as CompetitionListItem["status"] | ""
})

const pagination = reactive({
  page: 1,
  size: 10
})

const statusOptions: Array<{ label: string; value: CompetitionListItem["status"] }> = [
  { label: "UPCOMING", value: "UPCOMING" },
  { label: "ONGOING", value: "ONGOING" },
  { label: "FINISHED", value: "FINISHED" }
]

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

const fetchList = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const { items: data, total: totalElements } = await listCompetitions({
      keyword: filters.keyword || undefined,
      status: (filters.status as CompetitionListItem["status"]) || undefined,
      page: pagination.page - 1,
      size: pagination.size
    })
    items.value = data
    total.value = typeof totalElements === "number" ? totalElements : null
  } catch (error: any) {
    items.value = []
    total.value = null
    errorMessage.value = showRequestError(error, "Failed to load competitions")
  } finally {
    loading.value = false
  }
}

let keywordTimer: number | undefined

const resetFilters = () => {
  filters.keyword = ""
  filters.status = ""
  pagination.page = 1
  fetchList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

const goDetail = (row: CompetitionListItem) => {
  if (!row?.id) return
  router.push(`/competitions/${row.id}`)
}

const formatDateRange = (row: CompetitionListItem) => {
  const start = row.startDate
  const end = row.endDate
  if (!start && !end) return ""
  return [start, end].filter(Boolean).join(" ~ ")
}

watch(
  () => filters.keyword,
  () => {
    if (keywordTimer) {
      window.clearTimeout(keywordTimer)
    }
    keywordTimer = window.setTimeout(() => {
      pagination.page = 1
      fetchList()
    }, 400)
  }
)

watch(
  () => filters.status,
  () => {
    pagination.page = 1
    fetchList()
  }
)

onMounted(fetchList)

onBeforeUnmount(() => {
  if (keywordTimer) {
    window.clearTimeout(keywordTimer)
  }
})
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>Competitions</h2>
    </div>

    <el-form class="filter-bar" label-position="top" label-width="80px">
      <el-row :gutter="12" align="bottom">
        <el-col :span="10">
          <el-form-item label="Keyword">
            <el-input v-model="filters.keyword" clearable placeholder="keyword" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="Status">
            <el-select v-model="filters.status" clearable placeholder="status" style="min-width: 160px">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label=" " label-width="80px">
            <el-button type="primary" @click="resetFilters">Reset</el-button>
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
      <el-table-column prop="status" label="Status" width="140" />
      <el-table-column label="Date Range" min-width="200">
        <template #default="{ row }">
          {{ formatDateRange(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="organizer" label="Organizer" min-width="180" />
      <el-table-column label="Action" width="120">
        <template #default="{ row }">
          <el-button size="small" @click.stop="goDetail(row)">Detail</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && !errorMessage && items.length === 0" class="empty-state">暂无竞赛</div>

    <div v-if="total !== null" class="pagination">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next, sizes"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
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

.filter-bar {
  margin-bottom: 8px;
}

.empty-state {
  padding: 16px 0;
  color: #6b7280;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
