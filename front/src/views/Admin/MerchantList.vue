<template>
  <div class="app-container">
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="店铺/联系人/电话" clearable style="width:220px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:140px">
            <el-option label="营业中" :value="1" />
            <el-option label="暂停营业" :value="2" />
            <el-option label="禁止营业" :value="3" />
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
        <span style="font-weight:bold">商家信息管理</span>
      </template>
      <el-table :data="merchantList" border v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="店铺名称" prop="shopName" min-width="150" />
        <el-table-column label="分类" prop="category" width="120" />
        <el-table-column label="联系人" prop="contactPerson" width="110" />
        <el-table-column label="联系电话" prop="phone" width="130" />
        <el-table-column label="地址" prop="address" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入驻时间" prop="createdAt" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-dropdown @command="status => changeStatus(row, status)">
              <el-button size="small" type="primary">状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1">设为营业中</el-dropdown-item>
                  <el-dropdown-item :command="2">设为暂停营业</el-dropdown-item>
                  <el-dropdown-item :command="3">设为禁止营业</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="danger" @click="openBlacklist(row)">拉黑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="编辑商家信息" width="520px">
      <el-form :model="editForm" label-width="96px">
        <el-form-item label="店铺名称"><el-input v-model="editForm.shopName" /></el-form-item>
        <el-form-item label="经营分类"><el-input v-model="editForm.category" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="editForm.contactPerson" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="店铺地址"><el-input v-model="editForm.address" /></el-form-item>
        <el-form-item label="店铺简介"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="blacklistVisible" title="加入黑名单" width="420px">
      <el-input v-model="blacklistReason" type="textarea" :rows="3" placeholder="请输入管控原因" />
      <template #footer>
        <el-button @click="blacklistVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBlacklist">确认拉黑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const api = axios.create({ baseURL: '', timeout: 8000 })
api.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use(res => res.data)

const loading = ref(false)
const merchantList = ref([])
const queryForm = ref({ keyword: '', status: null })
const editVisible = ref(false)
const blacklistVisible = ref(false)
const editForm = ref({})
const currentId = ref(null)
const blacklistReason = ref('')

const statusText = status => ({ 1: '营业中', 2: '暂停营业', 3: '禁止营业' }[status] || '未知')
const statusType = status => ({ 1: 'success', 2: 'warning', 3: 'danger' }[status] || 'info')

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryForm.value.keyword) params.keyword = queryForm.value.keyword
    if (queryForm.value.status !== null && queryForm.value.status !== '') params.status = queryForm.value.status
    const res = await api.get('/api/admin/merchants', { params })
    if (res.code === 200) merchantList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.value = { keyword: '', status: null }
  loadList()
}

const openEdit = row => {
  currentId.value = row.id
  editForm.value = { ...row }
  editVisible.value = true
}

const saveEdit = async () => {
  const res = await api.put(`/api/admin/merchants/${currentId.value}`, editForm.value)
  if (res.code === 200) {
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } else {
    ElMessage.error(res.message || '保存失败')
  }
}

const changeStatus = async (row, status) => {
  await ElMessageBox.confirm(`确认将 ${row.shopName} 设为“${statusText(status)}”？`, '状态管控')
  const res = await api.put(`/api/admin/merchants/${row.id}/status`, null, { params: { status } })
  if (res.code === 200) {
    ElMessage.success('状态已更新')
    loadList()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

const openBlacklist = row => {
  currentId.value = row.id
  blacklistReason.value = ''
  blacklistVisible.value = true
}

const confirmBlacklist = async () => {
  if (!blacklistReason.value.trim()) {
    ElMessage.warning('请输入管控原因')
    return
  }
  const res = await api.post(`/api/admin/merchants/${currentId.value}/blacklist`, null, {
    params: { reason: blacklistReason.value }
  })
  if (res.code === 200) {
    ElMessage.success('已加入黑名单')
    blacklistVisible.value = false
    loadList()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
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
