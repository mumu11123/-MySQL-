<template>
  <div class="app-container">
    <el-card class="mb-4">
      <el-radio-group v-model="filterStatus" @change="loadList">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">已驳回</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card>
      <template #header>
        <span style="font-weight: bold;">入驻申请列表</span>
      </template>

      <el-table :data="applyList" border v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="店铺名称" prop="shopName" min-width="140" />
        <el-table-column label="经营分类" prop="category" width="120" />
        <el-table-column label="联系人" prop="contactPerson" width="100" />
        <el-table-column label="联系电话" prop="phone" width="130" />
        <el-table-column label="店铺地址" prop="address" min-width="160" show-overflow-tooltip />
        <el-table-column label="营业执照" width="100">
          <template #default="{ row }">
            <el-button v-if="row.businessLicense" link type="primary" @click="previewImage(row.businessLicense)">
              查看
            </el-button>
            <span v-else style="color:#999">未上传</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="appliedAt" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
              <el-button type="warning" size="small" @click="handleSupplement(row)">退回补充</el-button>
            </template>
            <template v-if="row.status === 2">
              <el-button type="warning" size="small" @click="handleBlacklist(row)">加入黑名单</el-button>
            </template>
            <span v-if="row.status === 1" style="color:#999">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 驳回原因输入弹窗 -->
    <el-dialog v-model="rejectVisible" title="审核驳回" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因（选填）" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="supplementVisible" title="退回补充资料" width="400px">
      <el-input v-model="supplementReason" type="textarea" :rows="3" placeholder="请输入需要补充或修正的资料说明" />
      <template #footer>
        <el-button @click="supplementVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmSupplement">确认退回</el-button>
      </template>
    </el-dialog>

    <!-- 通过确认弹窗 -->
    <el-dialog v-model="approveVisible" title="审核通过" width="400px">
      <p>确认审核通过该商家入驻申请？</p>
      <p style="color:#909399;font-size:13px">通过后将自动创建商家账号。</p>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="success" @click="confirmApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- 审核通过结果弹窗 -->
    <el-dialog v-model="resultVisible" title="审核通过 - 商家账号信息" width="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商家账号">{{ createdAccount.username }}</el-descriptions-item>
        <el-descriptions-item label="初始密码">{{ createdAccount.password }}</el-descriptions-item>
      </el-descriptions>
      <p style="color:#f56c6c;font-size:13px;margin-top:10px">
        请妥善保管，告知商家登录后尽快修改密码！
      </p>
      <template #footer>
        <el-button type="primary" @click="resultVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 加入黑名单弹窗 -->
    <el-dialog v-model="blacklistVisible" title="加入黑名单" width="400px">
      <el-input v-model="blacklistReason" type="textarea" :rows="3" placeholder="请输入拉黑原因" />
      <template #footer>
        <el-button @click="blacklistVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBlacklist">确认拉黑</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible" title="营业执照" width="600px" align-center :close-on-click-modal="true">
      <div style="display:flex;justify-content:center;padding:20px">
        <img :src="previewUrl" style="max-width:100%;max-height:70vh;border-radius:8px" />
      </div>
    </el-dialog>
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
const applyList = ref([])
const filterStatus = ref(null)
const rejectVisible = ref(false)
const approveVisible = ref(false)
const resultVisible = ref(false)
const blacklistVisible = ref(false)
const supplementVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const rejectReason = ref('')
const blacklistReason = ref('')
const supplementReason = ref('')
const currentId = ref(null)
const createdAccount = ref({ username: '', password: '' })

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value !== null) params.status = filterStatus.value
    const res = await api.get('/api/admin/apply/list', { params })
    if (res.code === 200) {
      applyList.value = res.data || []
    }
  } catch (err) {
    console.error('加载申请列表失败', err)
  } finally {
    loading.value = false
  }
}

const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '退回补充' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const handleApprove = (row) => {
  currentId.value = row.id
  approveVisible.value = true
}

const confirmApprove = async () => {
  try {
    const res = await api.put(`/api/admin/apply/${currentId.value}/approve`)
    if (res.code === 200) {
      createdAccount.value = res.data || {}
      approveVisible.value = false
      resultVisible.value = true
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleReject = (row) => {
  currentId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  try {
    const res = await api.put(`/api/admin/apply/${currentId.value}/reject`, null, {
      params: { rejectReason: rejectReason.value }
    })
    if (res.code === 200) {
      ElMessage.success('已驳回')
      rejectVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleSupplement = (row) => {
  currentId.value = row.id
  supplementReason.value = ''
  supplementVisible.value = true
}

const confirmSupplement = async () => {
  try {
    const res = await api.put(`/api/admin/apply/${currentId.value}/supplement`, null, {
      params: { reason: supplementReason.value }
    })
    if (res.code === 200) {
      ElMessage.success('已退回补充资料')
      supplementVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleBlacklist = (row) => {
  currentId.value = row.id
  blacklistReason.value = ''
  blacklistVisible.value = true
}

const confirmBlacklist = async () => {
  if (!blacklistReason.value.trim()) {
    ElMessage.warning('请输入拉黑原因')
    return
  }
  try {
    const res = await api.post(`/api/admin/apply/${currentId.value}/blacklist`, null, {
      params: { reason: blacklistReason.value }
    })
    if (res.code === 200) {
      ElMessage.success('已加入黑名单')
      blacklistVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.app-container {
  padding: 0;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
