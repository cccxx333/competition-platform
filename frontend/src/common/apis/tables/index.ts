import type * as Tables from "./type"
import { client } from "@/api/client"

/** 增 */
export function createTableDataApi(data: Tables.CreateOrUpdateTableRequestData) {
  return client.post("tables", data)
}

/** 删 */
export function deleteTableDataApi(id: number) {
  return client.delete(`tables/${id}`)
}

/** 改 */
export function updateTableDataApi(data: Tables.CreateOrUpdateTableRequestData) {
  return client.put("tables", data)
}

/** 查 */
export function getTableDataApi(params: Tables.TableRequestData) {
  return client.get<Tables.TableResponseData>("tables", { params })
}
