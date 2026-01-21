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
        meta: { title: "Dashboard" },
        children: [
          {
            path: "student",
            name: "StudentDashboard",
            component: () => import("@/views/dashboard/StudentDashboard.vue"),
            meta: { title: "Dashboard" }
          },
          {
            path: "teacher",
            name: "TeacherDashboard",
            component: () => import("@/views/dashboard/TeacherDashboard.vue"),
            meta: { title: "Dashboard" }
          },
          {
            path: "admin",
            name: "AdminDashboard",
            component: () => import("@/views/dashboard/AdminDashboard.vue"),
            meta: { title: "Dashboard" }
          }
        ]
      },
      {
        path: "me/profile",
        name: "MyProfile",
        component: () => import("@/views/me/Profile.vue"),
        meta: { title: "Profile" }
      },
      {
        path: "me/skills",
        name: "MySkills",
        component: () => import("@/views/me/Skills.vue"),
        meta: { title: "Skills" }
      },
      {
        path: "competitions",
        name: "CompetitionList",
        component: () => import("@/views/competitions/CompetitionList.vue"),
        meta: { title: "Competitions" }
      },
      {
        path: "competitions/:id",
        name: "CompetitionDetail",
        component: () => import("@/views/competitions/CompetitionDetail.vue"),
        meta: { title: "Competition Detail", hidden: true }
      },
      {
        path: "teacher/applications",
        name: "TeacherApplicationList",
        component: () => import("@/views/teacher/TeacherApplicationList.vue"),
        meta: { title: "Teacher Applications", hidden: true }
      },
      {
        path: "admin/teacher-applications",
        name: "AdminTeacherApplicationReviewList",
        component: () => import("@/views/admin/AdminTeacherApplicationReviewList.vue"),
        meta: { title: "Teacher Applications Review", hidden: true }
      },
      {
        path: "teams/join",
        name: "JoinTeam",
        component: () => import("@/views/teams/JoinTeam.vue"),
        meta: { title: "Join Team" }
      },
      {
        path: "teams/my-applications",
        name: "MyTeamApplications",
        component: () => import("@/views/teams/MyApplications.vue"),
        meta: { title: "My Applications" }
      },
      {
        path: "teams/my",
        name: "MyTeam",
        component: () => import("@/views/teams/MyTeam.vue"),
        meta: { title: "My Team" }
      },
      {
        path: "teams/lookup",
        name: "TeamLookup",
        component: () => import("@/views/teams/TeamLookup.vue"),
        meta: { title: "Team Lookup", roles: ["ADMIN", "TEACHER"] }
      },
      {
        path: "teams/:teamId",
        name: "TeamDetail",
        component: () => import("@/views/teams/TeamDetail.vue"),
        meta: { title: "Team Detail", hidden: true }
      },
      {
        path: "teams/:teamId/posts",
        name: "TeamPosts",
        component: () => import("@/views/teams/TeamPosts.vue"),
        meta: { title: "Team Posts", hidden: true }
      },
      {
        path: "teams/:teamId/posts/:postId",
        name: "PostThread",
        component: () => import("@/views/teams/PostThread.vue"),
        meta: { title: "Post Thread", hidden: true }
      },
      {
        path: "teams/:teamId/members",
        name: "TeamMembers",
        component: () => import("@/views/teams/TeamMembers.vue"),
        meta: { title: "Team Members", hidden: true }
      },
      {
        path: "teams/:teamId/submissions",
        name: "TeamSubmissions",
        component: () => import("@/views/teams/TeamSubmissions.vue"),
        meta: { title: "Team Submissions", hidden: true }
      },
      {
        path: "teams/review",
        name: "TeacherReviewApplications",
        component: () => import("@/views/teams/TeacherReview.vue"),
        meta: { title: "Teacher Review" }
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
