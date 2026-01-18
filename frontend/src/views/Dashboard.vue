<script lang="ts" setup>
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const rolePath = computed(() => {
  const role = authStore.user?.role?.toUpperCase()
  if (role === "ADMIN") return "/dashboard/admin"
  if (role === "TEACHER") return "/dashboard/teacher"
  if (role === "STUDENT") return "/dashboard/student"
  return ""
})

const isRoot = computed(() => route.path === "/dashboard")

watchEffect(() => {
  if (isRoot.value && rolePath.value) {
    router.replace(rolePath.value)
  }
})
</script>

<template>
  <router-view />
  <div v-if="isRoot">Redirecting...</div>
</template>
