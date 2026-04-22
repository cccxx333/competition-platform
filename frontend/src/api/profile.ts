import { client } from "@/api/client"

export type UserProfile = {
  id?: number
  accountNo?: string
  username?: string
  displayName?: string
  realName?: string
  email?: string
  approvalStatus?: string
  school?: string
  major?: string
  grade?: string
  role?: string
  [key: string]: unknown
}

export type UserProfileUpdatePayload = {
  username?: string
  displayName?: string
  email?: string
  realName?: string
  phone?: string
  avatarUrl?: string
  school?: string
  major?: string
  grade?: string
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

export async function updateMyProfile(payload: UserProfileUpdatePayload): Promise<UserProfile> {
  try {
    const response = await client.put("/users/me", payload)
    return unwrapData<UserProfile>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to update profile")
  }
}
