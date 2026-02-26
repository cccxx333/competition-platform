<script lang="ts" setup>
type StatusKind = "competition" | "teacherApplication" | "teamApplication" | "team"

const props = withDefaults(
  defineProps<{
    status?: string | null
    kind?: StatusKind
  }>(),
  {
    status: "",
    kind: undefined
  }
)

const normalizedStatus = computed(() => String(props.status ?? "").toUpperCase())

const resolvedKind = computed<StatusKind | "unknown">(() => {
  if (props.kind) return props.kind
  const value = normalizedStatus.value
  if (["UPCOMING", "ONGOING", "FINISHED"].includes(value)) return "competition"
  if (["PENDING", "APPROVED", "REJECTED"].includes(value)) return "teacherApplication"
  if (["REMOVED"].includes(value)) return "teamApplication"
  if (["RECRUITING", "CLOSED", "DISBANDED"].includes(value)) return "team"
  return "unknown"
})

const label = computed(() => {
  const value = normalizedStatus.value
  if (resolvedKind.value === "competition") {
    if (value === "UPCOMING") return "未开始"
    if (value === "ONGOING") return "进行中"
    if (value === "FINISHED") return "已结束"
  }
  if (resolvedKind.value === "teacherApplication") {
    if (value === "PENDING") return "待审核"
    if (value === "APPROVED") return "已通过"
    if (value === "REJECTED") return "已拒绝"
  }
  if (resolvedKind.value === "teamApplication") {
    if (value === "PENDING") return "待审核"
    if (value === "APPROVED") return "已通过"
    if (value === "REJECTED") return "已拒绝"
    if (value === "REMOVED") return "已移除"
  }
  if (resolvedKind.value === "team") {
    if (value === "RECRUITING") return "招募中"
    if (value === "CLOSED") return "已关闭"
    if (value === "DISBANDED") return "已解散"
  }
  return value ? "未知" : "-"
})

const tone = computed(() => {
  const value = normalizedStatus.value
  if (["APPROVED", "ONGOING", "RECRUITING"].includes(value)) return "success"
  if (["REJECTED", "DISBANDED"].includes(value)) return "danger"
  if (["REMOVED", "FINISHED", "CLOSED"].includes(value)) return "info"
  if (["PENDING", "UPCOMING"].includes(value)) return "warning"
  return "neutral"
})
</script>

<template>
  <span v-if="label" class="cp-status-tag" :class="`cp-status-tag--${tone}`">
    {{ label }}
  </span>
</template>

<style scoped>
.cp-status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
  line-height: 1.4;
  white-space: nowrap;
}

.cp-status-tag--success {
  color: #2e7d32;
  background: #eef7f1;
  border-color: #cfead8;
}

.cp-status-tag--warning {
  color: #b45309;
  background: #fff6e6;
  border-color: #fde5c0;
}

.cp-status-tag--danger {
  color: #c62828;
  background: #fdecec;
  border-color: #f9caca;
}

.cp-status-tag--info,
.cp-status-tag--neutral {
  color: #6b7280;
  background: #f2f3f5;
  border-color: #e5e7eb;
}
</style>
