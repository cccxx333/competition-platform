import { client } from "@/api/client"

export type StudentDashboardStats = {
  pendingTeacherReviewCount?: number
  ongoingCompetitionCount?: number
  participationCount?: number
  awardCount?: number
}

export type AdminDashboardStats = {
  pendingTeacherUserCount?: number
  finishedPendingAwardCompetitionCount?: number
  ongoingCompetitionCount?: number
  competitionTotalCount?: number
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

export async function getStudentDashboardStats(): Promise<StudentDashboardStats> {
  try {
    const response = await client.get("/dashboard/student-stats")
    return unwrapData<StudentDashboardStats>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load student dashboard stats")
  }
}

export async function getAdminDashboardStats(): Promise<AdminDashboardStats> {
  try {
    const response = await client.get("/dashboard/admin-stats")
    return unwrapData<AdminDashboardStats>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load admin dashboard stats")
  }
}
