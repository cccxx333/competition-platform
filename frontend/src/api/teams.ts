import { client } from "@/api/client"

export type TeamStatus = "RECRUITING" | "CLOSED" | "DISBANDED" | string

export type TeamLeader = {
  id?: number
  username?: string
  realName?: string
  avatarUrl?: string
  [key: string]: unknown
}

export type TeamCompetition = {
  id?: number
  name?: string
  status?: string
  maxTeamSize?: number
  registrationDeadline?: string
  [key: string]: unknown
}

export type TeamSkill = {
  skillId?: number
  skillName?: string
  weight?: number
  [key: string]: unknown
}

export type TeamDto = {
  id?: number
  name?: string
  description?: string
  status?: TeamStatus
  createdAt?: string
  maxMembers?: number
  currentMembers?: number
  leader?: TeamLeader | null
  competition?: TeamCompetition | null
  teamSkills?: TeamSkill[]
  [key: string]: unknown
}

export type TeamMemberView = {
  userId?: number
  username?: string
  realName?: string
  role?: string
  joinedAt?: string
  [key: string]: unknown
}

export type TeamAwardSummary = {
  hasAward?: boolean
  awardId?: number
  competitionId?: number
  teamId?: number
  awardName?: string
  publishedAt?: string
  [key: string]: unknown
}

export type TeamListResult = {
  items: TeamDto[]
  total?: number
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

export async function getMyTeam(): Promise<TeamDto | null> {
  try {
    const response = await client.get("/users/me/team")
    return unwrapData<TeamDto | null>(response?.data) ?? null
  } catch (error: any) {
    throw toError(error, "Failed to load team")
  }
}

export async function getTeamDetail(teamId: number): Promise<TeamDto> {
  try {
    const response = await client.get(`/teams/${teamId}`)
    return unwrapData<TeamDto>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load team detail")
  }
}

export async function getTeamAwardSummary(teamId: number): Promise<TeamAwardSummary> {
  try {
    const response = await client.get(`/teams/${teamId}/award`)
    return unwrapData<TeamAwardSummary>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load team award")
  }
}

export async function listTeamMembers(teamId: number): Promise<TeamMemberView[]> {
  try {
    const response = await client.get(`/teams/${teamId}/members`)
    return unwrapData<TeamMemberView[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load team members")
  }
}

export async function closeTeam(teamId: number): Promise<TeamDto> {
  try {
    const response = await client.put(`/teams/${teamId}/close`)
    return unwrapData<TeamDto>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to close team")
  }
}

export async function removeMember(teamId: number, userId: number, reason?: string): Promise<void> {
  try {
    await client.delete(`/teams/${teamId}/members/${userId}`, {
      params: { reason: reason?.trim() ? reason : undefined }
    })
  } catch (error: any) {
    throw toError(error, "Failed to remove member")
  }
}

export async function listTeams(params: { keyword?: string; page?: number; size?: number } = {}): Promise<TeamListResult> {
  try {
    if (params.keyword?.trim()) {
      const response = await client.get("/teams/search", { params: { keyword: params.keyword.trim() } })
      const items = unwrapData<TeamDto[]>(response?.data) ?? []
      return { items, total: items.length }
    }
    const response = await client.get("/teams", {
      params: { page: params.page ?? 0, size: params.size ?? 20 }
    })
    const payload = unwrapData<any>(response?.data)
    const items = (payload?.content ?? payload ?? []) as TeamDto[]
    const total = typeof payload?.totalElements === "number" ? payload.totalElements : items.length
    return { items, total }
  } catch (error: any) {
    throw toError(error, "Failed to load teams")
  }
}

export async function disbandTeam(teamId: number): Promise<TeamDto> {
  try {
    const response = await client.put(`/teams/${teamId}/disband`)
    return unwrapData<TeamDto>(response?.data)
  } catch (error: any) {
    throw toError(error, "解散队伍失败")
  }
}

export async function listMyTeams(params: { keyword?: string } = {}): Promise<TeamDto[]> {
  try {
    const response = await client.get("/teams/mine", {
      params: params.keyword?.trim() ? { keyword: params.keyword.trim() } : undefined
    })
    return unwrapData<TeamDto[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load my teams")
  }
}
