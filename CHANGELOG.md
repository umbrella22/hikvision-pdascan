# 更新记录

本插件所有显著变更均记录于此文件。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-08-10

### 新增

- 将海康 DS-MDT201 广播扫码能力封装为 UniApp Android Module 插件。
- 提供 `startScan`、`stopScan`、`startListening`、`configure`、`emitTestEvent`、`getConfiguration` 等 JS 调用方法。
- 通过 `PdaScan.onScanResult`、`PdaScan.onStatus` 全局事件推送扫码结果与设备状态。
- 支持通过 `resultAction`、`resultDataKey` 配置覆盖默认的扫码结果广播与条码字段。
- 兼容 Android 13（API 33）动态注册广播接收器的 `RECEIVER_EXPORTED` 要求。

### 变更

- 包名由 `com.ikaros.celaeno.pdascan` 调整为 `com.ikaros.hikvision.mdt201`。
- 新增 `syncUniPluginPackage` Gradle 任务，打包后自动生成符合 UniApp 原生插件规范的 `output/` 目录。
