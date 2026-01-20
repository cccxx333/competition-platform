<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import type { RouteRecordNameGeneric, RouteRecordRaw } from "vue-router"

interface Props {
  data: RouteRecordRaw[]
  isPressUpOrDown: boolean
}

const props = defineProps<Props>()

/** 閫変腑鐨勮彍鍗?*/
const modelValue = defineModel<RouteRecordNameGeneric | undefined>({ required: true })

const instance = getCurrentInstance()

const scrollbarHeight = ref<number>(0)

/** 鑿滃崟鐨勬牱寮?*/
function itemStyle(item: RouteRecordRaw) {
  const flag = item.name === modelValue.value
  return {
    background: flag ? "var(--el-color-primary)" : "",
    color: flag ? "#ffffff" : ""
  }
}

/** 榧犳爣绉诲叆 */
function handleMouseenter(item: RouteRecordRaw) {
  // 濡傛灉涓婇敭鎴栦笅閿笌 mouseenter 浜嬩欢鍚屾椂鐢熸晥锛屽垯浠ヤ笂涓嬮敭涓哄噯锛屼笉鎵ц璇ュ嚱鏁扮殑璧嬪€奸€昏緫
  if (props.isPressUpOrDown) return
  modelValue.value = item.name
}

/** 璁＄畻婊氬姩鍙鍖洪珮搴?*/
function getScrollbarHeight() {
  // el-scrollbar max-height="40vh"
  scrollbarHeight.value = Number((window.innerHeight * 0.4).toFixed(1))
}

/** 鏍规嵁涓嬫爣璁＄畻鍒伴《閮ㄧ殑璺濈 */
function getScrollTop(index: number) {
  const currentInstance = instance?.proxy?.$refs[`resultItemRef${index}`] as HTMLDivElement[]
  if (!currentInstance) return 0
  const currentRef = currentInstance[0]
  // 128 = 涓や釜 result-item 锛?6 + 56 = 112锛夐珮搴︿笌涓婁笅 margin锛? + 8 = 16锛夊ぇ灏忎箣鍜?
  const scrollTop = currentRef.offsetTop + 128
  return scrollTop > scrollbarHeight.value ? scrollTop - scrollbarHeight.value : 0
}

// 鍦ㄧ粍浠舵寕杞藉墠娣诲姞绐楀彛澶у皬鍙樺寲浜嬩欢鐩戝惉鍣?
onBeforeMount(() => {
  window.addEventListener("resize", getScrollbarHeight)
})

// 鍦ㄧ粍浠舵寕杞芥椂绔嬪嵆璁＄畻婊氬姩鍙鍖洪珮搴?
onMounted(() => {
  getScrollbarHeight()
})

// 鍦ㄧ粍浠跺嵏杞藉墠绉婚櫎绐楀彛澶у皬鍙樺寲浜嬩欢鐩戝惉鍣?
onBeforeUnmount(() => {
  window.removeEventListener("resize", getScrollbarHeight)
})

defineExpose({ getScrollTop })
</script>

<template>
  <!-- 澶栧眰 div 涓嶈兘鍒犻櫎锛屾槸鐢ㄦ潵鎺ユ敹鐖剁粍浠?click 浜嬩欢鐨?-->
  <div>
    <div
      v-for="(item, index) in props.data"
      :key="index"
      :ref="`resultItemRef${index}`"
      class="result-item"
      :style="itemStyle(item)"
      @mouseenter="handleMouseenter(item)"
    >
      <SvgIcon v-if="item.meta?.svgIcon" :name="item.meta.svgIcon" class="svg-icon" />
      <component v-else-if="item.meta?.elIcon" :is="item.meta.elIcon" class="el-icon" />
      <span class="result-item-title">
        {{ item.meta?.title }}
      </span>
      <SvgIcon v-if="modelValue && modelValue === item.name" name="keyboard-enter" class="svg-icon" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@@/assets/styles/mixins.scss";

.result-item {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 15px;
  margin-bottom: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  .svg-icon {
    min-width: 1em;
    font-size: 18px;
  }
  .el-icon {
    width: 1em;
    font-size: 18px;
  }
  &-title {
    flex: 1;
    margin-left: 12px;
    @extend %ellipsis;
  }
}
</style>

