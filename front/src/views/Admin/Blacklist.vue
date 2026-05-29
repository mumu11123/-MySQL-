<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span style="font-weight: bold;">商家黑名单</span>
      </template>

      <el-table :data="blacklist" border v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="商家名称" prop="shopName" min-width="140">
          <template #default="{ row }">
            {{ row.shopName || '(商家ID: ' + row.merchantId + ')' }}
          </template>
        </el-table-column>
        <el-table-column label="商家ID" prop="merchantId" width="100" />
        <el-table-column label="拉黑原因" prop="reason" min-width="200" show-overflow-tooltip />
        <el-table-column label="拉黑时间" prop="createdAt" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确认移出黑名单？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="handleRemove(row)"
            >
              <template #reference>
                <el-button type="danger" size="small" link>移出黑名单</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const api = axios.create({ baseURL: '', timeout: 8000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => res.data,
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('adminToken')
      location.href = '/admin/login'
    }
    return Promise.reject(err)
  }
)

const loading = ref(false)
const blacklist = ref([])

const loadList = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/admin/blacklist')
    if (res.code === 200) {
      blacklist.value = res.data || []
    }
  } catch (err) {
    console.error('加载黑名单失败', err)
  } finally {
    loading.value = false
  }
}

const handleRemove = async (row) => {
  try {
    const res = await api.delete(`/api/admin/apply/${row.id}/blacklist/${row.merchantId}`)
    if (res.code === 200) {
      ElMessage.success('已移出黑名单')
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadList()
})
</script>
