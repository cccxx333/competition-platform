import { client } from "@/api/client"

export type ApplicationStatus = "PENDING" | "APPROVED" | "REJECTED" | "REMOVED"

export type ApplicationItem = {
  id?: number
  competitionId?: number
  teamId?: number
  studentId?: number
  status?: ApplicationStatus
  reason?: string | null
  appliedAt?: string
  reviewedAt?: string
  reviewedBy?: number
  isActive?: boolean
  [key: string]: unknown
}

export type TeamDto = {
  id?: number
  name?: string
  status?: string
  description?: string
  [key: string]: unknown
}

const unwrapData = <T>(payload: any): T => {
  return (payload?.data ?? payload) as T
}

const toError = (error: any, fallback: string) => {
  const status = error?.response?.status
  const message = error?.response?.data?.message ?? error?.response?.data?.reason ?? fallback
  const err = new Error(message)
  ;(err as any).status = status
  ;(err as any).rawMessage = message
  ;(err as any).responseData = error?.response?.data
  return err
}

export async function createApplication(payload: { competitionId: number; teamId: number }): Promise<ApplicationItem> {
  try {
    const response = await client.post("/applications", payload)
    return unwrapData<ApplicationItem>(response?.data)
  } catch (error: any) {
    throw toError(error, "Request failed")
  }
}

export async function listMyApplications(): Promise<ApplicationItem[]> {
  try {
    const response = await client.get("/users/me/applications")
    return unwrapData<ApplicationItem[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load applications")
  }
}

export async function getMyTeam(): Promise<TeamDto | null> {
  try {
    const response = await client.get("/users/me/team")
    return unwrapData<TeamDto | null>(response?.data) ?? null
  } catch (error: any) {
    throw toError(error, "Failed to load team")
  }
}

export async function listPendingApplications(params: { status?: string; teamId?: number } = {}): Promise<ApplicationItem[]> {
  try {
    const response = await client.get("/teacher/applications", {
      params: { status: params.status ?? "PENDING", teamId: params.teamId }
    })
    return unwrapData<ApplicationItem[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load pending applications")
  }
}

export async function reviewApplication(
  id: number,
  payload: { approved: boolean; reason?: string }
): Promise<ApplicationItem> {
  try {
    const reason = payload.reason?.trim()
    const response = await client.put(`/teacher/applications/${id}/review`, {
      approved: payload.approved,
      reason: reason ? reason : null
    })
    return unwrapData<ApplicationItem>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to review application")
  }
}
