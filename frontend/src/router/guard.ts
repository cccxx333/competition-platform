import type { Router } from "vue-router"
import { pinia } from "@/pinia"
import { useAuthStore } from "@/stores/auth"

const LOGIN_PATH = "/login"
const DASHBOARD_PATH = "/dashboard"

export function registerNavigationGuard(router: Router) {
  router.beforeEach((to) => {
    const authStore = useAuthStore(pinia)
    const authed = Boolean(authStore.token)

    if (to.path === LOGIN_PATH && authed) return DASHBOARD_PATH
    if (to.path !== LOGIN_PATH && !authed) return LOGIN_PATH
    return true
  })
}
