<script lang="ts" setup>
import DashboardLayout from "@/components/Dashboard/DashboardLayout.vue"
import OngoingCompetitionsPanel from "@/components/Dashboard/OngoingCompetitionsPanel.vue"
</script>

<template>
  <DashboardLayout>
    <template #topLeft>
      <h2>????</h2>
      <p>????</p>
    </template>
    <template #topRight>
      <h2>????</h2>
      <p>????</p>
    </template>
    <template #bottom>
      <OngoingCompetitionsPanel />
    </template>
  </DashboardLayout>
</template>
