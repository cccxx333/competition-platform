<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import type { RouteLocationMatched } from "vue-router"
import { useRouteListener } from "@@/composables/useRouteListener"
import { compile } from "path-to-regexp"

const route = useRoute()

const router = useRouter()

const { listenerRouteChange } = useRouteListener()

/** 瀹氫箟鍝嶅簲寮忔暟鎹?breadcrumbs锛岀敤浜庡瓨鍌ㄩ潰鍖呭睉瀵艰埅淇℃伅 */
const breadcrumbs = ref<RouteLocationMatched[]>([])

/** 鑾峰彇闈㈠寘灞戝鑸俊鎭?*/
function getBreadcrumb() {
  breadcrumbs.value = route.matched.filter(item => item.meta?.title && item.meta?.breadcrumb !== false)
}

/** 缂栬瘧璺敱璺緞 */
function pathCompile(path: string) {
  const toPath = compile(path)
  return toPath(route.params)
}

/** 澶勭悊闈㈠寘灞戝鑸偣鍑讳簨浠?*/
function handleLink(item: RouteLocationMatched) {
  const { redirect, path } = item
  if (redirect) return router.push(redirect as string)
  router.push(pathCompile(path))
}

// 鐩戝惉璺敱鍙樺寲锛屾洿鏂伴潰鍖呭睉瀵艰埅淇℃伅
listenerRouteChange((route) => {
  if (route.path.startsWith("/redirect/")) return
  getBreadcrumb()
}, true)
</script>

<template>
  <el-breadcrumb>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
      <span v-if="item.redirect === 'noRedirect' || index === breadcrumbs.length - 1" class="no-redirect">
        {{ item.meta.title }}
      </span>
      <a v-else @click.prevent="handleLink(item)">
        {{ item.meta.title }}
      </a>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style lang="scss" scoped>
.el-breadcrumb {
  line-height: var(--v3-navigationbar-height);
  .no-redirect {
    color: var(--el-text-color-placeholder);
  }
  a {
    font-weight: normal;
  }
}
</style>

