import { client } from "@/api/client"

export type CompetitionListItem = {
  id?: number
  name?: string
  description?: string
  organizer?: string
  startDate?: string
  endDate?: string
  registrationDeadline?: string
  minTeamSize?: number
  maxTeamSize?: number
  category?: string
  level?: string
  status?: "UPCOMING" | "ONGOING" | "FINISHED"
  createdById?: number
  createdAt?: string
  updatedAt?: string
  matchScore?: number
  recommend?: boolean
  recommendReason?: string
  [key: string]: unknown
}

export type CompetitionDetail = CompetitionListItem

type CompetitionPage = {
  content?: CompetitionListItem[]
  totalElements?: number
  number?: number
  size?: number
}

export type CompetitionListParams = {
  keyword?: string
  status?: CompetitionListItem["status"]
  page?: number
  size?: number
  recommend?: boolean
  topK?: number
  applyable?: boolean
}

export type CompetitionAdminUpdatePayload = {
  status?: CompetitionListItem["status"]
  startDate?: string
  endDate?: string
  registrationDeadline?: string
  description?: string
}

export type CompetitionCreatePayload = {
  name: string
  organizer?: string
  level?: string
  startDate: string
  endDate: string
  registrationDeadline: string
  minTeamSize: number
  maxTeamSize: number
  description?: string
}

export type ApplyableCompetitionItem = {
  id?: number
  name?: string
  registrationDeadline?: string
  status?: CompetitionListItem["status"]
  [key: string]: unknown
}

export type TeamRecommendReason = {
  skillId?: number
  skillName?: string
  weight?: number
  [key: string]: unknown
}

export type TeamRecommendation = {
  teamId?: number
  teamName?: string
  teamStatus?: "RECRUITING" | "CLOSED" | "DISBANDED" | string
  matchScore?: number
  reasons?: TeamRecommendReason[]
  fallbackSorted?: boolean
  [key: string]: unknown
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

export async function listCompetitions(params: CompetitionListParams) {
  try {
    const response = await client.get("/competitions", { params })
    const payload = unwrapData<CompetitionPage | CompetitionListItem[]>(response?.data)
    const items = Array.isArray(payload) ? payload : payload?.content ?? []
    const total = Array.isArray(payload) ? undefined : payload?.totalElements
    const page = Array.isArray(payload) ? undefined : payload?.number
    const size = Array.isArray(payload) ? undefined : payload?.size
    return { items, total, page, size }
  } catch (error: any) {
    throw toError(error, "Failed to load competitions")
  }
}

export async function listApplyableCompetitions(keyword: string, page = 0, size = 20) {
  try {
    const trimmed = keyword?.trim()
    const response = await client.get("/competitions", {
      params: {
        applyable: true,
        keyword: trimmed ? trimmed : undefined,
        page,
        size
      }
    })
    const payload = unwrapData<CompetitionPage | ApplyableCompetitionItem[]>(response?.data)
    const items = Array.isArray(payload) ? payload : payload?.content ?? []
    return items as ApplyableCompetitionItem[]
  } catch (error: any) {
    throw toError(error, "加载可报名竞赛失败")
  }
}

export async function getRecommendedTeams(competitionId: number, topK = 10): Promise<TeamRecommendation[]> {
  try {
    const response = await client.get(`/competitions/${competitionId}/teams/recommend`, {
      params: { topK }
    })
    return unwrapData<TeamRecommendation[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "加载推荐队伍失败")
  }
}

export async function getCompetitionDetail(id: number | string): Promise<CompetitionDetail> {
  try {
    const response = await client.get(`/competitions/${id}`)
    return unwrapData<CompetitionDetail>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load competition detail")
  }
}

export async function createCompetition(payload: CompetitionCreatePayload): Promise<CompetitionDetail> {
  try {
    const response = await client.post("/competitions", payload)
    return unwrapData<CompetitionDetail>(response?.data)
  } catch (error: any) {
    throw toError(error, "发布竞赛失败")
  }
}

export async function updateCompetitionAdmin(
  id: number,
  payload: CompetitionAdminUpdatePayload
): Promise<CompetitionDetail> {
  try {
    const response = await client.put(`/admin/competitions/${id}`, payload)
    return unwrapData<CompetitionDetail>(response?.data)
  } catch (error: any) {
    throw toError(error, "更新竞赛失败")
  }
}

export async function updateCompetition(
  id: number,
  payload: CompetitionAdminUpdatePayload
): Promise<CompetitionDetail> {
  try {
    const response = await client.put(`/competitions/${id}`, payload)
    return unwrapData<CompetitionDetail>(response?.data)
  } catch (error: any) {
    throw toError(error, "更新竞赛失败")
  }
}

export async function updateCompetitionStatus(
  id: number,
  status: CompetitionListItem["status"]
): Promise<CompetitionDetail> {
  return updateCompetition(id, { status })
}
