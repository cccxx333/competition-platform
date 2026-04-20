/*
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
*/
import type { RouteRecordRaw } from "vue-router"
import { pinia } from "@/pinia"
import { constantRoutes } from "@/router"
import { routerConfig } from "@/router/config"
import { flatMultiLevelRoutes } from "@/router/helper"

function hasPermission(roles: string[], route: RouteRecordRaw) {
  const routeRoles = route.meta?.roles
  return routeRoles ? roles.some(role => routeRoles.includes(role)) : true
}

function filterDynamicRoutes(routes: RouteRecordRaw[], roles: string[]) {
  const res: RouteRecordRaw[] = []
  routes.forEach((route) => {
    const tempRoute = { ...route }
    if (hasPermission(roles, tempRoute)) {
      if (tempRoute.children) {
        tempRoute.children = filterDynamicRoutes(tempRoute.children, roles)
      }
      res.push(tempRoute)
    }
  })
  return res
}

const dynamicRoutes: RouteRecordRaw[] = []

export const usePermissionStore = defineStore("permission", () => {
  // 可访问路由
  const routes = ref<RouteRecordRaw[]>([])

  // 有访问权限的动态路由
  const addRoutes = ref<RouteRecordRaw[]>([])

  // 根据角色生成可访问 Routes（常量路由 + 有权限的动态路由）
  const setRoutes = (roles: string[]) => {
    const accessedRoutes = filterDynamicRoutes(dynamicRoutes, roles)
    set(accessedRoutes)
  }

  // 所有路由 = 常量路由 + 全部动态路由
  const setAllRoutes = () => {
    set(dynamicRoutes)
  }

  // 统一设置 routes 与 addRoutes
  const set = (accessedRoutes: RouteRecordRaw[]) => {
    routes.value = constantRoutes.concat(accessedRoutes)
    addRoutes.value = routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(accessedRoutes) : accessedRoutes
  }

  return { routes, addRoutes, setRoutes, setAllRoutes }
})

/**
 * @description 在 SPA 中可用于 pinia 实例激活前访问 store
 * @description 在 SSR 中可用于 setup 外访问 store
 */
export function usePermissionStoreOutside() {
  return usePermissionStore(pinia)
}

