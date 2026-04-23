import type { Router } from "vue-router"
import { pinia } from "@/pinia"
import { useAuthStore } from "@/stores/auth"

const LOGIN_PATH = "/login"
const DASHBOARD_PATH = "/dashboard"

export function registerNavigationGuard(router: Router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore(pinia)
    const authed = Boolean(authStore.token)

    if (to.path === LOGIN_PATH && authed) return DASHBOARD_PATH
    if (to.path !== LOGIN_PATH && !authed) return LOGIN_PATH
    if (authed && !authStore.isMeLoaded) {
      try {
        await authStore.loadMe()
      } catch {
        authStore.clearToken()
        return LOGIN_PATH
      }
    }
    const routeRoles = (to.meta?.roles as string[] | undefined) ?? []
    if (routeRoles.length > 0) {
      const currentRole = String(authStore.user?.role ?? "").toUpperCase()
      const allowed = routeRoles.map((role) => String(role).toUpperCase())
      if (!allowed.includes(currentRole)) {
        return DASHBOARD_PATH
      }
    }
    return true
  })
}
