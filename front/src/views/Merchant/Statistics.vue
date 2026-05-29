<template>
  <div class="app-container">
    <!-- 快捷筛选 -->
    <el-card class="mb-4">
      <el-radio-group v-model="quickType" @change="handleQuickChange">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>

      <el-date-picker
        v-if="quickType === 'custom'"
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="margin-left:20px"
        @change="loadAll"
      />
    </el-card>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">{{ overviewLabel.amount }}</div>
            <div class="stat-value" style="color:#f56c6c">¥{{ overview.totalAmount }}</div>
            <div class="stat-ratio" v-if="overview.amountRingRatio">
              环比 {{ overview.amountRingRatio }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">{{ overviewLabel.orders }}</div>
            <div class="stat-value" style="color:#409eff">{{ overview.totalOrders }}</div>
            <div class="stat-ratio" v-if="overview.orderRingRatio">
              环比 {{ overview.orderRingRatio }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">{{ overviewLabel.dishes }}</div>
            <div class="stat-value" style="color:#e6a23c">{{ overview.totalDishes }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">{{ overviewLabel.avgPrice }}</div>
            <div class="stat-value" style="color:#67c23a">¥{{ overview.avgPrice }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日：时段销售分布柱状图 -->
    <el-card v-if="quickType === 'today'" class="mb-4">
      <template #header>
        <span style="font-weight:bold">今日时段销售分布</span>
      </template>
      <div v-if="hourlyData.length > 0" class="chart-container">
        <div class="chart-bars">
          <div
            v-for="item in hourlyData"
            :key="item.hour"
            class="bar-wrapper"
            :class="{ 'low-activity': isLowActivityHour(item.hour) }"
          >
            <div class="bar-group">
              <div
                class="bar"
                :style="{ height: getBarHeight(item.amount) + 'px' }"
                :title="`¥${item.amount} / ${item.orders}单`"
              ></div>
            </div>
            <span class="bar-label">{{ item.hour }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无今日销售数据" />
    </el-card>

    <!-- 每日销售明细表格 -->
    <el-card class="mb-4">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:bold">{{ tableTitle }}</span>
          <span style="color:#999;font-size:13px">共 {{ dailyList.length }} 天</span>
        </div>
      </template>

      <el-table :data="dailyList" border v-loading="loading">
        <el-table-column label="日期" width="140">
          <template #default="{row}">
            {{ row.date }} {{ getWeekday(row.date) }}
          </template>
        </el-table-column>
        <el-table-column label="销售额" prop="dailyAmount">
          <template #default="{row}">
            ¥{{ row.dailyAmount }}
          </template>
        </el-table-column>
        <el-table-column label="订单数" prop="dailyOrders" />
        <el-table-column label="销量" prop="dailyDishes" />
        <el-table-column label="客单价" prop="dailyAvgPrice">
          <template #default="{row}">
            ¥{{ row.dailyAvgPrice }}
          </template>
        </el-table-column>
        <el-table-column label="环比" prop="ringRatio">
          <template #default="{row}">
            <el-tag
              :type="row.ringRatio.startsWith('+') ? 'success' : row.ringRatio.startsWith('-') ? 'danger' : 'info'"
              size="small"
            >
              {{ row.ringRatio }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="quickType !== 'today'"
        v-model:current-page="dailyPage"
        v-model:page-size="dailyPageSize"
        layout="total, prev, pager, next"
        style="margin-top:20px;justify-content:flex-end"
        @current-change="loadDailyList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '../../utils/request'

const quickType = ref('today')
const dateRange = ref([])

const overview = ref({
  totalAmount: 0,
  totalOrders: 0,
  totalDishes: 0,
  avgPrice: 0,
  amountRingRatio: '',
  orderRingRatio: ''
})

const dailyList = ref([])
const dailyPage = ref(1)
const dailyPageSize = ref(10)
const dailyTotal = ref(0)
const loading = ref(false)
const hourlyData = ref([])

// 低活跃时段（凌晨）用灰色标识
const lowActivityHours = ['00:00', '02:00', '04:00']

// ========== 计算属性 ==========
const overviewLabel = computed(() => {
  const map = {
    today: { amount: '今日销售额', orders: '今日订单', dishes: '今日销量', avgPrice: '今日客单价' },
    week: { amount: '本周销售额', orders: '本周订单', dishes: '本周销量', avgPrice: '本周客单价' },
    month: { amount: '本月销售额', orders: '本月订单', dishes: '本月销量', avgPrice: '本月客单价' },
    custom: { amount: '区间销售额', orders: '区间订单', dishes: '区间销量', avgPrice: '区间客单价' }
  }
  return map[quickType.value] || map.today
})

const tableTitle = computed(() => {
  const map = {
    today: '今日销售汇总',
    week: '本周每日销售明细',
    month: '本月每日销售明细',
    custom: '自定义区间销售明细'
  }
  return map[quickType.value] || '销售明细'
})

// ========== 快捷筛选切换 ==========
const handleQuickChange = () => {
  if (quickType.value !== 'custom') {
    loadAll()
  }
}

const loadAll = async () => {
  await Promise.all([
    loadOverview(),
    loadDailyList()
  ])
  // 今日额外加载时段数据
  if (quickType.value === 'today') {
    await loadHourlySales()
  }
}

// ========== 概览数据 ==========
const loadOverview = async () => {
  const params = buildDateParams()
  const res = await http.get('/api/merchant/stats/sales/overview', { params })
  if (res.code === 200) {
    overview.value = res.data
  }
}

// ========== 每日明细 ==========
const loadDailyList = async () => {
  loading.value = true
  const params = {
    ...buildDateParams(),
    page: dailyPage.value,
    pageSize: dailyPageSize.value
  }
  const res = await http.get('/api/merchant/stats/daily/list', { params })
  if (res.code === 200) {
    dailyList.value = res.data
    dailyTotal.value = res.data.length
  }
  loading.value = false
}

// ========== 今日时段销售趋势 ==========
const getLocalDateString = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadHourlySales = async () => {
  try {
    const today = getLocalDateString()
    const res = await http.get('/api/merchant/stats/hourly', { params: { date: today } })
    if (res.code === 200) {
      hourlyData.value = res.data || []
    }
  } catch (err) {
    console.error('获取时段销售数据失败', err)
    hourlyData.value = []
  }
}

// ========== 柱状图工具方法 ==========
const isLowActivityHour = (hour) => {
  return lowActivityHours.includes(hour)
}

const maxBarHeight = 150
const getBarHeight = (amount) => {
  if (!amount || hourlyData.value.length === 0) return 0
  const maxAmount = Math.max(...hourlyData.value.map(d => parseFloat(d.amount) || 0))
  if (maxAmount === 0) return 0
  return Math.max((parseFloat(amount) / maxAmount) * maxBarHeight, 3)
}

// ========== 工具方法 ==========
const getWeekday = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekdays = ['(日)', '(一)', '(二)', '(三)', '(四)', '(五)', '(六)']
  return weekdays[date.getDay()]
}

const buildDateParams = () => {
  if (quickType.value === 'custom' && dateRange.value.length === 2) {
    return {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
  }
  return { quickType: quickType.value }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.mb-4 {
  margin-bottom: 16px;
}

/* 概览卡片 */
.stat-item {
  text-align: center;
  padding: 10px;
}
.stat-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}
.stat-ratio {
  color: #999;
  font-size: 12px;
}

/* ===== 时段销售趋势柱状图 ===== */
.chart-container {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 10px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  width: 100%;
  height: 180px;
  gap: 4px;
}

.bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  gap: 6px;
}

/* 低活跃时段（凌晨）用灰色 */
.bar-wrapper.low-activity .bar {
  background: linear-gradient(to top, #d9d9d9, #f0f0f0);
}

.bar-wrapper.low-activity .bar-label {
  color: #999;
}

.bar-group {
  display: flex;
  align-items: flex-end;
  height: 150px;
}

.bar {
  width: 18px;
  min-height: 3px;
  border-radius: 4px 4px 0 0;
  background: linear-gradient(to top, #52c41a, #95de64);
  transition: height 0.5s ease;
  cursor: pointer;
}

.bar:hover {
  background: linear-gradient(to top, #389e0d, #73d13d);
}

.bar-label {
  font-size: 11px;
  color: #666;
  white-space: nowrap;
}
</style>