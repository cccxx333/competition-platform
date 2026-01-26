<script lang="ts" setup>
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
import { useAuthStore } from "@/stores/auth"

const authStore = useAuthStore()
const username = computed(() => authStore.user?.username?.trim() || "管理员")
</script>

<template>
  <div>
    <div class="page-header">
      <h2>欢迎回来，{{ username }}！</h2>
    </div>
    <DashboardLayout>
      <template #topLeft>
        <h2>系统概览</h2>
        <p>关键数据汇总</p>
      </template>
      <template #topRight>
        <h2>管理提醒</h2>
        <p>关注最新动态</p>
      </template>
      <template #bottom>
        <OngoingCompetitionsPanel />
      </template>
    </DashboardLayout>
  </div>
</template>

<style scoped>
.page-header h2 {
  font-size: 35px;
}
</style>
