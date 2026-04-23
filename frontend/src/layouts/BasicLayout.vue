<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { Avatar, Document, House, Medal, Star, Trophy, User, SwitchButton, Setting } from "@element-plus/icons-vue"
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
    <el-aside width="220px" class="basic-layout__aside">
      <div class="basic-layout__logo">
        <img class="basic-layout__sidebar-motto" src="/branding/motto.png" alt="校训" />
      </div>
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/dashboard">
          <el-icon class="basic-layout__menu-icon"><House /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-sub-menu index="/competitions">
          <template #title>
            <el-icon class="basic-layout__menu-icon"><Trophy /></el-icon>
            <span>竞赛</span>
          </template>
          <el-menu-item index="/competitions">竞赛列表</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/competitions/apply">竞赛报名</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/teams">
          <template #title>
            <el-icon class="basic-layout__menu-icon"><Avatar /></el-icon>
            <span>队伍</span>
          </template>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my-applications">我的申请</el-menu-item>
          <el-menu-item v-if="roleUpper === 'STUDENT'" index="/teams/my">我的队伍</el-menu-item>
          <el-menu-item v-if="roleUpper === 'TEACHER' || roleUpper === 'ADMIN'" index="/teams/lookup">
            队伍查询
          </el-menu-item>
          <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teams/review">审核</el-menu-item>
          <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/awards/publish">奖项发布</el-menu-item>
          <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/teacher-applications">申请审核</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teacher/applications">
          <el-icon class="basic-layout__menu-icon"><Document /></el-icon>
          <span>创建队伍申请</span>
        </el-menu-item>
        <el-menu-item v-if="roleUpper === 'TEACHER'" index="/teacher/managed-applications">
          <el-icon class="basic-layout__menu-icon"><Document /></el-icon>
          <span>审核负责的竞赛</span>
        </el-menu-item>

        <el-menu-item v-if="roleUpper === 'ADMIN'" index="/admin/users">
          <el-icon class="basic-layout__menu-icon"><Setting /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/me/profile">
          <el-icon class="basic-layout__menu-icon"><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item v-if="roleUpper !== 'TEACHER' && roleUpper !== 'ADMIN'" index="/me/skills">
          <el-icon class="basic-layout__menu-icon"><Star /></el-icon>
          <span>技能</span>
        </el-menu-item>
        <el-menu-item v-else-if="roleUpper === 'ADMIN'" index="/me/skills">
          <el-icon class="basic-layout__menu-icon"><Star /></el-icon>
          <span>技能管理</span>
        </el-menu-item>
        <el-menu-item v-if="roleUpper === 'STUDENT'" index="/me/honors">
          <el-icon class="basic-layout__menu-icon"><Medal /></el-icon>
          <span>荣誉</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="basic-layout__header">
        <div class="basic-layout__title">学科竞赛平台</div>
        <div class="basic-layout__user">
          <div class="basic-layout__user-meta">
            <el-icon class="basic-layout__user-avatar"><User /></el-icon>
            <div class="basic-layout__user-text">
              <div class="basic-layout__user-name">{{ authStore.user?.username ?? "用户" }}</div>
              <div class="basic-layout__user-role">{{ authStore.user?.role ?? "-" }}</div>
            </div>
          </div>
          <el-button type="default" link class="basic-layout__logout" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </el-button>
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
  display: flex;
  align-items: flex-start;
  justify-content: center;
  height: 80px;
  padding: -4px 8px 0;
  text-align: center;
  font-weight: 600;
  font-size: 20px;
}

.basic-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}

.basic-layout__title {
  display: inline-flex;
  align-items: center;
  margin-left: 30px; /* 往右移 */
  font-size: 25px; /* 字体大小 */
  font-weight: 650; /* 字体粗细 */
  gap: 12px;
}

.basic-layout__sidebar-motto {
  margin-top: -30px;
  height: 110px;
  object-fit: contain;
}

.basic-layout__user {
  display: inline-flex;
  align-items: center;
  gap: 20px;
}

.basic-layout__user-meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.basic-layout__user-avatar {
  font-size: 22px;
  color: #111827;
}

.basic-layout__user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.1;
}

.basic-layout__user-name {
  font-weight: 600;
  color: #111827;
}

.basic-layout__user-role {
  font-size: 12px;
  color: #6b7280;
}

.basic-layout__logout {
  gap: 6px;
  color: #374151;
}

.basic-layout__menu-icon {
  margin-right: 8px;
}

.basic-layout__main {
  background: #f5f7fa;
}
</style>
