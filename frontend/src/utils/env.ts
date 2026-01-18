export function getApiBaseUrl(): string {
  return import.meta.env.VITE_API_BASE_URL ?? "/api"
}

export function getApiProxyTarget(): string {
  return import.meta.env.VITE_API_PROXY_TARGET ?? "http://localhost:8080"
}
