<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import type { RouterLink } from "vue-router"
import Screenfull from "@@/components/Screenfull/index.vue"
import { useRouteListener } from "@@/composables/useRouteListener"
import { ArrowLeft, ArrowRight } from "@element-plus/icons-vue"
import { useSettingsStore } from "@/pinia/stores/settings"

interface Props {
  tagRefs: InstanceType<typeof RouterLink>[] | null
}

const props = defineProps<Props>()

const route = useRoute()

const settingsStore = useSettingsStore()

const { listenerRouteChange } = useRouteListener()

/** 婊氬姩鏉＄粍浠跺厓绱犵殑寮曠敤 */
const scrollbarRef = useTemplateRef("scrollbarRef")

/** 婊氬姩鏉″唴瀹瑰厓绱犵殑寮曠敤 */
const scrollbarContentRef = useTemplateRef("scrollbarContentRef")

/** 褰撳墠婊氬姩鏉¤窛绂诲乏杈圭殑璺濈 */
let currentScrollLeft = 0

/** 姣忔婊氬姩璺濈 */
const translateDistance = 200

/** 婊氬姩鏃惰Е鍙?*/
function scroll({ scrollLeft }: { scrollLeft: number }) {
  currentScrollLeft = scrollLeft
}

/** 榧犳爣婊氳疆婊氬姩鏃惰Е鍙?*/
function wheelScroll({ deltaY }: WheelEvent) {
  if (deltaY.toString().startsWith("-")) {
    scrollTo("left")
  } else {
    scrollTo("right")
  }
}

/** 鑾峰彇鍙兘闇€瑕佺殑瀹藉害 */
function getWidth() {
  // 鍙粴鍔ㄥ唴瀹圭殑闀垮害
  const scrollbarContentRefWidth = scrollbarContentRef.value!.clientWidth
  // 婊氬姩鍙鍖哄搴?
  const scrollbarRefWidth = scrollbarRef.value!.wrapRef!.clientWidth
  // 鏈€鍚庡墿浣欏彲婊氬姩鐨勫搴?
  const lastDistance = scrollbarContentRefWidth - scrollbarRefWidth - currentScrollLeft

  return { scrollbarContentRefWidth, scrollbarRefWidth, lastDistance }
}

/** 宸﹀彸婊氬姩 */
function scrollTo(direction: "left" | "right", distance: number = translateDistance) {
  let scrollLeft = 0
  const { scrollbarContentRefWidth, scrollbarRefWidth, lastDistance } = getWidth()
  // 娌℃湁妯悜婊氬姩鏉★紝鐩存帴缁撴潫
  if (scrollbarRefWidth > scrollbarContentRefWidth) return
  if (direction === "left") {
    scrollLeft = Math.max(0, currentScrollLeft - distance)
  } else {
    scrollLeft = Math.min(currentScrollLeft + distance, currentScrollLeft + lastDistance)
  }
  scrollbarRef.value!.setScrollLeft(scrollLeft)
}

/** 绉诲姩鍒扮洰鏍囦綅缃?*/
function moveTo() {
  const tagRefs = props.tagRefs!
  for (let i = 0; i < tagRefs.length; i++) {
    // @ts-expect-error ignore
    if (route.path === tagRefs[i].$props.to.path) {
      // @ts-expect-error ignore
      const el: HTMLElement = tagRefs[i].$el
      const offsetWidth = el.offsetWidth
      const offsetLeft = el.offsetLeft
      const { scrollbarRefWidth } = getWidth()
      // 褰撳墠 tag 鍦ㄥ彲瑙嗗尯鍩熷乏杈规椂
      if (offsetLeft < currentScrollLeft) {
        const distance = currentScrollLeft - offsetLeft
        scrollTo("left", distance)
        return
      }
      // 褰撳墠 tag 鍦ㄥ彲瑙嗗尯鍩熷彸杈规椂
      const width = scrollbarRefWidth + currentScrollLeft - offsetWidth
      if (offsetLeft > width) {
        const distance = offsetLeft - width
        scrollTo("right", distance)
        return
      }
    }
  }
}

// 鐩戝惉璺敱鍙樺寲锛岀Щ鍔ㄥ埌鐩爣浣嶇疆
listenerRouteChange(() => {
  nextTick(moveTo)
})
</script>

<template>
  <div class="scroll-container">
    <el-tooltip content="Scroll left">
      <el-icon class="arrow left" @click="scrollTo('left')">
        <ArrowLeft />
      </el-icon>
    </el-tooltip>
    <el-scrollbar ref="scrollbarRef" @wheel.passive="wheelScroll" @scroll="scroll">
      <div ref="scrollbarContentRef" class="scrollbar-content">
        <slot />
      </div>
    </el-scrollbar>
    <el-tooltip content="Scroll right">
      <el-icon class="arrow right" @click="scrollTo('right')">
        <ArrowRight />
      </el-icon>
    </el-tooltip>
    <Screenfull v-if="settingsStore.showScreenfull" :content="true" class="screenfull" />
  </div>
</template>

<style lang="scss" scoped>
.scroll-container {
  height: 100%;
  user-select: none;
  display: flex;
  justify-content: space-between;
  .arrow {
    width: 40px;
    height: 100%;
    font-size: 18px;
    cursor: pointer;
    &.left {
      box-shadow: 5px 0 5px -6px var(--el-border-color-darker);
    }
    &.right {
      box-shadow: -5px 0 5px -6px var(--el-border-color-darker);
    }
  }
  .el-scrollbar {
    flex: 1;
    // 闃叉鎹㈣锛堣秴鍑哄搴︽椂锛屾樉绀烘粴鍔ㄦ潯锛?
    white-space: nowrap;
    .scrollbar-content {
      display: inline-block;
    }
  }
  .screenfull {
    width: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
  }
}
</style>

