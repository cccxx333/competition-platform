<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import { useLayoutMode } from "@@/composables/useLayoutMode"
import { removeLayoutsConfig } from "@@/utils/cache/local-storage"
import { Refresh } from "@element-plus/icons-vue"
import { useSettingsStore } from "@/pinia/stores/settings"
import SelectLayoutMode from "./SelectLayoutMode.vue"

const { isLeft } = useLayoutMode()

const settingsStore = useSettingsStore()

// 浣跨敤 storeToRefs 灏嗘彁鍙栫殑灞炴€т繚鎸佸叾鍝嶅簲鎬?
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

// 闈炲乏渚фā寮忔椂锛孒eader 閮芥槸 fixed 甯冨眬
watchEffect(() => {
  !isLeft.value && (fixedHeader.value = true)
})

/** 閲嶇疆椤圭洰閰嶇疆 */
function resetLayoutsConfig() {
  removeLayoutsConfig()
  location.reload()
}
</script>

<template>
  <div class="setting-container">
    <h4>Layout Settings</h4>
    <SelectLayoutMode />
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

