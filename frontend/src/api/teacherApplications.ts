import { client } from "@/api/client"

export type TeacherApplicationStatus = "PENDING" | "APPROVED" | "REJECTED"

export type TeacherApplicationSkill = {
  skillId?: number
  weight?: number
}

export type TeacherApplicationItem = {
  id?: number
  competitionId?: number
  teacherId?: number
  status?: TeacherApplicationStatus
  appliedAt?: string
  reviewedAt?: string
  reviewedBy?: number
  reviewComment?: string
  generatedTeamId?: number
  skills?: TeacherApplicationSkill[]
  [key: string]: unknown
}

export type TeacherApplicationCreatePayload = {
  teamName?: string
  description?: string
  skills?: TeacherApplicationSkill[]
}

export type TeacherApplicationReviewPayload = {
  approved: boolean
  reviewComment?: string
}

export type TeacherApplicationListParams = {
  status?: TeacherApplicationStatus
  competitionId?: number
}

export type TeacherApplicationListItem = {
  id?: number
  competitionId?: number
  competitionName?: string
  status?: TeacherApplicationStatus
  createdAt?: string
  updatedAt?: string
  reviewComment?: string
  description?: string
  teamDescription?: string
  skills?: TeacherApplicationSkill[]
}

export type AdminTeacherApplicationListItem = {
  id?: number
  teacherId?: number
  teacherName?: string
  competitionId?: number
  competitionName?: string
  status?: TeacherApplicationStatus
  createdAt?: string
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

export async function createTeacherApplication(
  competitionId: number,
  payload: TeacherApplicationCreatePayload
): Promise<TeacherApplicationItem> {
  try {
    const response = await client.post("/teacher-applications", {
      competitionId,
      ...payload
    })
    return unwrapData<TeacherApplicationItem>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to submit teacher application")
  }
}

export async function listMyTeacherApplications(params: TeacherApplicationListParams = {}) {
  try {
    const response = await client.get("/teacher-applications", { params })
    return unwrapData<TeacherApplicationItem[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load teacher applications")
  }
}

export async function listMyTeacherApplicationPage(params: {
  page?: number
  size?: number
  status?: TeacherApplicationStatus
}) {
  try {
    const response = await client.get("/teacher-applications", { params })
    const payload = unwrapData<PageResponse<TeacherApplicationListItem> | TeacherApplicationListItem[]>(response?.data)
    const items = Array.isArray(payload) ? payload : payload?.content ?? []
    const total = Array.isArray(payload) ? undefined : payload?.totalElements
    const page = Array.isArray(payload) ? undefined : payload?.number
    const size = Array.isArray(payload) ? undefined : payload?.size
    const totalPages = Array.isArray(payload) ? undefined : payload?.totalPages
    return { items, total, page, size, totalPages }
  } catch (error: any) {
    throw toError(error, "Failed to load teacher applications")
  }
}

export async function adminListTeacherApplications(params: TeacherApplicationListParams = {}) {
  try {
    const response = await client.get("/admin/teacher-applications", { params })
    return unwrapData<TeacherApplicationItem[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load admin applications")
  }
}

export async function adminListTeacherApplicationPage(params: {
  page?: number
  size?: number
  status?: TeacherApplicationStatus
  keyword?: string
}) {
  try {
    const response = await client.get("/admin/teacher-applications", { params })
    const payload = unwrapData<PageResponse<AdminTeacherApplicationListItem> | AdminTeacherApplicationListItem[]>(
      response?.data
    )
    const items = Array.isArray(payload) ? payload : payload?.content ?? []
    const total = Array.isArray(payload) ? undefined : payload?.totalElements
    const page = Array.isArray(payload) ? undefined : payload?.number
    const size = Array.isArray(payload) ? undefined : payload?.size
    const totalPages = Array.isArray(payload) ? undefined : payload?.totalPages
    return { items, total, page, size, totalPages }
  } catch (error: any) {
    throw toError(error, "Failed to load admin applications")
  }
}

export async function adminReviewTeacherApplication(
  applicationId: number,
  payload: TeacherApplicationReviewPayload
): Promise<TeacherApplicationItem> {
  try {
    const reviewComment = payload.reviewComment?.trim()
    const response = await client.put(`/admin/teacher-applications/${applicationId}/review`, {
      approved: payload.approved,
      reviewComment: reviewComment ? reviewComment : null
    })
    return unwrapData<TeacherApplicationItem>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to review application")
  }
}
