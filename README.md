# pda-scan

海康 DS-MDT201 广播扫码 **UTS 插件**（uni_modules 规范）。

插件源码位于 [uni_modules/pda-scan](uni_modules/pda-scan)，将海康 DS-MDT201 的广播扫码能力封装为 uni-app 可导入的 API，扫码结果通过持续回调推送给前端，仅支持 Android 平台。

## 使用

将 `uni_modules/pda-scan` 目录复制到 uni-app 项目的 `uni_modules/` 下，然后在页面中导入：

```js
import { startScan, stopScan, stopListening } from "@/uni_modules/pda-scan";

startScan({}, (res) => {
  if (res.type === "scan") {
    console.log("条码：", res.barcode);
  }
});
```

详细说明见 [uni_modules/pda-scan/README.md](uni_modules/pda-scan/README.md)。

## 示例工程

[Example](Example) 目录包含一个可直接用 HBuilderX 打开的演示工程（含演示页与插件副本），见 [Example/README.md](Example/README.md)。
