<!--
@deprecated This file is not used by router/layout anymore.
Active layout/sidebar is frontend/src/layouts/BasicLayout.vue
Do not edit. Kept for historical reference.
-->
<script lang="ts" setup>
import type { RouteLocationNormalizedGeneric, RouteRecordRaw, RouterLink } from "vue-router"
import type { TagView } from "@/pinia/stores/tags-view"
import { useRouteListener } from "@@/composables/useRouteListener"
import { Close } from "@element-plus/icons-vue"
import path from "path-browserify"
import { usePermissionStore } from "@/pinia/stores/permission"
import { useTagsViewStore } from "@/pinia/stores/tags-view"
import ScrollPane from "./ScrollPane.vue"

const router = useRouter()

const route = useRoute()

const tagsViewStore = useTagsViewStore()

const permissionStore = usePermissionStore()

const { listenerRouteChange } = useRouteListener()

/** 鏍囩椤电粍浠跺厓绱犵殑寮曠敤鏁扮粍 */
const tagRefs = useTemplateRef<InstanceType<typeof RouterLink>[]>("tagRefs")

/** 鍙抽敭鑿滃崟鐨勭姸鎬?*/
const visible = ref(false)

/** 鍙抽敭鑿滃崟鐨?top 浣嶇疆 */
const top = ref(0)

/** 鍙抽敭鑿滃崟鐨?left 浣嶇疆 */
const left = ref(0)

/** 褰撳墠姝ｅ湪鍙抽敭鎿嶄綔鐨勬爣绛鹃〉 */
const selectedTag = ref<TagView>({})

/** 鍥哄畾鐨勬爣绛鹃〉 */
let affixTags: TagView[] = []

/** 鍒ゆ柇鏍囩椤垫槸鍚︽縺娲?*/
function isActive(tag: TagView) {
  return tag.path === route.path
}

/** 鍒ゆ柇鏍囩椤垫槸鍚﹀浐瀹?*/
function isAffix(tag: TagView) {
  return tag.meta?.affix
}

/** 绛涢€夊嚭鍥哄畾鏍囩椤?*/
function filterAffixTags(routes: RouteRecordRaw[], basePath = "/") {
  const tags: TagView[] = []
  routes.forEach((route) => {
    if (isAffix(route)) {
      const tagPath = path.resolve(basePath, route.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: route.name,
        meta: { ...route.meta }
      })
    }
    if (route.children) {
      const childTags = filterAffixTags(route.children, route.path)
      tags.push(...childTags)
    }
  })
  return tags
}

/** 鍒濆鍖栨爣绛鹃〉 */
function initTags() {
  affixTags = filterAffixTags(permissionStore.routes)
  for (const tag of affixTags) {
    // 蹇呴』鍚湁 name 灞炴€?
    tag.name && tagsViewStore.addVisitedView(tag)
  }
}

/** 娣诲姞鏍囩椤?*/
function addTags(route: RouteLocationNormalizedGeneric) {
  if (route.name) {
    tagsViewStore.addVisitedView(route)
    tagsViewStore.addCachedView(route)
  }
}

/** 鍒锋柊褰撳墠姝ｅ湪鍙抽敭鎿嶄綔鐨勬爣绛鹃〉 */
function refreshSelectedTag(view: TagView) {
  tagsViewStore.delCachedView(view)
  router.replace({ path: `/redirect${view.path}`, query: view.query })
}

/** 鍏抽棴褰撳墠姝ｅ湪鍙抽敭鎿嶄綔鐨勬爣绛鹃〉 */
function closeSelectedTag(view: TagView) {
  tagsViewStore.delVisitedView(view)
  tagsViewStore.delCachedView(view)
  isActive(view) && toLastView(tagsViewStore.visitedViews, view)
}

/** 鍏抽棴鍏朵粬鏍囩椤?*/
function closeOthersTags() {
  const fullPath = selectedTag.value.fullPath
  if (fullPath !== route.path && fullPath !== undefined) {
    router.push(fullPath)
  }
  tagsViewStore.delOthersVisitedViews(selectedTag.value)
  tagsViewStore.delOthersCachedViews(selectedTag.value)
}

/** 鍏抽棴鎵€鏈夋爣绛鹃〉 */
function closeAllTags(view: TagView) {
  tagsViewStore.delAllVisitedViews()
  tagsViewStore.delAllCachedViews()
  if (affixTags.some(tag => tag.path === route.path)) return
  toLastView(tagsViewStore.visitedViews, view)
}

/** 璺宠浆鍒版渶鍚庝竴涓爣绛鹃〉 */
function toLastView(visitedViews: TagView[], view: TagView) {
  const latestView = visitedViews.slice(-1)[0]
  const fullPath = latestView?.fullPath
  if (fullPath !== undefined) {
    router.push(fullPath)
  } else {
    // 濡傛灉 TagsView 鍏ㄩ儴琚叧闂簡锛屽垯榛樿閲嶅畾鍚戝埌涓婚〉
    if (view.name === "Dashboard") {
      // 閲嶆柊鍔犺浇涓婚〉
      router.push({ path: `/redirect${view.path}`, query: view.query })
    } else {
      router.push("/")
    }
  }
}

/** 鎵撳紑鍙抽敭鑿滃崟闈㈡澘 */
function openMenu(tag: TagView, e: MouseEvent) {
  const menuMinWidth = 100
  // 褰撳墠椤甸潰瀹藉害
  const offsetWidth = document.body.offsetWidth
  // 闈㈡澘鐨勬渶澶у乏杈硅窛
  const maxLeft = offsetWidth - menuMinWidth
  // 闈㈡澘璺濈榧犳爣鎸囬拡鐨勮窛绂?
  const left15 = e.clientX + 10
  left.value = left15 > maxLeft ? maxLeft : left15
  top.value = e.clientY
  // 鏄剧ず闈㈡澘
  visible.value = true
  // 鏇存柊褰撳墠姝ｅ湪鍙抽敭鎿嶄綔鐨勬爣绛鹃〉
  selectedTag.value = tag
}

/** 鍏抽棴鍙抽敭鑿滃崟闈㈡澘 */
function closeMenu() {
  visible.value = false
}

watch(visible, (value) => {
  value ? document.body.addEventListener("click", closeMenu) : document.body.removeEventListener("click", closeMenu)
})

initTags()

// 鐩戝惉璺敱鍙樺寲
listenerRouteChange((route) => {
  addTags(route)
}, true)
</script>

<template>
  <div class="tags-view-container">
    <ScrollPane class="tags-view-wrapper" :tag-refs="tagRefs">
      <router-link
        v-for="tag in tagsViewStore.visitedViews"
        :key="tag.path"
        ref="tagRefs"
        :class="{ active: isActive(tag) }"
        class="tags-view-item"
        :to="{ path: tag.path, query: tag.query }"
        @click.middle="!isAffix(tag) && closeSelectedTag(tag)"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        {{ tag.meta?.title }}
        <el-icon v-if="!isAffix(tag)" :size="12" @click.prevent.stop="closeSelectedTag(tag)">
          <Close />
        </el-icon>
      </router-link>
    </ScrollPane>
    <ul v-show="visible" class="contextmenu" :style="{ left: `${left}px`, top: `${top}px` }">
      <li @click="refreshSelectedTag(selectedTag)">
        鍒锋柊
      </li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
        鍏抽棴
      </li>
      <li @click="closeOthersTags">
        鍏抽棴鍏跺畠
      </li>
      <li @click="closeAllTags(selectedTag)">
        鍏抽棴鎵€鏈?
      </li>
    </ul>
  </div>
</template>

<style lang="scss" scoped>
.tags-view-container {
  height: var(--v3-tagsview-height);
  width: 100%;
  color: var(--v3-tagsview-text-color);
  overflow: hidden;
  .tags-view-wrapper {
    .tags-view-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid var(--v3-tagsview-tag-border-color);
      border-radius: var(--v3-tagsview-tag-border-radius);
      background-color: var(--v3-tagsview-tag-bg-color);
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;
      &:first-of-type {
        margin-left: 5px;
      }
      &:last-of-type {
        margin-right: 5px;
      }
      &.active {
        background-color: var(--v3-tagsview-tag-active-bg-color);
        color: var(--v3-tagsview-tag-active-text-color);
        border-color: var(--v3-tagsview-tag-active-border-color);
      }
      .el-icon {
        margin-left: 5px;
        margin-right: 1px;
        border-radius: 50%;
        &:hover {
          background-color: var(--v3-tagsview-tag-icon-hover-bg-color);
          color: var(--v3-tagsview-tag-icon-hover-color);
        }
      }
    }
  }
  .contextmenu {
    margin: 0;
    z-index: 3000;
    position: fixed;
    list-style-type: none;
    padding: 5px 0;
    border-radius: 4px;
    font-size: 12px;
    color: var(--v3-tagsview-contextmenu-text-color);
    background-color: var(--v3-tagsview-contextmenu-bg-color);
    box-shadow: var(--v3-tagsview-contextmenu-box-shadow);
    li {
      margin: 0;
      padding: 7px 16px;
      cursor: pointer;
      &:hover {
        color: var(--v3-tagsview-contextmenu-hover-text-color);
        background-color: var(--v3-tagsview-contextmenu-hover-bg-color);
      }
    }
  }
}
</style>

