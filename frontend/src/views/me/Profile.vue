<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getMyProfile } from "@/api/profile"

const loading = ref(false)
const profile = ref<Awaited<ReturnType<typeof getMyProfile>> | null>(null)

const fields = computed(() => {
  const data = profile.value
  if (!data) return []
  return [
    { label: "Username", value: data.username },
    { label: "Real Name", value: data.realName },
    { label: "Email", value: data.email },
    { label: "School", value: data.school },
    { label: "Major", value: data.major },
    { label: "Grade", value: data.grade },
    { label: "Role", value: data.role }
  ].filter(item => Boolean(item.value))
})

const reload = async () => {
  loading.value = true
  try {
    profile.value = await getMyProfile()
  } catch (error: any) {
    const message = error?.message ?? "Failed to load profile"
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <h2>Profile</h2>
    <el-descriptions v-if="fields.length" :column="1">
      <el-descriptions-item v-for="item in fields" :key="item.label" :label="item.label">
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>
    <div v-else>Empty</div>
  </el-card>
</template>
