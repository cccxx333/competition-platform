/*
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
*/
import { isArray } from "@@/utils/validate"
import { useUserStore } from "@/pinia/stores/user"

/** 鍏ㄥ眬鏉冮檺鍒ゆ柇鍑芥暟锛屽拰鏉冮檺鎸囦护 v-permission 鍔熻兘绫讳技 */
export function checkPermission(permissionRoles: string[]): boolean {
  if (isArray(permissionRoles) && permissionRoles.length > 0) {
    const { roles } = useUserStore()
    return roles.some(role => permissionRoles.includes(role))
  } else {
    console.error("鍙傛暟蹇呴』鏄竴涓暟缁勪笖闀垮害澶т簬 0锛屽弬鑰冿細checkPermission(['admin', 'editor'])")
    return false
  }
}

