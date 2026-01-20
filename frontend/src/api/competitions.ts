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

export async function getCompetitionDetail(id: number | string): Promise<CompetitionDetail> {
  try {
    const response = await client.get(`/competitions/${id}`)
    return unwrapData<CompetitionDetail>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load competition detail")
  }
}
