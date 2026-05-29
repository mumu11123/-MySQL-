<template>
  <div class="app-container">
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
        @change="loadDishRank"
      />
    </el-card>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>菜品销售排行</span>
          <el-select v-model="rankLimit" style="width:120px" @change="loadDishRank">
            <el-option label="TOP 3" :value="3" />
            <el-option label="TOP 10" :value="10" />
            <el-option label="全部" :value="0" />
          </el-select>
        </div>
      </template>

      <el-table :data="dishRank" border v-loading="loading">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column label="菜品名称" prop="dishName" />
        <el-table-column label="销量" prop="saleCount" sortable />
        <el-table-column label="销售额" prop="saleAmount" sortable>
          <template #default="{ row }">
            ￥{{ row.saleAmount }}
          </template>
        </el-table-column>
        <el-table-column label="占比" prop="amountPercent" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../../utils/request'

const quickType = ref('today')
const dateRange = ref([])
const rankLimit = ref(10)
const dishRank = ref([])
const loading = ref(false)

const handleQuickChange = () => {
  if (quickType.value !== 'custom') {
    loadDishRank()
  }
}

const loadDishRank = async () => {
  loading.value = true
  try {
    const params = {
      ...buildDateParams(),
      limit: rankLimit.value || undefined
    }
    const res = await http.get('/api/merchant/stats/dish/rank', { params })
    if (res.code === 200) {
      dishRank.value = res.data
    }
  } catch (err) {
    console.error('加载菜品排行失败', err)
  } finally {
    loading.value = false
  }
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
  loadDishRank()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
