import { client } from "@/api/client"

type LoginResponse = {
  token?: string
  role?: string
  data?: {
    token?: string
    role?: string
  }
}

type RegisterPayload = {
  username: string
  email: string
  password: string
  realName?: string
  school?: string
  major?: string
  grade?: string
  phone?: string
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

export async function register(payload: RegisterPayload): Promise<void> {
  await client.post("/users/register", payload)
}
