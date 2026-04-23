<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import { removeLayoutsConfig } from "@@/utils/cache/local-storage"
import { Refresh } from "@element-plus/icons-vue"
import { useSettingsStore } from "@/pinia/stores/settings"

const isLeft = { value: true }

const settingsStore = useSettingsStore()

// 使用 storeToRefs 保持提取属性的响应性
const {
  showTagsView,
  showLogo,
  fixedHeader,
  showFooter,
  showNotify,
  showThemeSwitch,
  showScreenfull,
  showSearchMenu,
  cacheTagsView,
  showWatermark,
  showGreyMode,
  showColorWeakness
} = storeToRefs(settingsStore)

/** Switch labels */
const switchSettings = {
  "Show Tags View": showTagsView,
  "Show Logo": showLogo,
  "Fixed Header": fixedHeader,
  "Show Footer": showFooter,
  "Show Notify": showNotify,
  "Show Theme Switch": showThemeSwitch,
  "Show Screenfull": showScreenfull,
  "Show Search Menu": showSearchMenu,
  "Cache Tags View": cacheTagsView,
  "Show Watermark": showWatermark,
  "Show Grey Mode": showGreyMode,
  "Show Color Weakness": showColorWeakness
}

// 非左侧布局模式时，Header 始终固定
watchEffect(() => {
  !isLeft.value && (fixedHeader.value = true)
})

/** 重置项目布局配置 */
function resetLayoutsConfig() {
  removeLayoutsConfig()
  location.reload()
}
</script>

<template>
  <div class="setting-container">
    <h4>Layout Settings</h4>
    <el-alert type="info" :closable="false" title="Legacy settings panel placeholder." />
    <el-divider />
    <h4>Feature Settings</h4>
    <div v-for="(settingValue, settingName, index) in switchSettings" :key="index" class="setting-item">
      <span class="setting-name">{{ settingName }}</span>
      <el-switch v-model="settingValue.value" :disabled="!isLeft && settingName === 'Fixed Header'" />
    </div>
    <el-button type="danger" :icon="Refresh" @click="resetLayoutsConfig">
      Reset
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
@import "@@/assets/styles/mixins.scss";

.setting-container {
  padding: 20px;
  .setting-item {
    font-size: 14px;
    color: var(--el-text-color-regular);
    padding: 5px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .setting-name {
      @extend %ellipsis;
    }
  }
  .el-button {
    margin-top: 40px;
    width: 100%;
  }
}
</style>

