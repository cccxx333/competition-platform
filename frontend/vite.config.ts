/// <reference types="vitest/config" />

import { resolve } from "node:path"
import vue from "@vitejs/plugin-vue"
import UnoCSS from "unocss/vite"
import AutoImport from "unplugin-auto-import/vite"
import SvgComponent from "unplugin-svg-component/vite"
import { ElementPlusResolver } from "unplugin-vue-components/resolvers"
import Components from "unplugin-vue-components/vite"
import { defineConfig, loadEnv } from "vite"
import { VueMcp } from "vite-plugin-vue-mcp"
import svgLoader from "vite-svg-loader"

// Configuring Vite: https://cn.vite.dev/config
export default defineConfig(({ mode }) => {
  const { VITE_PUBLIC_PATH, VITE_API_PROXY_TARGET } = loadEnv(mode, process.cwd(), "") as ImportMetaEnv
  const apiProxyTarget = VITE_API_PROXY_TARGET || "http://localhost:8080"
  return {
    // 开发与构建时使用的公共基础路径
    base: VITE_PUBLIC_PATH,
    resolve: {
      alias: {
        // @ 指向 src 目录
        "@": resolve(__dirname, "src"),
        // @@ 指向 src/common 通用目录
        "@@": resolve(__dirname, "src/common")
      }
    },
    // 开发服务器配置
    server: {
      // 监听所有地址
      host: true,
      // 服务端口
      port: 3333,
      // 端口被占用时是否直接退出
      strictPort: false,
      // 是否自动打开浏览器
      open: true,
      // 反向代理
      proxy: {
        "/api": {
          target: apiProxyTarget,
          // enable WebSocket if needed
          ws: false,
          // change origin to avoid CORS issues
          changeOrigin: true
        },
        "/files": {
          target: apiProxyTarget,
          changeOrigin: true
        }
      },
      // 是否允许跨域
      cors: true,
      // 预热常用文件，提升初始加载速度
      warmup: {
        clientFiles: [
          "./src/layouts/**/*.*",
          "./src/pinia/**/*.*",
          "./src/router/**/*.*"
        ]
      }
    },
    // 构建配置
    build: {
      // 自定义 Rollup 打包配置
      rollupOptions: {
        output: {
          /**
           * @name 分块策略
           * @description 1. 这些包名必须存在，否则构建会报错
           * @description 2. 不需要自定义分块时可删除该配置
           */
          manualChunks: {
            vue: ["vue", "vue-router", "pinia"],
            element: ["element-plus", "@element-plus/icons-vue"],
            vxe: ["vxe-table"]
          }
        }
      },
      // 是否启用 gzip 体积报告，关闭可略微提升构建速度
      reportCompressedSize: false,
      // 单个 chunk 超过 2048kB 时给出警告
      chunkSizeWarningLimit: 2048
    },
    // esbuild 配置
    esbuild:
      mode === "development"
        ? undefined
        : {
            // 构建时移除 console.log
            pure: ["console.log"],
            // 构建时移除 debugger
            drop: ["debugger"],
            // 构建时移除所有注释
            legalComments: "none"
          },
    // 依赖预构建
    optimizeDeps: {
      include: ["element-plus/es/components/*/style/css"]
    },
    // CSS 相关配置
    css: {
      // 开启 CSS 预处理并行
      preprocessorMaxWorkers: true
    },
    // 插件配置
    plugins: [
      vue(),
      // 支持将 SVG 导入为 Vue 组件
      svgLoader({
        defaultImport: "url",
        svgoConfig: {
          plugins: [
            {
              name: "preset-default",
              params: {
                overrides: {
                  // @see https://github.com/svg/svgo/issues/1128
                  removeViewBox: false
                }
              }
            }
          ]
        }
      }),
      // 自动生成 SvgIcon 组件与 SVG 雪碧图
      SvgComponent({
        iconDir: [resolve(__dirname, "src/common/assets/icons")],
        preserveColor: resolve(__dirname, "src/common/assets/icons/preserve-color"),
        dts: true,
        dtsDir: resolve(__dirname, "types/auto")
      }),
      // UnoCSS 原子化样式
      UnoCSS(),
      // 自动按需导入 API
      AutoImport({
        imports: ["vue", "vue-router", "pinia"],
        dts: "types/auto/auto-imports.d.ts",
        resolvers: [ElementPlusResolver()]
      }),
      // 自动按需导入组件
      Components({
        dts: "types/auto/components.d.ts",
        resolvers: [ElementPlusResolver()]
      }),
      // 为项目开启 MCP Server
      VueMcp()
    ],
    // Configuring Vitest: https://cn.vitest.dev/config
    test: {
      include: ["tests/**/*.test.{ts,js}"],
      environment: "happy-dom",
      server: {
        deps: {
          inline: ["element-plus"]
        }
      }
    }
  }
})





