import { client } from "@/api/client"

export type UserSkill = {
  id?: number
  userId?: number
  skillId?: number
  skillName?: string
  skillCategory?: string
  proficiency?: number
  [key: string]: unknown
}

export type Skill = {
  id?: number
  name?: string
  category?: string
  description?: string
  isActive?: number
  createdAt?: string
  [key: string]: unknown
}

export type SkillPayload = {
  name: string
  category?: string
  description?: string
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

export async function listMySkills(): Promise<UserSkill[]> {
  try {
    const response = await client.get("/users/me/skills")
    return unwrapData<UserSkill[]>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load skills")
  }
}

export async function listSkills(keyword?: string): Promise<Skill[]> {
  try {
    const response = keyword
      ? await client.get("/skills/search", { params: { keyword } })
      : await client.get("/skills")
    return unwrapData<Skill[]>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to load skills")
  }
}

export async function createSkill(payload: SkillPayload): Promise<Skill> {
  try {
    const response = await client.post("/skills", payload)
    return unwrapData<Skill>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to create skill")
  }
}

export async function updateSkill(id: number, payload: SkillPayload): Promise<Skill> {
  try {
    const response = await client.put(`/skills/${id}`, payload)
    return unwrapData<Skill>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to update skill")
  }
}

export async function bindMySkill(skillId: number, level?: number): Promise<UserSkill> {
  try {
    const response = await client.post("/users/me/skills", { skillId, level })
    return unwrapData<UserSkill>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to bind skill")
  }
}

export async function updateMySkillLevel(skillId: number, level: number): Promise<UserSkill> {
  try {
    const response = await client.put(`/users/me/skills/${skillId}/level`, { level })
    return unwrapData<UserSkill>(response?.data)
  } catch (error: any) {
    throw toError(error, "Failed to update skill level")
  }
}

export async function unbindMySkill(skillId: number): Promise<void> {
  try {
    await client.delete(`/users/me/skills/${skillId}`)
  } catch (error: any) {
    throw toError(error, "Failed to unbind skill")
  }
}
