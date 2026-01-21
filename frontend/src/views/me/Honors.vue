<script lang="ts" setup>
import { getMyHonors, type AwardDetail, type UserHonorsResponse } from "@/api/honors"
import { toYmd } from "@/common/utils/datetime"
import { getApiErrorMessage } from "@/utils/errorMessage"

const loading = ref(false)
const honors = ref<UserHonorsResponse | null>(null)
const errorMessage = ref("")

const awards = computed<AwardDetail[]>(() => honors.value?.awards ?? [])
const showCounts = computed(() => {
  const data = honors.value
  return data?.participationCount != null || data?.awardCount != null
})

const getFallbackMessage = (status?: number) => {
  if (status === 400) return "请求无效"
  if (status === 401) return "会话已过期，请重新登录"
  if (status === 403) return "无权限查看荣誉"
  if (status === 404) return "荣誉信息不存在"
  if (status === 409) return "请求冲突"
  return "加载荣誉失败"
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
      <h2>我的荣誉</h2>
      <el-button size="small" :loading="loading" @click="loadHonors">刷新</el-button>
    </div>

    <el-alert v-if="errorMessage" type="error" show-icon :title="errorMessage" class="status-alert" />

    <el-card v-if="showCounts" shadow="never" class="section">
      <h3>概览</h3>
      <el-descriptions :column="1">
        <el-descriptions-item v-if="honors?.participationCount != null" label="参赛次数">
          {{ honors?.participationCount }}
        </el-descriptions-item>
        <el-descriptions-item v-if="honors?.awardCount != null" label="获奖次数">
          {{ honors?.awardCount }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="section">
      <h3>奖项</h3>
      <el-table v-if="awards.length" :data="awards" style="width: 100%">
        <el-table-column prop="awardName" label="奖项" min-width="160" />
        <el-table-column prop="competitionId" label="竞赛 ID" width="140" />
        <el-table-column prop="teamId" label="队伍 ID" width="120" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ row.publishedAt ? toYmd(row.publishedAt) : "-" }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-else-if="!loading && !errorMessage"
        description="暂无荣誉"
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
