<script lang="ts" setup>
type StatusKind = "competition" | "teacherApplication"

const props = withDefaults(
  defineProps<{
    value?: string | null
    kind?: StatusKind
  }>(),
  {
    kind: undefined,
    value: ""
  }
)

const normalizedValue = computed(() => String(props.value || "").toUpperCase())

const resolvedKind = computed<StatusKind | "unknown">(() => {
  if (props.kind) return props.kind
  if (["UPCOMING", "ONGOING", "FINISHED"].includes(normalizedValue.value)) {
    return "competition"
  }
  if (["PENDING", "APPROVED", "REJECTED"].includes(normalizedValue.value)) {
    return "teacherApplication"
  }
  return "unknown"
})

const colorClass = computed(() => {
  const value = normalizedValue.value
  if (resolvedKind.value === "competition") {
    if (value === "UPCOMING") return "status-pill--orange"
    if (value === "ONGOING") return "status-pill--green"
    if (value === "FINISHED") return "status-pill--gray"
  }
  if (resolvedKind.value === "teacherApplication") {
    if (value === "PENDING") return "status-pill--orange"
    if (value === "APPROVED") return "status-pill--green"
    if (value === "REJECTED") return "status-pill--red"
  }
  return "status-pill--gray"
})
</script>

<template>
  <span v-if="normalizedValue" class="status-pill" :class="colorClass">
    {{ normalizedValue }}
  </span>
</template>

<style scoped>
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 28px;
  padding: 0 14px;
  min-width: 96px;
  border-radius: 9999px;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-size: 12px;
  box-sizing: border-box;
  border: 1px solid transparent;
}

.status-pill--orange {
  background-color: #fff4e5;
  color: #d97706;
  border-color: #fde5c0;
}

.status-pill--green {
  background-color: #eaf6ef;
  color: #2e7d32;
  border-color: #cfead8;
}

.status-pill--gray {
  background-color: #f2f3f5;
  color: #6b7280;
  border-color: #e5e7eb;
}

.status-pill--red {
  background-color: #fdecec;
  color: #c62828;
  border-color: #f9caca;
}
</style>
