<template>
  <view class="page">
    <!-- 监听状态卡片 -->
    <view class="card">
      <view class="status-row">
        <view class="status-dot" :class="listening ? 'on' : 'off'"></view>
        <text class="status-text">{{ listening ? '正在监听扫码广播' : '未监听扫码广播' }}</text>
      </view>
      <view class="config-row">
        <text class="config-label">resultAction</text>
        <text class="config-value">{{ config.resultAction }}</text>
      </view>
      <view class="config-row">
        <text class="config-label">resultDataKey</text>
        <text class="config-value">{{ config.resultDataKey }}</text>
      </view>
    </view>

    <!-- 扫码配置卡片 -->
    <view class="card">
      <view class="card-title">扫码配置</view>
      <view class="field">
        <text class="field-label">resultAction（扫码结果广播）</text>
        <input class="field-input" v-model="actionInput" placeholder="android.intent.ACTION_SCAN_OUTPUT" />
      </view>
      <view class="field">
        <text class="field-label">resultDataKey（条码字段）</text>
        <input class="field-input" v-model="dataKeyInput" placeholder="barcode" />
      </view>
      <view class="btn-group single">
        <view class="btn" @click="onGetConfig">读取当前配置</view>
        <view class="btn primary" @click="onConfigure">保存配置</view>
      </view>
    </view>

    <!-- 扫码控制卡片 -->
    <view class="card">
      <view class="card-title">扫码控制</view>
      <view class="btn-group">
        <view class="btn primary" @click="onStart">开始扫码</view>
        <view class="btn warn" @click="onStop">停止扫码</view>
      </view>
      <view class="btn-group">
        <view class="btn" @click="onListen">仅监听（硬件触发）</view>
        <view class="btn warn" @click="onStopListen">停止监听</view>
      </view>
      <view class="btn-group single">
        <view class="btn" @click="onTest">发送测试事件</view>
      </view>
      <text class="tip">设备端需开启广播输出模式。默认结果广播 android.intent.ACTION_SCAN_OUTPUT，条码字段 barcode。</text>
    </view>

    <!-- 事件日志卡片 -->
    <view class="card log-card">
      <view class="card-title">事件日志</view>
      <scroll-view scroll-y class="log">
        <view v-if="logs.length === 0" class="log-empty">
          暂无事件。点击「发送测试事件」可验证 UTS → JS 链路。
        </view>
        <view v-for="(item, index) in logs" :key="index" class="log-item" :class="item.type">
          <text class="log-time">{{ item.time }}</text>
          <text class="log-text">{{ item.text }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import {
  startScan,
  stopScan,
  stopListening,
  startListening,
  configure,
  emitTestEvent,
  getConfiguration,
} from '@/uni_modules/pda-scan'

const listening = ref(false)
const config = ref({
  resultAction: 'android.intent.ACTION_SCAN_OUTPUT',
  resultDataKey: 'barcode',
  isListening: false,
})
const actionInput = ref('android.intent.ACTION_SCAN_OUTPUT')
const dataKeyInput = ref('barcode')
const logs = ref([])

const pad = (n) => String(n).padStart(2, '0')

function addLog(type, text) {
  const now = new Date()
  const time = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  logs.value.unshift({ type, time, text })
  if (logs.value.length > 100) {
    logs.value.length = 100
  }
}

/** 统一处理插件回调事件：写日志 + 同步监听状态 + 刷新配置展示 */
function handleEvent(res) {
  if (res.type === 'scan') {
    addLog('scan', `扫码结果：${res.barcode}${res.isTest ? '（测试事件）' : ''}`)
  } else if (res.type === 'status') {
    addLog('status', `状态：${res.status}（监听中：${res.isListening}）`)
  } else if (res.type === 'error') {
    addLog('error', `错误：${res.code} - ${res.message}`)
  }
  if (typeof res.isListening === 'boolean') {
    listening.value = res.isListening
    config.value = {
      resultAction: res.resultAction,
      resultDataKey: res.resultDataKey,
      isListening: res.isListening,
    }
  }
}

function onStart() {
  addLog('status', '调用 startScan ...')
  startScan({ resultAction: actionInput.value, resultDataKey: dataKeyInput.value }, handleEvent)
}

function onStop() {
  addLog('status', '调用 stopScan（停止扫码控制，保留监听）...')
  stopScan(handleEvent)
}

function onStopListen() {
  addLog('status', '调用 stopListening（停止监听）...')
  stopListening(handleEvent)
}

function onListen() {
  addLog('status', '调用 startListening ...')
  startListening({ resultAction: actionInput.value, resultDataKey: dataKeyInput.value }, handleEvent)
}

function onConfigure() {
  addLog('status', '调用 configure ...')
  configure({ resultAction: actionInput.value, resultDataKey: dataKeyInput.value }, handleEvent)
}

function onTest() {
  addLog('status', '调用 emitTestEvent ...')
  emitTestEvent(handleEvent)
}

function onGetConfig() {
  const cfg = getConfiguration()
  handleEvent(cfg)
}
</script>

<style scoped>
.page {
  padding: 24rpx;
  min-height: 100vh;
}

.card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 20rpx;
}

.status-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.status-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-right: 14rpx;
}

.status-dot.on {
  background-color: #07c160;
  box-shadow: 0 0 10rpx rgba(7, 193, 96, 0.5);
}

.status-dot.off {
  background-color: #c0c4cc;
}

.status-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
}

.config-row {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}

.config-label {
  font-size: 24rpx;
  color: #999999;
  width: 180rpx;
}

.config-value {
  font-size: 24rpx;
  color: #666666;
  flex: 1;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 16rpx;
}

.field-label {
  font-size: 24rpx;
  color: #666666;
  margin-bottom: 10rpx;
}

.field-input {
  height: 72rpx;
  background-color: #f5f6f8;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333333;
}

.btn-group {
  display: flex;
  margin-bottom: 16rpx;
}

.btn-group:last-child {
  margin-bottom: 0;
}

.btn-group.single {
  margin-top: 8rpx;
}

.btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 12rpx;
  margin: 0 8rpx;
  background-color: #f0f2f5;
  color: #333333;
  text-align: center;
}

.btn:first-child {
  margin-left: 0;
}

.btn:last-child {
  margin-right: 0;
}

.btn.primary {
  background-color: #2979ff;
  color: #ffffff;
}

.btn.warn {
  background-color: #fa5151;
  color: #ffffff;
}

.tip {
  display: block;
  font-size: 22rpx;
  color: #999999;
  line-height: 1.6;
  margin-top: 20rpx;
}

.log-card {
  display: flex;
  flex-direction: column;
}

.log {
  height: 480rpx;
}

.log-empty {
  font-size: 24rpx;
  color: #bbbbbb;
  padding: 60rpx 0;
  text-align: center;
}

.log-item {
  display: flex;
  align-items: baseline;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.log-time {
  font-size: 22rpx;
  color: #999999;
  margin-right: 16rpx;
  width: 130rpx;
  flex-shrink: 0;
}

.log-text {
  font-size: 26rpx;
  color: #333333;
  flex: 1;
}

.log-item.scan .log-text {
  color: #07c160;
}

.log-item.status .log-text {
  color: #2979ff;
}

.log-item.error .log-text {
  color: #fa5151;
}

.notice {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.8;
}
</style>
