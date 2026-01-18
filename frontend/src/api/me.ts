import { client } from "@/api/client"

export type MeProfile = {
  id?: number
  username?: string
  role?: string
  [key: string]: unknown
}

export async function getMe(): Promise<MeProfile> {
  const response = await client.get("/users/me")
  return response?.data as MeProfile
}
