import type * as Users from "./type"
import { client } from "@/api/client"

/** 获取当前登录用户详情 */
export function getCurrentUserApi() {
  return client.get<Users.CurrentUserResponseData>("users/me")
}
