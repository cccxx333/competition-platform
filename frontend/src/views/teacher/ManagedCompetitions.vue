<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { listCompetitions, type CompetitionListItem } from "@/api/competitions"
import StatusTag from "@@/components/StatusTag/index.vue"
import { useAuthStore } from "@/stores/auth"

type StatusFilterValue = "" | "UPCOMING" | "ONGOING" | "FINISHED"

const authStore = useAuthStore()
const loading = ref(false)
const items = ref<CompetitionListItem[]>([])
const total = ref(0)
const errorMessage = ref("")

const filters = reactive({
  keyword: "",
  status: "" as StatusFilterValue
})

const pagination = reactive({
  page: 0,
  size: 10
})

const statusOptions: Array<{ label: string; value: StatusFilterValue }> = [
  { label: "全部状态", value: "" },
  { label: "未开始", value: "UPCOMING" },
  { label: "进行中", value: "ONGOING" },
  { label: "已结束", value: "FINISHED" }
]

const formatDate = (value?: string | null) => value || "-"

const formatDateRange = (startDate?: string | null, endDate?: string | null) => {
  const start = formatDate(startDate)
  const end = formatDate(endDate)
  if (start === "-" && end === "-") return "-"
  return `${start} ~ ${end}`
}

const fetchList = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const { items: data, total: totalElements, page, size } = await listCompetitions({
      managedOnly: true,
      keyword: filters.keyword.trim() || undefined,
      status: filters.status || undefined,
      page: pagination.page,
      size: pagination.size
    })
    items.value = data
    total.value = typeof totalElements === "number" ? totalElements : 0
    if (typeof page === "number" && page !== pagination.page) {
      pagination.page = page
    }
    if (typeof size === "number" && size !== pagination.size) {
      pagination.size = size
    }
  } catch (error: any) {
    items.value = []
    total.value = 0
    errorMessage.value = error?.message || "加载负责竞赛列表失败"
    ElMessage.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 0
  fetchList()
}

const handleReset = () => {
  filters.keyword = ""
  filters.status = ""
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

onMounted(async () => {
  if (!authStore.user?.id) {
    try {
      await authStore.loadMe()
    } catch (error) {
      // Ignore here and let fetchList show empty/error state.
    }
  }
  fetchList()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>负责竞赛</h2>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="请输入竞赛名"
          clearable
          class="filter-keyword"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="全部状态" clearable class="filter-status">
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-button type="primary" class="search-btn" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-alert v-if="errorMessage" type="error" :title="errorMessage" show-icon :closable="false" class="table-alert" />

      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" label="竞赛名" min-width="220" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="competition" />
          </template>
        </el-table-column>
        <el-table-column label="报名截止时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.registrationDeadline) }}
          </template>
        </el-table-column>
        <el-table-column label="比赛起止时间" min-width="230">
          <template #default="{ row }">
            {{ formatDateRange(row.startDate, row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="organizer" label="主办方" min-width="200">
          <template #default="{ row }">
            {{ row.organizer || "-" }}
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
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.filter-card {
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-keyword {
  width: 280px;
}

.filter-status {
  width: 180px;
}

.search-btn {
  --el-button-bg-color: #111111;
  --el-button-border-color: #111111;
  --el-button-hover-bg-color: #000000;
  --el-button-hover-border-color: #000000;
  --el-button-active-bg-color: #000000;
  --el-button-active-border-color: #000000;
}

.table-alert {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
