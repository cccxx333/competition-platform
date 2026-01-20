type ErrorLike = {
  message?: unknown
  response?: {
    data?: {
      message?: unknown
      reason?: unknown
    }
  }
  rawMessage?: unknown
  responseData?: {
    message?: unknown
    reason?: unknown
  }
}

const isReadableMessage = (value: unknown): value is string => {
  if (typeof value !== "string") return false
  const trimmed = value.trim()
  if (!trimmed) return false
  if (trimmed.includes("�")) return false
  if (/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/.test(trimmed)) return false
  return true
}

export const getApiErrorMessage = (err: unknown, fallback: string): string => {
  const error = err as ErrorLike
  const candidates = [
    error?.response?.data?.message,
    error?.response?.data?.reason,
    error?.responseData?.message,
    error?.responseData?.reason,
    error?.rawMessage,
    error?.message
  ]
  for (const candidate of candidates) {
    if (isReadableMessage(candidate)) return candidate
  }
  return fallback
}
