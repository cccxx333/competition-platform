<script lang="ts" setup>
import { listMyTeacherApplicationPage, type TeacherApplicationListItem, type TeacherApplicationSkill } from "@/api/teacherApplications"
import { listSkills, type Skill } from "@/api/skills"
import StatusPill from "@@/components/StatusPill/index.vue"

const loading = ref(false)
const items = ref<TeacherApplicationListItem[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const selectedApplication = ref<TeacherApplicationListItem | null>(null)
const skillNameById = ref<Record<number, string>>({})
const skillsLoaded = ref(false)
const skillsLoading = ref(false)

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

const openDetailDialog = (row: TeacherApplicationListItem) => {
  selectedApplication.value = row
  detailDialogVisible.value = true
  loadSkillDict()
}

const closeDetailDialog = () => {
  detailDialogVisible.value = false
  selectedApplication.value = null
}

const loadSkillDict = async () => {
  if (skillsLoaded.value || skillsLoading.value) return
  skillsLoading.value = true
  try {
    const data = await listSkills()
    const map: Record<number, string> = {}
    data.forEach((skill: Skill) => {
      if (typeof skill.id === "number" && skill.name) {
        map[skill.id] = skill.name
      }
    })
    skillNameById.value = map
    skillsLoaded.value = true
  } catch (error) {
    skillsLoaded.value = true
  } finally {
    skillsLoading.value = false
  }
}

const formatSkills = (skills?: TeacherApplicationSkill[]) => {
  if (!skills || !skills.length) return "无"
  return skills.map(skill => {
    const id = skill.skillId
    const weight = skill.weight ?? 1
    const name = typeof id === "number" ? (skillNameById.value[id] || `技能ID:${id}`) : "技能ID:-"
    return `${name}（权重：${weight}）`
  }).join("、")
}

onMounted(fetchList)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>我的教师申请</h2>
    </div>

    <el-table :data="items" style="width: 100%">
      <el-table-column prop="competitionName" label="竞赛" min-width="200" />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <StatusPill :value="row.status" kind="teacherApplication" />
        </template>
      </el-table-column>
      <el-table-column label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="审核时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="原因" min-width="200">
        <template #default="{ row }">
          <el-tooltip v-if="getReason(row) !== '-'" :content="getReason(row)" placement="top">
            <span class="truncate">{{ getReason(row) }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="详情" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="openDetailDialog(row)">查看</el-button>
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

  <el-dialog v-model="detailDialogVisible" title="申请详情" width="520px" @close="closeDetailDialog">
    <div class="detail-row">
      <div class="detail-label">队伍所需技能</div>
      <div class="detail-value">{{ formatSkills(selectedApplication?.skills) }}</div>
    </div>
    <div class="detail-row">
      <div class="detail-label">队伍说明</div>
      <div class="detail-value">{{ selectedApplication?.teamDescription || "无" }}</div>
    </div>
    <template #footer>
      <el-button type="primary" @click="closeDetailDialog">关闭</el-button>
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

.detail-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-label {
  width: 120px;
  color: var(--el-text-color-secondary);
}

.detail-value {
  flex: 1;
  word-break: break-word;
}
</style>
