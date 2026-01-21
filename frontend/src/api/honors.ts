import { client } from "@/api/client"

export type AwardDetail = {
  awardId?: number
  awardName?: string
  competitionId?: number
  teamId?: number
  publishedAt?: string
  [key: string]: unknown
}

export type UserHonorsResponse = {
  participationCount?: number
  awardCount?: number
  awards?: AwardDetail[]
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
  ;(err as any).rawMessage = message
  ;(err as any).responseData = error?.response?.data
  return err
}

export async function getMyHonors(): Promise<UserHonorsResponse> {
  try {
    const response = await client.get("/users/me/honors")
    return unwrapData<UserHonorsResponse>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load honors")
  }
}
