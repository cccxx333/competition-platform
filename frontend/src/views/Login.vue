<script lang="ts" setup>
import type { FormInstance, FormRules } from "element-plus"
import { ElMessage } from "element-plus"
import { login, register } from "@/api/auth"
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

const registerDialogVisible = ref(false)
const registerFormRef = ref<FormInstance>()
const registering = ref(false)
const registerForm = reactive({
  username: "",
  accountNo: "",
  email: "",
  password: ""
})

const registerRules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  accountNo: [{ required: true, message: "请输入学号/工号", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入有效邮箱", trigger: "blur" }
  ],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
}

const openRegister = () => {
  registerDialogVisible.value = true
}

const validateForm = async (ref: FormInstance | undefined, invalidMessage: string) => {
  if (!ref) {
    ElMessage.error("表单未初始化，请刷新后重试")
    return false
  }
  try {
    const valid = await ref.validate()
    if (!valid) {
      ElMessage.warning(invalidMessage)
      return false
    }
    return true
  } catch {
    ElMessage.warning(invalidMessage)
    return false
  }
}

const extractErrorMessage = (error: any, fallback: string) => {
  const responseData = error?.response?.data
  if (responseData?.errors && typeof responseData.errors === "object") {
    const first = Object.values(responseData.errors)[0]
    if (typeof first === "string" && first.trim()) {
      return first
    }
  }
  if (typeof responseData?.message === "string" && responseData.message.trim()) {
    return responseData.message
  }
  if (typeof error?.message === "string" && error.message.trim()) {
    return error.message
  }
  return fallback
}

const handleRegister = async () => {
  const valid = await validateForm(registerFormRef.value, "请先完善注册信息")
  if (!valid) return

  registering.value = true
  try {
    await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })
    ElMessage.success("注册成功，请登录")
    form.username = registerForm.username
    form.password = ""
    registerForm.username = ""
    registerForm.accountNo = ""
    registerForm.email = ""
    registerForm.password = ""
    registerDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(extractErrorMessage(error, "注册失败"))
  } finally {
    registering.value = false
  }
}

const handleLogin = async () => {
  const valid = await validateForm(formRef.value, "请输入用户名和密码")
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
    ElMessage.error(extractErrorMessage(error, "登录失败"))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card shadow="never" class="login-card">
    <div class="login-logo">
      <img src="/branding/logo.png" alt="Logo" />
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <div class="login-actions">
        <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        <el-button class="register-button" type="default" plain @click="openRegister">注册</el-button>
      </div>
    </el-form>
  </el-card>
  <el-dialog v-model="registerDialogVisible" title="注册" width="420px">
    <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="registerForm.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="学号/工号" prop="accountNo">
        <el-input v-model="registerForm.accountNo" autocomplete="off" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="registerForm.email" autocomplete="email" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="registerForm.password" type="password" autocomplete="new-password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="registerDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="registering" @click="handleRegister">提交</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.login-card {
  position: relative;
  max-width: 540px;
  margin: 80px auto;
  padding: 24px 32px 32px;
}

.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.login-logo img {
  width: 110px;
  height: 110px;
  object-fit: contain;
}

.login-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  width: 100%;
}

.register-button {
  background-color: #fff;
}
</style>
