/*
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
*/
import { useRouteListener } from "@@/composables/useRouteListener"
import { DeviceEnum } from "@@/constants/app-key"
import { useAppStore } from "@/pinia/stores/app"

/** 参考 Bootstrap 响应式断点，移动端宽度阈值设为 992 */
const MAX_MOBILE_WIDTH = 992

/**
 * @name 浏览器宽度变化 Composable
 * @description 根据浏览器宽度变化切换布局状态
 */
export function useResize() {
  const appStore = useAppStore()

  const { listenerRouteChange } = useRouteListener()

  // 判断当前设备是否为移动端
  const isMobile = () => {
    const rect = document.body.getBoundingClientRect()
    return rect.width - 1 < MAX_MOBILE_WIDTH
  }

  // 处理窗口尺寸变化
  const resizeHandler = () => {
    if (!document.hidden) {
      const _isMobile = isMobile()
      appStore.toggleDevice(_isMobile ? DeviceEnum.Mobile : DeviceEnum.Desktop)
      _isMobile && appStore.closeSidebar(true)
    }
  }

  // 监听路由变化，在移动端自动关闭侧边栏
  listenerRouteChange(() => {
    if (appStore.device === DeviceEnum.Mobile && appStore.sidebar.opened) {
      appStore.closeSidebar(false)
    }
  })

  // 挂载前注册 resize 监听
  onBeforeMount(() => {
    window.addEventListener("resize", resizeHandler)
  })

  // 挂载后根据当前窗口宽度初始化设备类型
  onMounted(() => {
    if (isMobile()) {
      appStore.toggleDevice(DeviceEnum.Mobile)
      appStore.closeSidebar(true)
    }
  })

  // 卸载前移除 resize 监听
  onBeforeUnmount(() => {
    window.removeEventListener("resize", resizeHandler)
  })
}

