<script lang="ts" setup>
import type { FormInstance, FormRules } from "element-plus"
import { ElMessage } from "element-plus"
import { login } from "@/api/auth"
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: "",
  password: ""
})

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await login(form.username, form.password)
    authStore.setToken(result.token)
    if (result.role) {
      authStore.setUser({ username: form.username, role: result.role })
    }
    router.replace("/dashboard")
  } catch (error: any) {
    ElMessage.error(error?.message ?? "登录失败")
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card shadow="never" class="login-card">
    <h2>登录</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
    </el-form>
  </el-card>
</template>

<style scoped>
.login-card {
  max-width: 420px;
  margin: 80px auto;
}
</style>
