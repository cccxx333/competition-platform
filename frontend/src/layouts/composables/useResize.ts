/*
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
*/
import { useRouteListener } from "@@/composables/useRouteListener"
import { DeviceEnum } from "@@/constants/app-key"
import { useAppStore } from "@/pinia/stores/app"

/** 鍙傝€?Bootstrap 鐨勫搷搴斿紡璁捐灏嗘渶澶хЩ鍔ㄧ瀹藉害璁剧疆涓?992 */
const MAX_MOBILE_WIDTH = 992

/**
 * @name 娴忚鍣ㄥ搴﹀彉鍖?Composable
 * @description 鏍规嵁娴忚鍣ㄥ搴﹀彉鍖栵紝鍙樻崲 Layout 甯冨眬
 */
export function useResize() {
  const appStore = useAppStore()

  const { listenerRouteChange } = useRouteListener()

  // 鐢ㄤ簬鍒ゆ柇褰撳墠璁惧鏄惁涓虹Щ鍔ㄧ
  const isMobile = () => {
    const rect = document.body.getBoundingClientRect()
    return rect.width - 1 < MAX_MOBILE_WIDTH
  }

  // 鐢ㄤ簬澶勭悊绐楀彛澶у皬鍙樺寲浜嬩欢
  const resizeHandler = () => {
    if (!document.hidden) {
      const _isMobile = isMobile()
      appStore.toggleDevice(_isMobile ? DeviceEnum.Mobile : DeviceEnum.Desktop)
      _isMobile && appStore.closeSidebar(true)
    }
  }

  // 鐩戝惉璺敱鍙樺寲锛屾牴鎹澶囩被鍨嬭皟鏁村竷灞€
  listenerRouteChange(() => {
    if (appStore.device === DeviceEnum.Mobile && appStore.sidebar.opened) {
      appStore.closeSidebar(false)
    }
  })

  // 鍦ㄧ粍浠舵寕杞藉墠娣诲姞绐楀彛澶у皬鍙樺寲浜嬩欢鐩戝惉鍣?
  onBeforeMount(() => {
    window.addEventListener("resize", resizeHandler)
  })

  // 鍦ㄧ粍浠舵寕杞藉悗鏍规嵁绐楀彛澶у皬鍒ゆ柇璁惧绫诲瀷骞惰皟鏁村竷灞€
  onMounted(() => {
    if (isMobile()) {
      appStore.toggleDevice(DeviceEnum.Mobile)
      appStore.closeSidebar(true)
    }
  })

  // 鍦ㄧ粍浠跺嵏杞藉墠绉婚櫎绐楀彛澶у皬鍙樺寲浜嬩欢鐩戝惉鍣?
  onBeforeUnmount(() => {
    window.removeEventListener("resize", resizeHandler)
  })
}

