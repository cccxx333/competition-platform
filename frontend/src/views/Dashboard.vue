<script lang="ts" setup>
import { client } from "@/api/client"

type HealthStatus = "idle" | "loading" | "success" | "error"

const status = ref<HealthStatus>("idle")
const message = ref("")
const responseText = ref("")

const runHealthCheck = async () => {
  status.value = "loading"
  message.value = ""
  responseText.value = ""
  try {
    const response = await client.get("/health/db")
    status.value = "success"
    responseText.value = JSON.stringify(response.data, null, 2)
  } catch (error: any) {
    status.value = "error"
    message.value = error?.message ?? "Request failed"
  }
}
</script>

<template>
  <el-card shadow="never">
    <h2>Dashboard</h2>
    <p>Dashboard placeholder.</p>
    <el-button type="primary" :loading="status === 'loading'" @click="runHealthCheck">
      Health Check
    </el-button>
    <div class="health-status">
      <div>Status: {{ status }}</div>
      <div v-if="status === 'error'">Error: {{ message }}</div>
      <pre v-if="status === 'success'">{{ responseText }}</pre>
    </div>
  </el-card>
</template>

<style scoped>
.health-status {
  margin-top: 16px;
  font-size: 14px;
}

pre {
  margin-top: 8px;
  white-space: pre-wrap;
}
</style>
