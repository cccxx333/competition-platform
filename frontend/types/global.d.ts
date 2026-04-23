export {}

declare global {
  type ApiResponseData<T = unknown> = {
    code?: number | string
    message?: string
    data: T
  }

  const ElLoading: typeof import("element-plus/es").ElLoading
  const ElNotification: typeof import("element-plus/es").ElNotification
  const ElMessageBox: typeof import("element-plus/es").ElMessageBox
}
