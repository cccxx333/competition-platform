import { client } from "@/api/client"

type LoginResponse = {
  token?: string
  data?: {
    token?: string
  }
}

export async function login(username: string, password: string): Promise<string> {
  const response = await client.post("/users/login", { username, password })
  const data = response?.data as LoginResponse | undefined
  const token = data?.token ?? data?.data?.token
  if (!token) {
    throw new Error("Token not found in response")
  }
  return token
}
