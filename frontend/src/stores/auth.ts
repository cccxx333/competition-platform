export const TOKEN_KEY = "cp_token"

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(getStoredToken())
  const isAuthed = computed(() => Boolean(token.value))

  const setToken = (value: string) => {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  const clearToken = () => {
    token.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, isAuthed, setToken, clearToken }
})
