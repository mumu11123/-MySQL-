<template>
  <div class="chat-container">
    <!-- 连接状态指示器 -->
    <div class="connection-status">
      <el-tag :type="pollingActive ? 'success' : 'info'" size="small">
        <el-icon v-if="pollingActive"><Refresh /></el-icon>
        <el-icon v-else><CircleClose /></el-icon>
        {{ pollingActive ? '实时同步中' : '同步已停止' }}
      </el-tag>
    </div>

    <!-- 顶部：订单选择器 -->
    <el-card class="order-select-card">
      <div style="display: flex; align-items: center; gap: 15px;">
        <span style="font-weight: bold; white-space: nowrap;">选择订单：</span>
        <el-select 
          v-model="currentOrderId" 
          @change="handleOrderChange"
          placeholder="请选择订单"
          style="flex: 1;"
          filterable
        >
          <el-option 
            v-for="o in orderList" 
            :key="o.id" 
            :label="`订单 #${o.orderNo}`" 
            :value="o.id" 
          >
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 10px;">
              <span>订单 #{{ o.orderNo }}</span>
              <el-tag :type="o.statusType" size="small">{{ o.statusText }}</el-tag>
              <el-badge v-if="o.unreadCount > 0" :value="o.unreadCount" />
            </div>
          </el-option>
        </el-select>
        <el-button type="primary" :icon="Refresh" @click="refreshOrderList" circle />
      </div>
    </el-card>

    <!-- 中间：消息列表 -->
    <el-card class="message-list-card" v-loading="loading">
      <div v-if="!currentOrderId" class="empty-state">
        <el-empty description="请选择一个订单开始聊天" />
      </div>

      <div v-else ref="msgBox" class="message-box" @scroll="handleScroll">
        <!-- 加载更多 -->
        <div v-if="hasMore" style="text-align: center; padding: 10px;">
          <el-button link type="primary" @click="loadMore" :loading="loadingMore">
            加载更多消息
          </el-button>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in msgList" :key="msg.id">
          <!-- 时间分割线 -->
          <div v-if="showTimeDivider(index)" class="time-divider">
            {{ formatTime(msg.createdAt) }}
          </div>

          <!-- 消息气泡 -->
          <div :class="['message-row', msg.senderType === 'merchant' ? 'right' : 'left']">
            <div class="message-wrapper">
              <!-- 头像 -->
              <el-avatar 
                :size="36" 
                :src="msg.senderType === 'merchant' ? merchantAvatar : userAvatar"
                :icon="UserFilled"
                class="avatar"
              />

              <!-- 气泡 -->
              <div class="bubble-wrapper">
                <div :class="['bubble', msg.senderType === 'merchant' ? 'bubble-right' : 'bubble-left']">
                  <!-- 图片消息 -->
                  <div v-if="isImage(msg.content)" class="image-message">
                    <img 
                      :src="msg.content" 
                      @click="previewImage(msg.content)"
                      style="max-width: 200px; max-height: 150px; border-radius: 8px; cursor: pointer;"
                    />
                  </div>
                  <!-- 文字消息 -->
                  <p v-else class="message-text">{{ msg.content }}</p>
                </div>

                <!-- 底部信息 -->
                <div :class="['message-meta', msg.senderType === 'merchant' ? 'meta-right' : 'meta-left']">
                  <span class="time">{{ formatShortTime(msg.createdAt) }}</span>
                  <span v-if="msg.senderType === 'merchant'" class="status">
                    <el-icon v-if="msg.status === 0" class="status-icon sending"><Loading /></el-icon>
                    <el-icon v-else-if="msg.status === 1" class="status-icon sent"><Check /></el-icon>
                    <span v-else-if="msg.status === 2" class="status-read">已读</span>
                    <el-icon v-else-if="msg.status === 3" class="status-icon failed"><WarningFilled /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空消息提示 -->
        <div v-if="msgList.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无消息，发送第一条消息吧" />
        </div>
      </div>
    </el-card>

    <!-- 底部：输入区域 -->
    <el-card class="input-card" v-if="currentOrderId">
      <div class="input-area">
        <el-upload
          :http-request="customImageUpload"
          :show-file-list="false"
          accept="image/*"
          class="upload-btn"
        >
          <el-button :icon="Picture" circle />
        </el-upload>

        <el-input
          v-model="sendText"
          type="textarea"
          :rows="2"
          placeholder="输入消息..."
          @keyup.enter.ctrl="sendMsg"
          resize="none"
          class="message-input"
        />

        <el-button 
          type="primary" 
          @click="sendMsg" 
          :loading="sending"
          :disabled="!sendText.trim()"
          class="send-btn"
        >
          发送
        </el-button>
      </div>
      <div class="input-tip">Ctrl + Enter 发送</div>
    </el-card>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px" align-center>
      <div style="display: flex; justify-content: center; padding: 20px;">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh; border-radius: 8px;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  UserFilled, 
  Refresh, 
  Picture, 
  Loading, 
  Check, 
  WarningFilled,
  CircleClose
} from '@element-plus/icons-vue'
import http from '../../utils/request'

// ========== 轮询状态 ==========
const pollingActive = ref(false)
let pollingTimer = null
const POLLING_INTERVAL = 3000  // 每3秒刷新一次消息状态

// ========== 页面状态 ==========
const orderList = ref([])
const currentOrderId = ref(null)
const currentUserId = ref(null)
const msgList = ref([])
const sendText = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const hasMore = ref(false)
const page = ref(1)
const pageSize = 20
const msgBox = ref(null)

// 头像
const merchantAvatar = ref('')
const userAvatar = ref('')

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')

// ========== 初始化 ==========
onMounted(() => {
  loadOrderList()
  loadMerchantLogo()

  // 每30秒刷新订单列表（未读数）
  const orderTimer = setInterval(loadOrderList, 30000)

  onUnmounted(() => {
    clearInterval(orderTimer)
    stopPolling()  // 清理轮询
  })
})

// 加载商家头像logo
const loadMerchantLogo = async () => {
  try {
    const res = await http.get('/api/merchant/info')
    if (res.code === 200 && res.data.logo) {
      // 把后端返回的logo赋值给商家头像
      merchantAvatar.value = res.data.logo
    }
  } catch (err) {
    console.error('获取商家logo失败', err)
  }
}

// ========== 轮询控制 ==========
const startPolling = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  pollingActive.value = true

  // 立即执行一次
  pollingLoadMessages()

  // 定时轮询
  pollingTimer = setInterval(() => {
    pollingLoadMessages()
  }, POLLING_INTERVAL)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  pollingActive.value = false
}

// 轮询加载消息（静默刷新，不显示loading）
const pollingLoadMessages = async () => {
  if (!currentOrderId.value) return

  try {
    const res = await http.get(`/api/merchant/message/list/${currentOrderId.value}`)
    if (res.code === 200 && res.data) {
      const newMessages = res.data

      // 检查是否有新消息或状态变化
      const hasChanges = checkMessageChanges(msgList.value, newMessages)

      if (hasChanges) {
        // 记录当前滚动位置
        const scrollBottom = msgBox.value 
          ? msgBox.value.scrollHeight - msgBox.value.scrollTop - msgBox.value.clientHeight 
          : 0

        msgList.value = newMessages

        // 如果有新消息（学生发来的），滚动到底部
        nextTick(() => {
          if (scrollBottom < 50) {
            scrollToBottom()
          }
          // 标记已读
          markAsRead(currentOrderId.value)
        })
      }
    }
  } catch (err) {
    // 轮询失败不提示，静默处理
    console.error('轮询消息失败', err)
  }
}

// 检查消息列表是否有变化（新消息或状态变化）
const checkMessageChanges = (oldList, newList) => {
  if (oldList.length !== newList.length) return true

  for (let i = 0; i < oldList.length; i++) {
    const oldMsg = oldList[i]
    const newMsg = newList[i]

    // 检查状态变化（比如从已发送变成已读）
    if (oldMsg.status !== newMsg.status) return true

    // 检查是否有新消息（id不同）
    if (oldMsg.id !== newMsg.id) return true
  }

  return false
}

// ========== 订单列表 ==========
const loadOrderList = async () => {
  try {
    const res = await http.get('/api/merchant/orders', { params: { status: '' } })
    if (res.code === 200) {
      const orderIds = res.data.map(o => o.id)
      let unreadMap = {}
      if (orderIds.length > 0) {
        const unreadRes = await http.get('/api/merchant/message/unread/list', {
          params: { orderIds: orderIds.join(',') }
        })
        if (unreadRes.code === 200) {
          unreadRes.data.forEach(msg => {
            unreadMap[msg.orderId] = (unreadMap[msg.orderId] || 0) + 1
          })
        }
      }

      orderList.value = res.data.map(o => ({
        ...o,
        statusText: getStatusText(o.status),
        statusType: getStatusType(o.status),
        unreadCount: unreadMap[o.id] || 0
      }))
    }
  } catch (err) {
    console.error('加载订单列表失败', err)
  }
}

const refreshOrderList = () => {
  loadOrderList()
  ElMessage.success('已刷新')
}

const handleOrderChange = async (orderId) => {
  if (!orderId) {
    stopPolling()
    return
  }

  const order = orderList.value.find(o => o.id === orderId)
  if (order) {
    currentUserId.value = order.userId
  }

  page.value = 1
  msgList.value = []

  // 先加载一次
  await loadMessages()

  // 标记已读
  await markAsRead(orderId)

  // 更新未读数
  if (order) order.unreadCount = 0

  // 启动轮询
  startPolling()
}

// ========== 消息列表 ==========
const loadMessages = async () => {
  if (!currentOrderId.value) return
  loading.value = true
  try {
    const res = await http.get(`/api/merchant/message/list/${currentOrderId.value}`)
    if (res.code === 200) {
      msgList.value = res.data
      nextTick(() => scrollToBottom())
    }
  } catch (err) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  loadingMore.value = true
  // TODO: 实现分页加载
  loadingMore.value = false
}

const markAsRead = async (orderId) => {
  try {
    await http.put(`/api/merchant/message/${orderId}/read`)
  } catch (err) {
    console.error('标记已读失败', err)
  }
}

// ========== 发送消息 ==========
const sendMsg = async () => {
  const content = sendText.value.trim()
  if (!content || !currentOrderId.value || !currentUserId.value) return

  sending.value = true

  const tempMsg = {
    id: Date.now(),
    orderId: currentOrderId.value,
    senderType: 'merchant',
    senderId: 0,
    receiverType: 'user',
    receiverId: currentUserId.value,
    content: content,
    status: 0,
    createdAt: new Date().toISOString(),
    isTemp: true
  }
  msgList.value.push(tempMsg)
  sendText.value = ''
  nextTick(() => scrollToBottom())

  try {
    const res = await http.post('/api/merchant/message/reply', {
      orderId: currentOrderId.value,
      userId: currentUserId.value,
      content: content
    })

    if (res.code === 200) {
      const idx = msgList.value.findIndex(m => m.id === tempMsg.id)
      if (idx !== -1) {
        msgList.value[idx].status = 1
      }
      ElMessage.success('发送成功')
      await loadMessages()
    } else {
      throw new Error(res.message)
    }
  } catch (err) {
    const idx = msgList.value.findIndex(m => m.id === tempMsg.id)
    if (idx !== -1) {
      msgList.value[idx].status = 3
    }
    ElMessage.error('发送失败：' + (err.message || '网络错误'))
  } finally {
    sending.value = false
  }
}

// ========== 图片上传 ==========
const customImageUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await http.post('/api/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.code === 200) {
      sendText.value = res.data
      await sendMsg()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败：' + (err.message || '网络错误'))
  }
}

// ========== 图片预览 ==========
const isImage = (content) => {
  if (!content) return false
  return content.startsWith('http') && /\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i.test(content)
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

// ========== 工具方法 ==========
const scrollToBottom = () => {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

const handleScroll = () => {
  // 上滑加载更多
}

const showTimeDivider = (index) => {
  if (index === 0) return true
  const curr = new Date(msgList.value[index].createdAt)
  const prev = new Date(msgList.value[index - 1].createdAt)
  return (curr - prev) > 5 * 60 * 1000
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const isYesterday = date.toDateString() === new Date(now - 86400000).toDateString()

  if (isToday) return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  if (isYesterday) return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatShortTime = (time) => {
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getStatusText = (status) => {
  const map = {
    0: '待支付', 2: '待接单', 3: '制作中',
    4: '待取餐', 5: '已完成', 6: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'info', 2: 'primary', 3: 'warning',
    4: 'success', 5: 'success', 6: 'danger'
  }
  return map[status] || ''
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(98vh - 40px);
  gap: 10px;
  position: relative;
}

/* 连接状态 */
.connection-status {
  position: absolute;
  top: -5px;
  right: 10px;
  z-index: 100;
}

.order-select-card {
  flex-shrink: 0;
}

.message-list-card {
  flex: 1;
  overflow: hidden;
}

.message-box {
  height: 96%;
  overflow-y: auto;
  padding: 10px;
}

.input-card {
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 消息行 */
.message-row {
  margin-bottom: 15px;
}

.message-row.left {
  text-align: left;
}

.message-row.right {
  text-align: right;
}

.message-wrapper {
  display: inline-flex;
  align-items: flex-start;
  max-width: 70%;
  gap: 8px;
}

.message-row.right .message-wrapper {
  flex-direction: row-reverse;
}

/* 气泡 */
.bubble-wrapper {
  display: flex;
  flex-direction: column;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  max-width: 100%;
}

.bubble-left {
  background: #f0f2f5;
  color: #333;
  border-top-left-radius: 2px;
}

.bubble-right {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.message-text {
  margin: 0;
  line-height: 1.5;
}

/* 图片消息 */
.image-message img {
  display: block;
}

/* 元信息 */
.message-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.meta-left {
  text-align: left;
}

.meta-right {
  text-align: right;
}

.time {
  margin-right: 5px;
}

.status-read {
  color: #67c23a;
  font-size: 11px;
}

.status-icon {
  font-size: 12px;
}

.status-icon.sending {
  animation: rotate 1s linear infinite;
}

.status-icon.failed {
  color: #f56c6c;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 时间分割线 */
.time-divider {
  text-align: center;
  margin: 15px 0;
  color: #999;
  font-size: 12px;
}

/* 输入区域 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.upload-btn {
  flex-shrink: 0;
}

.message-input {
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  height: 52px;
}

.input-tip {
  text-align: right;
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}
</style>