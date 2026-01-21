<script lang="ts" setup>
import { getMyHonors, type AwardDetail, type UserHonorsResponse } from "@/api/honors"
import { getApiErrorMessage } from "@/utils/errorMessage"

const loading = ref(false)
const honors = ref<UserHonorsResponse | null>(null)
const errorMessage = ref("")

const awards = computed<AwardDetail[]>(() => honors.value?.awards ?? [])
const showCounts = computed(() => {
  const data = honors.value
  return data?.participationCount != null || data?.awardCount != null
})

const formatDateTime = (value?: string | null) => {
  if (!value) return ""
  if (value.includes("T")) {
    const [date, time] = value.split("T")
    return `${date} ${time.slice(0, 5)}`
  }
  return value
}

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "Invalid request"
  if (status === 401) return "Session expired, please log in again"
  if (status === 403) return "No permission to view honors"
  if (status === 404) return "Honors not found"
  if (status === 409) return "Request conflict"
  return "Failed to load honors"
}

const loadHonors = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    honors.value = await getMyHonors()
  } catch (error: any) {
    const status = error?.status ?? error?.response?.status
    errorMessage.value = getApiErrorMessage(error, getFallbackMessage(status))
    honors.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadHonors)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <div class="page-header">
      <h2>My Honors</h2>
      <el-button size="small" :loading="loading" @click="loadHonors">Refresh</el-button>
    </div>

    <el-alert v-if="errorMessage" type="error" show-icon :title="errorMessage" class="status-alert" />

    <el-card v-if="showCounts" shadow="never" class="section">
      <h3>Summary</h3>
      <el-descriptions :column="1">
        <el-descriptions-item v-if="honors?.participationCount != null" label="Participation Count">
          {{ honors?.participationCount }}
        </el-descriptions-item>
        <el-descriptions-item v-if="honors?.awardCount != null" label="Award Count">
          {{ honors?.awardCount }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="section">
      <h3>Awards</h3>
      <el-table v-if="awards.length" :data="awards" style="width: 100%">
        <el-table-column prop="awardName" label="Award" min-width="160" />
        <el-table-column prop="competitionId" label="Competition ID" width="140" />
        <el-table-column prop="teamId" label="Team ID" width="120" />
        <el-table-column label="Published At" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishedAt) || "-" }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-else-if="!loading && !errorMessage"
        description="No honors yet"
      />
    </el-card>
  </el-card>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-alert {
  margin-bottom: 12px;
}

.section {
  margin-bottom: 12px;
}
</style>
