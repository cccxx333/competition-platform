import axios from "axios"
import { router } from "@/router"
import { pinia } from "@/pinia"
import { getApiBaseUrl } from "@/utils/env"
import { getStoredToken, useAuthStore } from "@/stores/auth"

const client = axios.create({
  baseURL: getApiBaseUrl()
})

client.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      useAuthStore(pinia).clearToken()
      router.replace("/login")
    }
    return Promise.reject(error)
  }
)

export { client }
