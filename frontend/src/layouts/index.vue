<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import { useDevice } from "@@/composables/useDevice"
import { useLayoutMode } from "@@/composables/useLayoutMode"
import { useWatermark } from "@@/composables/useWatermark"
import { getCssVar, setCssVar } from "@@/utils/css"
import { useSettingsStore } from "@/pinia/stores/settings"
import { RightPanel, Settings } from "./components"
import { useResize } from "./composables/useResize"
import LeftMode from "./modes/LeftMode.vue"
import LeftTopMode from "./modes/LeftTopMode.vue"
import TopMode from "./modes/TopMode.vue"

// Layout 甯冨眬鍝嶅簲寮?
useResize()

const { setWatermark, clearWatermark } = useWatermark()

const { isMobile } = useDevice()

const { isLeft, isTop, isLeftTop } = useLayoutMode()

const settingsStore = useSettingsStore()

const { showSettings, showTagsView, showWatermark } = storeToRefs(settingsStore)

// #region 闅愯棌鏍囩鏍忔椂鍒犻櫎鍏堕珮搴︼紝鏄负浜嗚 Logo 缁勪欢楂樺害鍜?Header 鍖哄煙楂樺害濮嬬粓涓€鑷?
const cssVarName = "--v3-tagsview-height"

const v3TagsviewHeight = getCssVar(cssVarName)

watchEffect(() => {
  showTagsView.value ? setCssVar(cssVarName, v3TagsviewHeight) : setCssVar(cssVarName, "0px")
})
// #endregion

// 寮€鍚垨鍏抽棴绯荤粺姘村嵃
watchEffect(() => {
  showWatermark.value ? setWatermark(import.meta.env.VITE_APP_TITLE) : clearWatermark()
})
</script>

<template>
  <div>
    <!-- 宸︿晶妯″紡 -->
    <LeftMode v-if="isLeft || isMobile" />
    <!-- 椤堕儴妯″紡 -->
    <TopMode v-else-if="isTop" />
    <!-- 娣峰悎妯″紡 -->
    <LeftTopMode v-else-if="isLeftTop" />
    <!-- 鍙充晶璁剧疆闈㈡澘 -->
    <RightPanel v-if="showSettings">
      <Settings />
    </RightPanel>
  </div>
</template>

