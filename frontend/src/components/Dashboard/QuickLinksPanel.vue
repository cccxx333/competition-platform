<script lang="ts" setup>
type QuickLinkItem = {
  label: string
  path: string
}

const props = defineProps<{
  title?: string
  items: QuickLinkItem[]
}>()
</script>

<template>
  <div class="quick-links-panel">
    <div class="quick-links-title">{{ props.title || "常用入口" }}</div>
    <el-card shadow="never" class="cp-card quick-links-card">
      <div class="quick-links-list">
        <router-link
          v-for="item in props.items"
          :key="`${item.path}-${item.label}`"
          :to="item.path"
          class="quick-link-item"
        >
          {{ item.label }}
        </router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.quick-links-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-links-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.quick-links-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-link-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  color: #303133;
  text-decoration: none;
  background: #ffffff;
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.quick-link-item:hover {
  border-color: #a0cfff;
  color: #409eff;
  background: #ecf5ff;
}
</style>
