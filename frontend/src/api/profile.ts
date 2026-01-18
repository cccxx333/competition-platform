import { client } from "@/api/client"

export type UserProfile = {
  id?: number
  username?: string
  realName?: string
  email?: string
  school?: string
  major?: string
  grade?: string
  role?: string
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

export async function getMyProfile(): Promise<UserProfile> {
  try {
    const response = await client.get("/users/me")
    return unwrapData<UserProfile>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load profile")
  }
}
