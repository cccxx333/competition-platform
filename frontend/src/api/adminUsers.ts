import { client } from "@/api/client"

export type UserRole = "ADMIN" | "TEACHER" | "STUDENT"
export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED"

export type AdminUserProfile = {
  id?: number
  accountNo?: string
  role?: UserRole | string
  approvalStatus?: ApprovalStatus | string
  username?: string
  displayName?: string
  realName?: string
  email?: string
  phone?: string
  avatarUrl?: string
  school?: string
  major?: string
  grade?: string
  createdAt?: string
}

export type AdminUserListParams = {
  page?: number
  size?: number
  keyword?: string
  role?: UserRole | ""
  approvalStatus?: ApprovalStatus | ""
}

export type AdminUserUpdatePayload = {
  username?: string
  displayName?: string
  email?: string
  realName?: string
  phone?: string
  avatarUrl?: string
  school?: string
  major?: string
  grade?: string
  approvalStatus?: ApprovalStatus | ""
}

type PageResponse<T> = {
  content?: T[]
  totalElements?: number
  number?: number
  size?: number
  totalPages?: number
}

const unwrapData = <T>(payload: any): T => {
  return (payload?.data ?? payload) as T
}

const toError = (error: any, fallback: string) => {
  const status = error?.response?.status
  const message = error?.response?.data?.message ?? fallback
  const err = new Error(message)
  ;(err as any).status = status
  return err
}

export async function listAdminUsersPage(params: AdminUserListParams = {}) {
  try {
    const response = await client.get("/admin/users", { params })
    const payload = unwrapData<PageResponse<AdminUserProfile> | AdminUserProfile[]>(response?.data)
    const items = Array.isArray(payload) ? payload : payload?.content ?? []
    const total = Array.isArray(payload) ? undefined : payload?.totalElements
    const page = Array.isArray(payload) ? undefined : payload?.number
    const size = Array.isArray(payload) ? undefined : payload?.size
    const totalPages = Array.isArray(payload) ? undefined : payload?.totalPages
    return { items, total, page, size, totalPages }
  } catch (error: any) {
    throw toError(error, "Failed to load users")
  }
}

export async function getAdminUser(userId: number): Promise<AdminUserProfile> {
  try {
    const response = await client.get(`/admin/users/${userId}`)
    return unwrapData<AdminUserProfile>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load user detail")
  }
}

export async function updateAdminUser(userId: number, payload: AdminUserUpdatePayload): Promise<AdminUserProfile> {
  try {
    const approvalStatus = payload.approvalStatus?.trim()
    const response = await client.put(`/admin/users/${userId}`, {
      ...payload,
      approvalStatus: approvalStatus || undefined
    })
    return unwrapData<AdminUserProfile>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to update user")
  }
}
