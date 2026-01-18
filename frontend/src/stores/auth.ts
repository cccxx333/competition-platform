import { type MeProfile, getMe } from "@/api/me"

export const TOKEN_KEY = "cp_token"

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(getStoredToken())
  const isAuthed = computed(() => Boolean(token.value))
  const user = ref<MeProfile | null>(null)
  const isMeLoaded = ref(false)
  let inFlight: Promise<void> | null = null

  const setToken = (value: string) => {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
    isMeLoaded.value = false
  }

  const clearToken = () => {
    clearUser()
    token.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  const setUser = (value: MeProfile | null) => {
    user.value = value
  }

  const clearUser = () => {
    user.value = null
    isMeLoaded.value = false
    inFlight = null
  }

  const loadMe = async () => {
    if (!token.value) {
      clearUser()
      return
    }
    if (isMeLoaded.value) return
    if (inFlight) return inFlight
    inFlight = (async () => {
      const me = await getMe()
      user.value = me
      isMeLoaded.value = true
    })().finally(() => {
      inFlight = null
    })
    return inFlight
  }

  return { token, isAuthed, user, isMeLoaded, setToken, setUser, clearUser, loadMe, clearToken }
})
