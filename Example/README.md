# pda-scan 示例工程

海康 DS-MDT201 广播扫码 UTS 插件（`pda-scan`）的演示工程，独立 uni-app（vue3）项目，可用 **HBuilderX** 直接打开运行。

## 目录结构

```
Example/
├── pages/index/index.vue   # 演示页：扫码控制、配置、事件日志
├── uni_modules/pda-scan/   # 插件副本（与仓库根目录 uni_modules/pda-scan 保持一致）
├── App.vue
├── main.js
├── manifest.json
├── pages.json
└── README.md
```

## 运行步骤

1. 用 **HBuilderX** 打开本目录（`Example`）。
2. 运行到 Android 真机/模拟器前，需先[制作自定义调试基座](https://doc.dcloud.net.cn/uni-app-x/tutorial/app-package.html#%E4%BD%9C%E8%87%AA%E5%AE%9A%E4%B9%89%E8%B0%83%E8%AF%95%E5%9F%BA%E5%BA%A7)（UTS 插件依赖），正式打包使用云打包即可。
3. 打开演示页后点击「发送测试事件」，若日志出现 `PDA_SCAN_EVENT_TEST`，说明 UTS → JS 链路正常。

## 演示页功能

- **扫码配置**：可编辑 `resultAction` / `resultDataKey`，保存后生效（`configure`），也可读取当前配置（`getConfiguration`）。
- **扫码控制**：`startScan`（开始扫码）、`stopScan`（停止扫码控制）、`startListening`（仅监听，硬件触发时使用）、`stopListening`（停止监听）、`emitTestEvent`（测试事件）。
- **事件日志**：展示持续回调推送的 scan / status / error 事件。

## 注意事项

- 插件仅支持 App-Android 平台；在其他平台运行会提示不支持。
- `Example/uni_modules/pda-scan` 是插件副本，修改插件源码后请同步复制，或直接以仓库根目录的 `uni_modules/pda-scan` 为准。
