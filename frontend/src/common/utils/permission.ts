/*
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
*/
import { isArray } from "@@/utils/validate"
import { useUserStore } from "@/pinia/stores/user"

/** 全局权限判断函数，作用与 v-permission 指令类似 */
export function checkPermission(permissionRoles: string[]): boolean {
  if (isArray(permissionRoles) && permissionRoles.length > 0) {
    const { roles } = useUserStore()
    return roles.some(role => permissionRoles.includes(role))
  } else {
    console.error("参数必须是一个非空数组，参考：checkPermission(['admin', 'editor'])")
    return false
  }
}

