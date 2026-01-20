<script lang="ts" setup>
import { useAuthStore } from "@/stores/auth"

const route = useRoute()
const authStore = useAuthStore()

const userLabel = computed(() => {
  const name = authStore.user?.username ?? "User"
  const role = authStore.user?.role
  return role ? `${name} (${role})` : name
})

const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())
</script>

<template>
  <el-container class="basic-layout">
    <el-aside width="200px" class="basic-layout__aside">
      <div class="basic-layout__logo">Competition Platform</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/dashboard">Dashboard</el-menu-item>
        <el-menu-item index="/competitions">Competitions</el-menu-item>
        <el-sub-menu index="/teams">
          <template #title>Teams</template>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/join">Join</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my-applications">My Applications</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my">My Team</el-menu-item>
          <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teams/review">Review</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teacher/applications">我的教师申请</el-menu-item>
        <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/teacher-applications">教师申请审核</el-menu-item>
        <el-menu-item index="/me/profile">Profile</el-menu-item>
        <el-menu-item index="/me/skills">Skills</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="basic-layout__header">
        <div class="basic-layout__title">Competition Platform</div>
        <div class="basic-layout__user">{{ userLabel }}</div>
      </el-header>
      <el-main class="basic-layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.basic-layout__aside {
  border-right: 1px solid #e5e7eb;
  padding: 16px 0;
}

.basic-layout__logo {
  font-weight: 600;
  padding: 0 16px 12px;
}

.basic-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}

.basic-layout__main {
  background: #f5f7fa;
}
</style>
