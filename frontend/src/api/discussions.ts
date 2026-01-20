import { client } from "@/api/client"

export type TeamDiscussionPost = {
  id?: number
  teamId?: number
  authorId?: number
  parentPostId?: number | null
  content?: string
  createdAt?: string
  updatedAt?: string
  [key: string]: unknown
}

export type TeamDiscussionPostCreatePayload = {
  content: string
  parentPostId?: number | null
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

export async function listTeamPosts(teamId: number): Promise<TeamDiscussionPost[]> {
  try {
    const response = await client.get(`/teams/${teamId}/posts`)
    return unwrapData<TeamDiscussionPost[]>(response?.data) ?? []
  } catch (error: any) {
    throw toError(error, "Failed to load team posts")
  }
}

export async function createTeamPost(teamId: number, payload: TeamDiscussionPostCreatePayload): Promise<TeamDiscussionPost> {
  try {
    const response = await client.post(`/teams/${teamId}/posts`, payload)
    return unwrapData<TeamDiscussionPost>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to create post")
  }
}

export async function replyTeamPost(teamId: number, payload: TeamDiscussionPostCreatePayload): Promise<TeamDiscussionPost> {
  try {
    const response = await client.post(`/teams/${teamId}/posts`, payload)
    return unwrapData<TeamDiscussionPost>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to reply post")
  }
}

export async function deleteTeamPost(teamId: number, postId: number): Promise<void> {
  try {
    await client.delete(`/teams/${teamId}/posts/${postId}`)
  } catch (error: any) {
    throw toError(error, "Failed to delete post")
  }
}
