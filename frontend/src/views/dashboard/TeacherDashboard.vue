<script lang="ts" setup>
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const username = computed(() => authStore.user?.username?.trim() || "同学")
</script>

<template>
  <div>
    <div class="page-header">
      <h2>欢迎回来，{{ username }}！</h2>
    </div>
    <DashboardLayout>
      <template #topLeft>
        <h2>教学概览</h2>
        <p>近期任务一览</p>
      </template>
      <template #topRight>
        <h2>进度提醒</h2>
        <p>关注待办事项</p>
      </template>
      <template #bottom>
        <OngoingCompetitionsPanel />
      </template>
    </DashboardLayout>
  </div>
</template>

<style scoped>

.page-header h2 {
  font-size: 35px; /* 你想要的大小 */
}
</style>
