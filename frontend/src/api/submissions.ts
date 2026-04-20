import { client } from "@/api/client"

export type TeamSubmission = {
  id?: number
  teamId?: number
  competitionId?: number
  submittedBy?: number
  submitterUsername?: string
  fileName?: string
  fileUrl?: string
  remark?: string
  submittedAt?: string
  isCurrent?: boolean
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
  if (status === 409 && typeof message === "string" && message.includes("disbanded")) {
    ;(err as any).isDisbanded = true
  }
  return err
}

export async function uploadSubmission(params: { teamId: number; file: File; remark?: string }): Promise<TeamSubmission> {
  try {
    const payload: Record<string, string | Blob> = { file: params.file }
    if (params.remark?.trim()) payload.remark = params.remark.trim()
    const response = await client.postForm(`/teams/${params.teamId}/submissions`, payload)
    return unwrapData<TeamSubmission>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to upload submission")
  }
}

export async function listSubmissions(teamId: number): Promise<TeamSubmission[]> {
  try {
    const response = await client.get(`/teams/${teamId}/submissions`)
    return unwrapData<TeamSubmission[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load submissions")
  }
}

