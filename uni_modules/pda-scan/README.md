# pda-scan（海康 DS-MDT201 广播扫码）UTS 插件

将海康 DS-MDT201 的广播扫码能力封装为 uni-app UTS 插件。插件通过注册 Android 广播接收器监听扫码结果广播，并将条码通过持续回调推送给前端。

- 支持 uni-app（vue2 / vue3）与 uni-app x
- 仅支持 Android 平台，`minSdk = 21`（Android 5.0）
- 实际功能应在配有海康扫描硬件的 PDA 上调用

## 安装

1. 将本目录（`uni_modules/pda-scan`）复制到 uni-app 项目的 `uni_modules/` 目录下。
2. 使用 **HBuilderX** 打开项目，**运行到手机或模拟器运行**需要先[制作自定义调试基座](https://doc.dcloud.net.cn/uni-app-x/tutorial/app-package.html#%E4%BD%9C%E8%87%AA%E5%AE%9A%E4%B9%89%E8%B0%83%E8%AF%95%E5%9F%BA%E5%BA%A7)；正式打包使用云打包即可。

> UTS 插件目前仅支持通过 HBuilderX 创建和使用，不支持 cli 方式。

## 快速开始

```js
import {
  startScan,
  stopScan,
  startListening,
  configure,
  emitTestEvent,
  getConfiguration,
} from "@/uni_modules/pda-scan";
```

### 启动扫码

```js
startScan(
  {
    resultAction: "android.intent.ACTION_SCAN_OUTPUT",
    resultDataKey: "barcode",
  },
  (res) => {
    if (res.type === "scan") {
      console.log("条码：", res.barcode);
    } else if (res.type === "status") {
      console.log("扫码状态：", res.status);
    } else if (res.type === "error") {
      console.error("扫码错误：", res.code, res.message);
    }
  },
);
```

回调会先收到一次 `status = "started"`，之后每次扫码收到 `type = "scan"` 事件。该回调为持续回调，可被多次调用；停止监听后自动失效。

### 停止扫码控制

仅发送停止扫码控制广播（让硬件停止扫描），**不会**注销接收器、不会清空回调：

```js
stopScan((res) => {
  console.log("已发送停止扫码指令：", res.status);
});
```

### 停止监听

注销扫码结果广播接收器并清空持续回调，**不会**发送停止扫码控制广播：

```js
stopListening((res) => {
  console.log("已停止监听：", res.isListening);
});
```

通常页面销毁时调用 `stopListening` 释放监听，需要硬件停止扫描时再配合 `stopScan`。

### 仅监听（扫描枪由硬件按键触发）

如果扫描枪已由硬件按键或其他组件触发，可只注册接收器而不发送开始扫码控制广播：

```js
startListening(
  {
    resultAction: "android.intent.ACTION_SCAN_OUTPUT",
    resultDataKey: "barcode",
  },
  (res) => {
    console.log("监听事件：", res);
  },
);
```

### 更新配置

```js
configure(
  {
    resultAction: "android.intent.ACTION_SCAN_OUTPUT",
    resultDataKey: "barcode",
  },
  (res) => {
    console.log("当前配置：", res);
  },
);
```

### 测试事件

```js
emitTestEvent((res) => {
  console.log("测试事件：", res.barcode); // PDA_SCAN_EVENT_TEST
});
```

`emitTestEvent` 会推送一次 `PDA_SCAN_EVENT_TEST` 测试条码，用于验证 UTS 到前端的链路；验证后不要将它作为实际扫码流程使用。

### 获取配置

```js
const cfg = getConfiguration();
console.log(cfg.resultAction, cfg.resultDataKey, cfg.isListening);
```

## 事件结构

所有回调参数均为 `PdaScanEvent`：

| 字段 | 类型 | 说明 |
|---|---|---|
| type | string | `scan`（扫码结果）/ `status`（状态）/ `error`（错误） |
| barcode | string \| undefined | 扫码结果条码，`type = "scan"` 时存在 |
| status | string \| undefined | `started` / `stopped` / `listening`，`type = "status"` 时存在 |
| code | string \| undefined | 错误码，`type = "error"` 时存在 |
| message | string \| undefined | 错误信息，`type = "error"` 时存在 |
| resultAction | string | 当前扫码结果广播 action |
| resultDataKey | string | 当前条码字段 key |
| isListening | boolean | 是否正在监听广播 |
| isTest | boolean | 是否为测试事件 |

## 设备配置

设备端需开启广播输出模式。默认结果广播为 `android.intent.ACTION_SCAN_OUTPUT`，条码字段为 `barcode`；如实机配置不同，在 `startScan`、`startListening` 或 `configure` 中传入 `resultAction`、`resultDataKey` 覆盖。

## 注意事项

- Android 13+（API 33）动态注册广播接收器必须声明 `RECEIVER_EXPORTED`，插件已处理。
- 插件不申请额外权限；`ACTION_SCAN_START` / `ACTION_SCAN_STOP` 控制广播由海康设备端处理。
