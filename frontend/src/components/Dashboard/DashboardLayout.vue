<script lang="ts" setup>
import { computed, useSlots } from "vue"
import OngoingCompetitionsPanel from "./OngoingCompetitionsPanel.vue"

const slots = useSlots()
const topSlotNames = computed(() => ["topLeft", "topMidLeft", "topCenter", "topRight"].filter((name) => !!slots[name]))
</script>

<template>
  <div class="dashboard-layout">
    <div
      class="dashboard-grid"
      :class="{
        'dashboard-grid--three': topSlotNames.length === 3,
        'dashboard-grid--four': topSlotNames.length === 4
      }"
    >
      <el-card v-for="slotName in topSlotNames" :key="slotName" shadow="never" class="cp-card dashboard-card">
        <slot :name="slotName" />
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

.dashboard-grid--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.dashboard-grid--four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
