<template>
  <div class="app-container">
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="菜品名称/分类" clearable style="width:220px" />
        </el-form-item>
        <el-form-item label="商家ID">
          <el-input-number v-model="queryForm.merchantId" :min="1" :controls="false" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-select v-model="queryForm.shelfStatus" placeholder="全部" clearable style="width:140px">
            <el-option label="在售" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span style="font-weight:bold">菜品月销量</span>
      </template>

      <el-table :data="dishList" border v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="店铺" prop="shopName" min-width="150" />
        <el-table-column label="菜品名称" prop="name" min-width="150" />
        <el-table-column label="分类" prop="category" width="120" />
        <el-table-column label="单价" prop="price" width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="原价" prop="originalPrice" width="100">
          <template #default="{ row }">{{ row.originalPrice ? `￥${row.originalPrice}` : '-' }}</template>
        </el-table-column>
        <el-table-column label="折扣" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.discount && row.discount < 1" type="warning" size="small">
              {{ (row.discount * 10).toFixed(1) }}折
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="月销量" prop="monthlySales" width="110" sortable />
        <el-table-column label="库存" width="90">
          <template #default="{ row }">
            <el-tag :type="row.stockStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.stockStatus === 1 ? '有库存' : '无库存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上架状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.shelfStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.shelfStatus === 1 ? '在售' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const api = axios.create({ baseURL: '', timeout: 8000 })
api.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use(res => res.data)

const loading = ref(false)
const dishList = ref([])
const queryForm = ref({ keyword: '', merchantId: null, shelfStatus: null })

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryForm.value.keyword) params.keyword = queryForm.value.keyword
    if (queryForm.value.merchantId) params.merchantId = queryForm.value.merchantId
    if (queryForm.value.shelfStatus !== null && queryForm.value.shelfStatus !== '') {
      params.shelfStatus = queryForm.value.shelfStatus
    }
    const res = await api.get('/api/admin/dishes', { params })
    if (res.code === 200) dishList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.value = { keyword: '', merchantId: null, shelfStatus: null }
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.app-container {
  padding: 0;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
