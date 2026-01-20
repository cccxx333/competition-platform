<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import type { RouteRecordNameGeneric, RouteRecordRaw } from "vue-router"
import { useDevice } from "@@/composables/useDevice"
import { isExternal } from "@@/utils/validate"
import { cloneDeep, debounce } from "lodash-es"
import { usePermissionStore } from "@/pinia/stores/permission"
import Footer from "./Footer.vue"
import Result from "./Result.vue"

/** 鎺у埗 modal 鏄鹃殣 */
const modelValue = defineModel<boolean>({ required: true })

const router = useRouter()

const { isMobile } = useDevice()

const inputRef = useTemplateRef("inputRef")

const scrollbarRef = useTemplateRef("scrollbarRef")

const resultRef = useTemplateRef("resultRef")

const keyword = ref<string>("")

const result = shallowRef<RouteRecordRaw[]>([])

const activeRouteName = ref<RouteRecordNameGeneric | undefined>(undefined)

/** 鏄惁鎸変笅浜嗕笂閿垨涓嬮敭锛堢敤浜庤В鍐冲拰 mouseenter 浜嬩欢鐨勫啿绐侊級 */
const isPressUpOrDown = ref<boolean>(false)

/** 鎺у埗鎼滅储瀵硅瘽妗嗗搴?*/
const modalWidth = computed(() => (isMobile.value ? "80vw" : "40vw"))

/** 鏍戝舰鑿滃崟 */
const menus = computed(() => cloneDeep(usePermissionStore().routes))

/** 鎼滅储锛堥槻鎶栵級 */
const handleSearch = debounce(() => {
  const flatMenus = flatTree(menus.value)
  const _keywords = keyword.value.toLocaleLowerCase().trim()
  result.value = flatMenus.filter(menu => keyword.value ? menu.meta?.title?.toLocaleLowerCase().includes(_keywords) : false)
  // 榛樿閫変腑鎼滅储缁撴灉鐨勭涓€椤?
  const length = result.value?.length
  activeRouteName.value = length > 0 ? result.value[0].name : undefined
}, 500)

/** 灏嗘爲褰㈣彍鍗曟墎骞冲寲涓轰竴缁存暟缁勶紝鐢ㄤ簬鑿滃崟鎼滅储 */
function flatTree(arr: RouteRecordRaw[], result: RouteRecordRaw[] = []) {
  arr.forEach((item) => {
    result.push(item)
    item.children && flatTree(item.children, result)
  })
  return result
}

/** 鍏抽棴鎼滅储瀵硅瘽妗?*/
function handleClose() {
  modelValue.value = false
  // 寤舵椂澶勭悊闃叉鐢ㄦ埛鐪嬪埌閲嶇疆鏁版嵁鐨勬搷浣?
  setTimeout(() => {
    keyword.value = ""
    result.value = []
  }, 200)
}

/** 鏍规嵁涓嬫爣浣嶇疆杩涜婊氬姩 */
function scrollTo(index: number) {
  if (!resultRef.value) return
  const scrollTop = resultRef.value.getScrollTop(index)
  // 鎵嬪姩鎺у埗 el-scrollbar 婊氬姩鏉℃粴鍔紝璁剧疆婊氬姩鏉″埌椤堕儴鐨勮窛绂?
  scrollbarRef.value?.setScrollTop(scrollTop)
}

/** 閿洏涓婇敭 */
function handleUp() {
  isPressUpOrDown.value = true
  const { length } = result.value
  if (length === 0) return
  // 鑾峰彇璇?name 鍦ㄨ彍鍗曚腑绗竴娆″嚭鐜扮殑浣嶇疆
  const index = result.value.findIndex(item => item.name === activeRouteName.value)
  // 濡傛灉宸插鍦ㄩ《閮?
  if (index === 0) {
    const bottomName = result.value[length - 1].name
    // 濡傛灉椤堕儴鍜屽簳閮ㄧ殑 bottomName 鐩稿悓锛屼笖闀垮害澶т簬 1锛屽氨鍐嶈烦涓€涓綅缃紙鍙В鍐抽亣鍒伴灏句袱涓浉鍚?name 瀵艰嚧鐨勪笂閿笉鑳界敓鏁堢殑闂锛?
    if (activeRouteName.value === bottomName && length > 1) {
      activeRouteName.value = result.value[length - 2].name
      scrollTo(length - 2)
    } else {
      // 璺宠浆鍒板簳閮?
      activeRouteName.value = bottomName
      scrollTo(length - 1)
    }
  } else {
    activeRouteName.value = result.value[index - 1].name
    scrollTo(index - 1)
  }
}

/** 閿洏涓嬮敭 */
function handleDown() {
  isPressUpOrDown.value = true
  const { length } = result.value
  if (length === 0) return
  // 鑾峰彇璇?name 鍦ㄨ彍鍗曚腑鏈€鍚庝竴娆″嚭鐜扮殑浣嶇疆锛堝彲瑙ｅ喅閬囧埌杩炵画涓や釜鐩稿悓 name 瀵艰嚧鐨勪笅閿笉鑳界敓鏁堢殑闂锛?
  const index = result.value.map(item => item.name).lastIndexOf(activeRouteName.value)
  // 濡傛灉宸插鍦ㄥ簳閮?
  if (index === length - 1) {
    const topName = result.value[0].name
    // 濡傛灉搴曢儴鍜岄《閮ㄧ殑 topName 鐩稿悓锛屼笖闀垮害澶т簬 1锛屽氨鍐嶈烦涓€涓綅缃紙鍙В鍐抽亣鍒伴灏句袱涓浉鍚?name 瀵艰嚧鐨勪笅閿笉鑳界敓鏁堢殑闂锛?
    if (activeRouteName.value === topName && length > 1) {
      activeRouteName.value = result.value[1].name
      scrollTo(1)
    } else {
      // 璺宠浆鍒伴《閮?
      activeRouteName.value = topName
      scrollTo(0)
    }
  } else {
    activeRouteName.value = result.value[index + 1].name
    scrollTo(index + 1)
  }
}

/** 閿洏鍥炶溅閿?*/
function handleEnter() {
  const { length } = result.value
  if (length === 0) return
  const name = activeRouteName.value
  const path = result.value.find(item => item.name === name)?.path
  if (path && isExternal(path)) return window.open(path, "_blank", "noopener, noreferrer")
  if (!name) return ElMessage.warning("鏃犳硶閫氳繃鎼滅储杩涘叆璇ヨ彍鍗曪紝璇蜂负瀵瑰簲鐨勮矾鐢辫缃敮涓€鐨?Name")
  try {
    router.push({ name })
  } catch {
    return ElMessage.warning("璇ヨ彍鍗曟湁蹇呭～鐨勫姩鎬佸弬鏁帮紝鏃犳硶閫氳繃鎼滅储杩涘叆")
  }
  handleClose()
}

/** 閲婃斁涓婇敭鎴栦笅閿?*/
function handleReleaseUpOrDown() {
  isPressUpOrDown.value = false
}
</script>

<template>
  <el-dialog
    v-model="modelValue"
    :before-close="handleClose"
    :width="modalWidth"
    top="5vh"
    class="search-modal__private"
    append-to-body
    @opened="inputRef?.focus()"
    @closed="inputRef?.blur()"
    @keydown.up="handleUp"
    @keydown.down="handleDown"
    @keydown.enter="handleEnter"
    @keyup.up.down="handleReleaseUpOrDown"
  >
    <el-input ref="inputRef" v-model="keyword" placeholder="鎼滅储鑿滃崟" size="large" clearable @input="handleSearch">
      <template #prefix>
        <SvgIcon name="search" class="svg-icon" />
      </template>
    </el-input>
    <el-empty v-if="result.length === 0" description="鏆傛棤鎼滅储缁撴灉" :image-size="100" />
    <template v-else>
      <p>鎼滅储缁撴灉</p>
      <el-scrollbar ref="scrollbarRef" max-height="40vh" always>
        <Result
          ref="resultRef"
          v-model="activeRouteName"
          :data="result"
          :is-press-up-or-down="isPressUpOrDown"
          @click="handleEnter"
        />
      </el-scrollbar>
    </template>
    <template #footer>
      <Footer :total="result.length" />
    </template>
  </el-dialog>
</template>

<style lang="scss">
.search-modal__private {
  .svg-icon {
    font-size: 18px;
  }
  .el-dialog__header {
    display: none;
  }
  .el-dialog__footer {
    border-top: 1px solid var(--el-border-color);
    padding-top: var(--el-dialog-padding-primary);
  }
}
</style>

