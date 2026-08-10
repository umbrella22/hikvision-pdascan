# pda-scan UniApp 原生插件

该工程将海康 DS-MDT201 的广播扫码能力封装为 UniApp Android Module 插件。

插件以 `compileSdk = 30` 编译，并声明 `minSdk = 21`，可与默认最低 SDK 为 21 的 UniApp 云打包基座合并。实际功能仅应在配有海康扫描硬件的 PDA 上调用。

## 原生依赖

将 DCloud App 离线 SDK 中的 `uniapp-v8-release.aar` 放到 `app/libs/`。该依赖仅用于编译，产物 AAR 不会打包它。

## 打包

执行 `./gradlew syncUniPluginPackage`，任务会先执行 `:app:assembleRelease`，随后在项目根目录生成 `output/`，其结构符合 UniApp 原生插件规范：

```
output/
├── android/          # AAR（pda-scan-release.aar）
├── package.json      # 插件描述文件
├── README.md         # 使用说明
└── CHANGELOG.md      # 更新记录
```

将 `output/` 下的 `package.json` 与 `android/` 目录一同放入 UniApp 项目的 `nativeplugins/pda-scan/` 即可。

## UniApp 调用

```js
const pdaScan = uni.requireNativePlugin("PdaScan");

plus.globalEvent.addEventListener("PdaScan.onScanResult", (result) => {
  console.log("条码：", result.barcode);
});

plus.globalEvent.addEventListener("PdaScan.onStatus", (result) => {
  console.log("扫码状态：", result.status, result);
});

pdaScan.startScan(
  {
    resultAction: "android.intent.ACTION_SCAN_OUTPUT",
    resultDataKey: "barcode",
  },
  (result) => {
    if (result.type === "scan") {
      console.log(result.barcode);
    }
  },
);

pdaScan.stopScan((result) => {
  console.log(result.status);
});
```

仅注册 `PdaScan.onScanResult` 不会自动开启 Android 广播接收器，必须在监听器注册后调用 `startScan` 或 `startListening`。如果扫描枪已由硬件按键或其他组件触发，可只注册接收器而不发送开始扫码控制广播：

```js
pdaScan.startListening(
  {
    resultAction: "android.intent.ACTION_SCAN_OUTPUT",
    resultDataKey: "barcode",
  },
  (result) => {
    console.log("监听状态：", result);
  },
);

pdaScan.emitTestEvent((result) => {
  console.log("测试事件状态：", result);
});
```

`emitTestEvent` 会向 `PdaScan.onScanResult` 发出 `PDA_SCAN_EVENT_TEST`，用于验证 Java 原生插件到 JS 全局事件的链路；验证后不要将它作为实际扫码流程使用。

设备端需开启广播输出模式。默认结果广播为 `android.intent.ACTION_SCAN_OUTPUT`，条码字段为 `barcode`；如实机配置不同，在 `startScan` 或 `configure` 中传入 `resultAction`、`resultDataKey` 覆盖。
