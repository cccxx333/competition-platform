import dayjs from "dayjs"

const INVALID_DATE = "N/A"

/** 格式化日期时间 */
export function formatDateTime(datetime: string | number | Date = "", template: string = "YYYY-MM-DD HH:mm:ss") {
  const day = dayjs(datetime)
  return day.isValid() ? day.format(template) : INVALID_DATE
}

export function toYmd(input?: string | number | Date | null): string {
  if (input == null || input === "") return ""
  const text = String(input)
  return text.length >= 10 ? text.slice(0, 10) : text
}
