<script lang="ts" setup>
import OngoingCompetitionsPanel from "./OngoingCompetitionsPanel.vue"
</script>

<template>
  <div class="dashboard-layout">
    <div class="dashboard-grid">
      <el-card shadow="never" class="cp-card dashboard-card">
        <slot name="topLeft" />
      </el-card>
      <el-card shadow="never" class="cp-card dashboard-card">
        <slot name="topRight" />
      </el-card>
    </div>
    <div class="dashboard-section">
      <slot name="bottom">
        <OngoingCompetitionsPanel />
      </slot>
    </div>
  </div>
</template>

<style scoped>
.dashboard-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.dashboard-card {
  min-height: 140px;
}

.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
