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
