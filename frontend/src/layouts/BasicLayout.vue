<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { useAuthStore } from "@/stores/auth"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const userLabel = computed(() => {
  const name = authStore.user?.username ?? "用户"
  const role = authStore.user?.role
  return role ? `${name} (${role})` : name
})

const roleUpper = computed(() => String(authStore.user?.role ?? "").toUpperCase())

const handleLogout = () => {
  authStore.logout()
  router.replace("/login")
  ElMessage.success("已退出登录")
}
</script>

<template>
  <el-container class="basic-layout">
    <el-aside width="200px" class="basic-layout__aside">
      <div class="basic-layout__logo">竞赛平台</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/competitions">竞赛</el-menu-item>
        <el-sub-menu index="/teams">
          <template #title>队伍</template>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/join">加入队伍</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my-applications">我的申请</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my">我的队伍</el-menu-item>
          <el-menu-item v-if="roleUpper === 'TEACHER' || roleUpper === 'ADMIN'" index="/teams/lookup">
            队伍查询
          </el-menu-item>
          <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teams/review">审核</el-menu-item>
          <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/awards/publish">奖项发布</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teacher/applications">我的教师申请</el-menu-item>
        <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/teacher-applications">教师申请审核</el-menu-item>
        <el-menu-item index="/me/profile">个人信息</el-menu-item>
        <el-menu-item v-if="roleUpper !== 'TEACHER'" index="/me/skills">技能</el-menu-item>
        <el-menu-item v-if="roleUpper === 'STUDENT'" index="/me/honors">荣誉</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="basic-layout__header">
        <div class="basic-layout__title">竞赛平台</div>
        <div class="basic-layout__user">
          <span>{{ userLabel }}</span>
          <el-button type="default" link @click="handleLogout">退出登录</el-button>
        </div>
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

.basic-layout__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.basic-layout__main {
  background: #f5f7fa;
}
</style>
