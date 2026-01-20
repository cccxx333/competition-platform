import { client } from "@/api/client"
import { getApiProxyTarget } from "@/utils/env"

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
    const form = new FormData()
    form.append("file", params.file)
    if (params.remark?.trim()) {
      form.append("remark", params.remark.trim())
    }
    const response = await client.post(`/teams/${params.teamId}/submissions`, form)
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

export const getSubmissionDownloadUrl = (item?: TeamSubmission | null): string => {
  const fileUrl = item?.fileUrl
  if (!fileUrl) return ""
  if (/^https?:\/\//i.test(fileUrl)) return fileUrl
  const base = import.meta.env.DEV ? getApiProxyTarget() : window.location.origin
  return new URL(fileUrl, base).toString()
}
