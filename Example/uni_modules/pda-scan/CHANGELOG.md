# 更新记录

本插件所有显著变更均记录于此文件。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-08-10

### 新增

- 将海康 DS-MDT201 广播扫码能力封装为 **UTS 插件**（`uni_modules/pda-scan`），替代原有 App 原生语言插件（AAR）形态，满足插件市场上架要求。
- 提供 `startScan`、`stopScan`、`startListening`、`stopListening`、`configure`、`emitTestEvent`、`getConfiguration` 等前端可导入的 API。
- 扫码结果与状态通过 `@UTSJS.keepAlive` 持续回调推送给前端，不再依赖 `uni.requireNativePlugin` 与 `plus.globalEvent`。
- 支持通过 `resultAction`、`resultDataKey` 配置覆盖默认的扫码结果广播与条码字段。
- 兼容 Android 13（API 33）动态注册广播接收器的 `RECEIVER_EXPORTED` 要求。
- 声明 `minSdk = 21`，支持 uni-app（vue2 / vue3）与 uni-app x。

### 变更

- 前端调用方式由 `uni.requireNativePlugin("PdaScan")` 调整为 `import { ... } from "@/uni_modules/pda-scan"`。
- 扫码结果事件由 `plus.globalEvent` 的 `PdaScan.onScanResult` / `PdaScan.onStatus` 调整为 startScan / startListening 的持续回调参数。
