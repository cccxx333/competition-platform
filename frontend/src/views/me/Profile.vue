<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { getMyProfile } from "@/api/profile"

const loading = ref(false)
const profile = ref<Awaited<ReturnType<typeof getMyProfile>> | null>(null)

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return
  }
  if (status === 400) {
    ElMessage.error("参数错误")
    return
  }
  if (status === 403) {
    ElMessage.error("无权限")
    return
  }
  if (status === 404) {
    ElMessage.error("资源不存在")
    return
  }
  if (status === 409) {
    ElMessage.error("业务冲突")
    return
  }
  ElMessage.error(fallback)
}

const fields = computed(() => {
  const data = profile.value
  if (!data) return []
  return [
    { label: "用户名", value: data.username },
    { label: "姓名", value: data.realName },
    { label: "邮箱", value: data.email },
    { label: "学校", value: data.school },
    { label: "专业", value: data.major },
    { label: "年级", value: data.grade },
    { label: "角色", value: data.role }
  ].filter(item => Boolean(item.value))
})

const reload = async () => {
  loading.value = true
  try {
    profile.value = await getMyProfile()
  } catch (error: any) {
    showRequestError(error, "加载个人信息失败")
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<template>
  <el-card shadow="never" v-loading="loading">
    <h2>个人信息</h2>
    <el-button size="small" :loading="loading" @click="reload">刷新</el-button>
    <el-descriptions v-if="fields.length" :column="1">
      <el-descriptions-item v-for="item in fields" :key="item.label" :label="item.label">
        {{ item.value }}
      </el-descriptions-item>
    </el-descriptions>
    <div v-else>暂无信息</div>
  </el-card>
</template>
