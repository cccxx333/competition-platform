import type { RouteRecordRaw } from "vue-router"
import { createRouter } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { flatMultiLevelRoutes } from "./helper"

const BasicLayout = () => import("@/layouts/BasicLayout.vue")

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/login",
    component: () => import("@/views/Login.vue"),
    meta: { hidden: true }
  },
  {
    path: "/",
    component: BasicLayout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/Dashboard.vue"),
        meta: { title: "概览" },
        children: [
          {
            path: "student",
            name: "StudentDashboard",
            component: () => import("@/views/dashboard/StudentDashboard.vue"),
            meta: { title: "概览" }
          },
          {
            path: "teacher",
            name: "TeacherDashboard",
            component: () => import("@/views/dashboard/TeacherDashboard.vue"),
            meta: { title: "概览" }
          },
          {
            path: "admin",
            name: "AdminDashboard",
            component: () => import("@/views/dashboard/AdminDashboard.vue"),
            meta: { title: "概览" }
          }
        ]
      },
      {
        path: "me/profile",
        name: "MyProfile",
        component: () => import("@/views/me/Profile.vue"),
        meta: { title: "个人信息" }
      },
      {
        path: "me/skills",
        name: "MySkills",
        component: () => import("@/views/me/Skills.vue"),
        meta: { title: "技能" }
      },
      {
        path: "me/honors",
        name: "MyHonors",
        component: () => import("@/views/me/Honors.vue"),
        meta: { title: "荣誉" }
      },
      {
        path: "competitions",
        name: "CompetitionList",
        component: () => import("@/views/competitions/CompetitionList.vue"),
        meta: { title: "竞赛" }
      },
      {
        path: "competitions/apply",
        name: "CompetitionApply",
        component: () => import("@/views/competitions/CompetitionApply.vue"),
        meta: { title: "竞赛报名" }
      },
      {
        path: "competitions/:id",
        name: "CompetitionDetail",
        component: () => import("@/views/competitions/CompetitionDetail.vue"),
        meta: { title: "竞赛详情", hidden: true }
      },
      {
        path: "teacher/applications",
        name: "TeacherApplicationList",
        component: () => import("@/views/teacher/TeacherApplicationList.vue"),
        meta: { title: "创建队伍申请", hidden: true }
      },
      {
        path: "teacher/applications/create/:competitionId",
        name: "TeacherApplicationCreate",
        component: () => import("@/views/teacher/TeacherApplyCreate.vue"),
        meta: { title: "创建队伍申请", hidden: true }
      },
      {
        path: "admin/teacher-applications",
        name: "AdminTeacherApplicationReviewList",
        component: () => import("@/views/admin/AdminTeacherApplicationReviewList.vue"),
        meta: { title: "创建队伍申请审核", hidden: true }
      },
      {
        path: "admin/users",
        name: "AdminUserManagement",
        component: () => import("@/views/admin/AdminUserManagement.vue"),
        meta: { title: "用户管理" }
      },
      {
        path: "admin/awards/publish",
        name: "AdminAwardPublish",
        component: () => import("@/views/admin/AwardPublish.vue"),
        meta: { title: "奖项发布" }
      },
      {
        path: "teams/my-applications",
        name: "MyTeamApplications",
        component: () => import("@/views/teams/MyApplications.vue"),
        meta: { title: "我的申请" }
      },
      {
        path: "teams/my",
        name: "MyTeam",
        component: () => import("@/views/teams/MyTeam.vue"),
        meta: { title: "我的队伍" }
      },
      {
        path: "teams/lookup",
        name: "TeamLookup",
        component: () => import("@/views/teams/TeamLookup.vue"),
        meta: { title: "队伍查询", roles: ["ADMIN", "TEACHER"] }
      },
      {
        path: "teams/:teamId",
        name: "TeamDetail",
        component: () => import("@/views/teams/TeamDetail.vue"),
        meta: { title: "队伍详情", hidden: true }
      },
      {
        path: "teams/:teamId/posts",
        name: "TeamPosts",
        component: () => import("@/views/teams/TeamPosts.vue"),
        meta: { title: "队伍讨论", hidden: true }
      },
      {
        path: "teams/:teamId/posts/:postId",
        name: "PostThread",
        component: () => import("@/views/teams/PostThread.vue"),
        meta: { title: "讨论详情", hidden: true }
      },
      {
        path: "teams/:teamId/members",
        name: "TeamMembers",
        component: () => import("@/views/teams/TeamMembers.vue"),
        meta: { title: "成员信息", hidden: true }
      },
      {
        path: "teams/:teamId/submissions",
        name: "TeamSubmissions",
        component: () => import("@/views/teams/TeamSubmissions.vue"),
        meta: { title: "队伍提交", hidden: true }
      },
      {
        path: "teams/review",
        name: "TeacherReviewApplications",
        component: () => import("@/views/teams/TeacherReview.vue"),
        meta: { title: "申请审核" }
      }
    ]
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
    meta: { hidden: true }
  }
]

export const router = createRouter({
  history: routerConfig.history,
  routes: routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

registerNavigationGuard(router)
