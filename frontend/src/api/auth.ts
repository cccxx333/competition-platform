import { client } from "@/api/client"

type LoginResponse = {
  token?: string
  role?: string
  data?: {
    token?: string
    role?: string
  }
}

export async function login(username: string, password: string): Promise<{ token: string; role?: string }> {
  const response = await client.post("/users/login", { username, password })
  const data = response?.data as LoginResponse | undefined
  const token = data?.token ?? data?.data?.token
  const role = data?.role ?? data?.data?.role
  if (!token) {
    throw new Error("Token not found in response")
  }
  return { token, role }
}
