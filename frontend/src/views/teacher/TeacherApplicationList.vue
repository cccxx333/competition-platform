<script lang="ts" setup>
import { listMyTeacherApplicationPage, type TeacherApplicationListItem } from "@/api/teacherApplications"
import StatusPill from "@@/components/StatusPill/index.vue"

const loading = ref(false)
const items = ref<TeacherApplicationListItem[]>([])
const total = ref(0)

const pagination = reactive({
  page: 0,
  size: 10
})

const formatDateTime = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const getReason = (item: TeacherApplicationListItem) => {
  if (item.status === "PENDING") return "-"
  return item.reviewComment?.trim() || "-"
}

const fetchList = async () => {
  loading.value = true
  try {
    const { items: data, total: totalElements, page, size } = await listMyTeacherApplicationPage({
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
  } catch (error) {
    console.error(error)
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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

onMounted(fetchList)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>My Teacher Applications</h2>
    </div>

    <el-table :data="items" style="width: 100%">
      <el-table-column prop="competitionName" label="Competition" min-width="200" />
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <StatusPill :value="row.status" kind="teacherApplication" />
        </template>
      </el-table-column>
      <el-table-column label="Applied At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Reviewed At" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="Reason" min-width="200">
        <template #default="{ row }">
          <el-tooltip v-if="getReason(row) !== '-'" :content="getReason(row)" placement="top">
            <span class="truncate">{{ getReason(row) }}</span>
          </el-tooltip>
          <span v-else>-</span>
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
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.truncate {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
</style>
