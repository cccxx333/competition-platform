import type * as Auth from "./type"
import { client } from "@/api/client"

/** 获取登录验证码 */
export function getCaptchaApi() {
  return client.get<Auth.CaptchaResponseData>("auth/captcha")
}

/** 登录并返回 Token */
export function loginApi(data: Auth.LoginRequestData) {
  return client.post<Auth.LoginResponseData>("auth/login", data)
}
