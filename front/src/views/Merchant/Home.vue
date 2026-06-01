<template>
  <div>
    <!-- 营业状态卡片 -->
    <el-card class="mb20">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 12px; font-size: 16px;">
          <span>店铺营业状态：</span>
          <el-tag :type="merchantStatusType(status)" size="large">
            {{ merchantStatusText(status) }}
          </el-tag>
        </div>

        <div style="display: flex; gap: 10px;">
          <el-button type="success" @click="change(1)" :disabled="status === 1 || status === 3">
            <el-icon><VideoPlay /></el-icon> 开始营业
          </el-button>
          <el-button type="danger" @click="change(2)" :disabled="status === 2 || status === 3">
            <el-icon><VideoPause /></el-icon> 暂停营业
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/dish')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff;">
              <el-icon :size="28"><Dish /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">在售菜品</div>
              <div class="stat-value" style="color: #409eff;">{{ stats.dishCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/order')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c;">
              <el-icon :size="28"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待接单</div>
              <div class="stat-value" style="color: #e6a23c;">{{ stats.waitOrderCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/order')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9eb; color: #67c23a;">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日完成</div>
              <div class="stat-value" style="color: #67c23a;">{{ stats.todayFinishCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/message')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c;">
              <el-icon :size="28"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">新消息</div>
              <div class="stat-value" style="color: #f56c6c;">
                <el-badge :value="stats.unreadCount" class="badge" :show-zero="false" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: bold;">待处理订单</span>
              <el-button link type="primary" @click="$router.push('/order')">查看全部</el-button>
            </div>
          </template>
          <div v-if="pendingOrders.length > 0">
            <div
              v-for="order in pendingOrders.slice(0, 5)"
              :key="order.id"
              class="pending-item"
              @click="goOrderDetail(order)"
            >
              <div class="pending-info">
                <span class="order-no">订单 #{{ order.orderNo }}</span>
                <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
              </div>
              <div class="pending-meta">
                <span class="amount">¥{{ order.actualAmount }}</span>
                <span class="time">{{ formatTime(order.createdAt) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待处理订单" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: bold;">今日销售概览</span>
              <el-button link type="primary" @click="$router.push('/statistics')">查看详情</el-button>
            </div>
          </template>
          <div class="today-overview">
            <div class="overview-row">
              <div class="overview-item">
                <div class="overview-label">今日销售额</div>
                <div class="overview-value" style="color:#409eff">¥{{ todayStats.totalAmount || 0 }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">今日订单</div>
                <div class="overview-value" style="color:#67c23a">{{ todayStats.totalOrders || 0 }}</div>
              </div>
            </div>
            <div class="overview-row">
              <div class="overview-item">
                <div class="overview-label">今日销量</div>
                <div class="overview-value" style="color:#e6a23c">{{ todayStats.totalDishes || 0 }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">客单价</div>
                <div class="overview-value" style="color:#f56c6c">¥{{ todayStats.avgPrice || 0 }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  VideoPause,
  Dish,
  Bell,
  CircleCheck,
  ChatDotRound
} from '@element-plus/icons-vue'
import http from '../../utils/request'

const router = useRouter()

// ========== 状态 ==========
const status = ref(2)
const stats = ref({
  dishCount: 0,
  waitOrderCount: 0,
  todayFinishCount: 0,
  unreadCount: 0
})
const todayStats = ref({
  amount: 0,
  orders: 0,
  dishes: 0,
  avgPrice: 0
})
const pendingOrders = ref([])
let refreshTimer = null

const merchantStatusText = (value) => {
  return { 1: '营业中', 2: '暂停营业', 3: '禁止营业' }[value] || '未知状态'
}

const merchantStatusType = (value) => {
  return { 1: 'success', 2: 'warning', 3: 'danger' }[value] || 'info'
}

// ========== 初始化 ==========
onMounted(async () => {
  await Promise.all([
    getStatus(),
    getStats(),
    getUnreadCount(),
    getPendingOrders(),
    getTodayOverview()
  ])

  // 每30秒自动刷新数据
  refreshTimer = setInterval(() => {
    getStats()
    getUnreadCount()
    getPendingOrders()
    getTodayOverview()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

// ========== 营业状态 ==========
const getStatus = async () => {
  try {
    const res = await http.get('/api/merchant/info')
    if (res.code === 200) {
      status.value = res.data.status
    }
  } catch (err) {
    console.error('获取营业状态失败', err)
  }
}

const change = async (s) => {
  try {
    const res = await http.put('/api/merchant/status', { status: s })
    if (res.code === 200) {
      status.value = s
      ElMessage.success(s === 1 ? '开始营业' : '已暂停营业')
    }
  } catch (err) {
    ElMessage.error('状态更新失败')
  }
}

// ========== 统计数据 ==========
const getStats = async () => {
  try {
    const res = await http.get('/api/merchant/stats')
    if (res.code === 200) {
      stats.value.dishCount = res.data.dishCount || 0
      stats.value.waitOrderCount = res.data.waitOrderCount || 0
      stats.value.todayFinishCount = res.data.todayFinishCount || 0
    }
  } catch (err) {
    console.error('获取统计数据失败', err)
  }
}

const getUnreadCount = async () => {
  try {
    const res = await http.get('/api/merchant/message/unread/count')
    if (res.code === 200) {
      stats.value.unreadCount = res.data || 0
    }
  } catch (err) {
    console.error('获取未读消息失败', err)
  }
}

// ========== 今日销售概览 ==========
const getTodayOverview = async () => {
  try {
    const res = await http.get('/api/merchant/stats/sales/overview', {
      params: { quickType: 'today' }
    })
    if (res.code === 200) {
      todayStats.value = res.data
    }
  } catch (err) {
    console.error('获取今日概览失败', err)
  }
}

// ========== 待处理订单 ==========
const getPendingOrders = async () => {
  try {
    const res = await http.get('/api/merchant/orders', { params: { status: 2 } })
    if (res.code === 200) {
      pendingOrders.value = res.data || []
    }
  } catch (err) {
    console.error('获取待处理订单失败', err)
  }
}

const goOrderDetail = (order) => {
  router.push('/order')
}

// ========== 工具方法 ==========
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

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

/* 统计卡片 */
.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #666;
  font-size: 16px;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  margin-top: -5px;
}

/* 待处理订单 */
.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.pending-item:last-child {
  border-bottom: none;
}

.pending-item:hover {
  background: #f5f7fa;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.pending-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-no {
  font-weight: bold;
  color: #333;
}

.pending-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.time {
  color: #999;
  font-size: 12px;
}

/* 今日销售概览 */
.today-overview {
  padding: 10px 0;
}

.overview-row {
  display: flex;
  justify-content: space-around;
  padding: 15px 0;
}

.overview-row:first-child {
  border-bottom: 1px solid #eee;
}

.overview-item {
  text-align: center;
  flex: 1;
}

.overview-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
}
</style>
