import { client } from "@/api/client"

export type UserProfile = {
  id?: number
  accountNo?: string
  role?: string
  username?: string
  realName?: string
  email?: string
  phone?: string
  avatarUrl?: string
  school?: string
  major?: string
  grade?: string
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

export async function getUserById(userId: number): Promise<UserProfile> {
  try {
    const response = await client.get(`/users/${userId}`)
    return unwrapData<UserProfile>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load user")
  }
}
