import { client } from "@/api/client"

export type AwardPublishRequest = {
  competitionId: number
  teamId: number
  awardName: string
}

export type AwardPublishResponse = {
  awardId?: number
  competitionId?: number
  teamId?: number
  awardName?: string
  recipientCount?: number
  recipientUserIds?: number[]
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

export async function publishAward(payload: AwardPublishRequest): Promise<AwardPublishResponse> {
  try {
    const response = await client.post("/admin/awards", payload)
    return unwrapData<AwardPublishResponse>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to publish award")
  }
}
