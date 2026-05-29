<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统参数" name="params">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统参数配置</span>
              <el-button type="warning" size="small" @click="resetParams">恢复默认</el-button>
            </div>
          </template>
          <el-table :data="params" border v-loading="loadingParams">
            <el-table-column label="分类" prop="category" width="100" />
            <el-table-column label="参数名称" prop="name" min-width="150" />
            <el-table-column label="参数值" prop="value" width="160" />
            <el-table-column label="说明" prop="remark" min-width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="openParam(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="备份恢复" name="backup">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据备份与恢复</span>
              <el-button type="primary" size="small" @click="createBackup">手动备份</el-button>
            </div>
          </template>
          <el-table :data="backups" border v-loading="loadingBackups">
            <el-table-column label="备份文件" prop="filename" min-width="220" />
            <el-table-column label="类型" prop="type" width="110" />
            <el-table-column label="状态" prop="status" width="110" />
            <el-table-column label="时间" prop="createdAt" width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="restore(row)">恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="logs">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>操作日志查询</span>
              <el-button size="small" @click="loadLogs">刷新</el-button>
            </div>
          </template>
          <el-table :data="logs" border v-loading="loadingLogs">
            <el-table-column label="操作人" prop="operator" width="110" />
            <el-table-column label="动作" prop="action" width="150" />
            <el-table-column label="详情" prop="detail" min-width="220" show-overflow-tooltip />
            <el-table-column label="时间" prop="createdAt" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="paramVisible" title="编辑系统参数" width="420px">
      <el-form :model="paramForm" label-width="84px">
        <el-form-item label="参数名称">{{ paramForm.name }}</el-form-item>
        <el-form-item label="参数说明">{{ paramForm.remark }}</el-form-item>
        <el-form-item label="参数值">
          <el-input v-model="paramForm.value" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paramVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParam">保存</el-button>
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

const activeTab = ref('params')
const params = ref([])
const backups = ref([])
const logs = ref([])
const loadingParams = ref(false)
const loadingBackups = ref(false)
const loadingLogs = ref(false)
const paramVisible = ref(false)
const paramForm = ref({})

const loadParams = async () => {
  loadingParams.value = true
  try {
    const res = await api.get('/api/admin/system/params')
    if (res.code === 200) params.value = res.data || []
  } finally {
    loadingParams.value = false
  }
}

const openParam = row => {
  paramForm.value = { ...row }
  paramVisible.value = true
}

const saveParam = async () => {
  const res = await api.put(`/api/admin/system/params/${paramForm.value.code}`, { value: paramForm.value.value })
  if (res.code === 200) {
    ElMessage.success('参数配置成功')
    paramVisible.value = false
    loadParams()
    loadLogs()
  } else {
    ElMessage.error(res.message || '保存失败')
  }
}

const resetParams = async () => {
  await ElMessageBox.confirm('确认恢复默认系统参数？', '提示')
  const res = await api.post('/api/admin/system/params/reset')
  if (res.code === 200) {
    ElMessage.success('已恢复默认参数')
    loadParams()
    loadLogs()
  }
}

const loadBackups = async () => {
  loadingBackups.value = true
  try {
    const res = await api.get('/api/admin/system/backups')
    if (res.code === 200) backups.value = res.data || []
  } finally {
    loadingBackups.value = false
  }
}

const createBackup = async () => {
  const res = await api.post('/api/admin/system/backup')
  if (res.code === 200) {
    ElMessage.success('备份完成')
    loadBackups()
    loadLogs()
  }
}

const restore = async row => {
  await ElMessageBox.confirm(`确认恢复备份 ${row.filename}？`, '数据恢复')
  const res = await api.post('/api/admin/system/restore', null, { params: { filename: row.filename } })
  if (res.code === 200) {
    ElMessage.success('恢复任务已记录')
    loadLogs()
  }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await api.get('/api/admin/system/logs')
    if (res.code === 200) logs.value = res.data || []
  } finally {
    loadingLogs.value = false
  }
}

onMounted(() => {
  loadParams()
  loadBackups()
  loadLogs()
})
</script>

<style scoped>
.app-container {
  padding: 0;
}
.card-header {
  align-items: center;
  display: flex;
  justify-content: space-between;
}
</style>
