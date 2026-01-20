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
  // 鍙闂殑璺敱
  const routes = ref<RouteRecordRaw[]>([])

  // 鏈夎闂潈闄愮殑鍔ㄦ€佽矾鐢?
  const addRoutes = ref<RouteRecordRaw[]>([])

  // 鏍规嵁瑙掕壊鐢熸垚鍙闂殑 Routes锛堝彲璁块棶鐨勮矾鐢?= 甯搁┗璺敱 + 鏈夎闂潈闄愮殑鍔ㄦ€佽矾鐢憋級
  const setRoutes = (roles: string[]) => {
    const accessedRoutes = filterDynamicRoutes(dynamicRoutes, roles)
    set(accessedRoutes)
  }

  // 鎵€鏈夎矾鐢?= 鎵€鏈夊父椹昏矾鐢?+ 鎵€鏈夊姩鎬佽矾鐢?
  const setAllRoutes = () => {
    set(dynamicRoutes)
  }

  // 缁熶竴璁剧疆
  const set = (accessedRoutes: RouteRecordRaw[]) => {
    routes.value = constantRoutes.concat(accessedRoutes)
    addRoutes.value = routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(accessedRoutes) : accessedRoutes
  }

  return { routes, addRoutes, setRoutes, setAllRoutes }
})

/**
 * @description 鍦?SPA 搴旂敤涓彲鐢ㄤ簬鍦?pinia 瀹炰緥琚縺娲诲墠浣跨敤 store
 * @description 鍦?SSR 搴旂敤涓彲鐢ㄤ簬鍦?setup 澶栦娇鐢?store
 */
export function usePermissionStoreOutside() {
  return usePermissionStore(pinia)
}

