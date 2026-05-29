<template>
  <div class="app-container">
    <!-- 总览统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card" @click="filterByShelf(null)">
          <div class="overview-content" :class="{ active: activeFilter === null }">
            <div class="overview-icon" style="background: #ecf5ff; color: #409eff;">
              <el-icon :size="28"><Dish /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-label">全部菜品</div>
              <div class="overview-value" style="color: #409eff;">{{ overview.total }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card" @click="filterByShelf(1)">
          <div class="overview-content" :class="{ active: activeFilter === 1 }">
            <div class="overview-icon" style="background: #f0f9eb; color: #67c23a;">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-label">在售菜品</div>
              <div class="overview-value" style="color: #67c23a;">{{ overview.onSale }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="overview-card" @click="filterByShelf(0)">
          <div class="overview-content" :class="{ active: activeFilter === 0 }">
            <div class="overview-icon" style="background: #fef0f0; color: #f56c6c;">
              <el-icon :size="28"><CircleClose /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-label">已下架</div>
              <div class="overview-value" style="color: #f56c6c;">{{ overview.offSale }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选查询 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="菜品名称">
          <el-input v-model="queryForm.name" placeholder="支持模糊匹配" clearable />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-input v-model="queryForm.category" placeholder="请输入分类" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDishList">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>菜品列表</span>
          <div>
            <el-button type="success" @click="batchOnSale" :disabled="selectedIds.length === 0">
              批量上架
            </el-button>
            <el-button type="danger" @click="batchOffSale" :disabled="selectedIds.length === 0">
              批量下架
            </el-button>
            <el-button type="primary" @click="openAddDialog" style="margin-left:10px">
              + 新增菜品
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="dishList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="ID" prop="id" width="80" />

        <el-table-column label="菜品图片" width="100">
          <template #default="{ row }">
            <div 
              v-if="row.imageUrl" 
              style="width:60px;height:60px;border-radius:6px;overflow:hidden;cursor:pointer"
              @click="previewImage(row.imageUrl)"
            >
              <img :src="row.imageUrl" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <div v-else style="width:60px;height:60px;display:flex;align-items:center;justify-content:center;background:#f5f7fa;border-radius:6px">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="名称" prop="name" min-width="120" />
        <el-table-column label="分类" prop="category" width="100" />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="90">
          <template #default="{ row }">
            <span v-if="row.originalPrice" style="text-decoration:line-through;color:#999">¥{{ row.originalPrice }}</span>
            <span v-else style="color:#ccc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.discount && row.discount < 1" type="warning" size="small">{{ (row.discount * 10).toFixed(1) }}折</el-tag>
            <span v-else style="color:#ccc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="月销量" width="90" sortable>
          <template #default="{ row }">
            {{ row.monthlySales ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="口味" prop="taste" width="100">
          <template #default="{ row }">
            <span>{{ row.taste || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.stockStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.stockStatus === 1 ? '有库存' : '无库存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上架状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.shelfStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.shelfStatus === 1 ? '在售' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button 
              :type="row.shelfStatus === 1 ? 'warning' : 'success'" 
              link 
              @click="toggleShelf(row)"
            >
              {{ row.shelfStatus === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="菜品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width:200px" :disabled="isDiscounted" />
          <span v-if="isDiscounted" style="margin-left:10px;color:#909399;font-size:12px">根据原价和折扣自动计算</span>
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :precision="2" :min="0" style="width:200px" />
        </el-form-item>
        <el-form-item label="折扣">
          <el-input-number v-model="form.discount" :precision="2" :min="0.1" :max="1" :step="0.05" style="width:200px" />
          <span style="margin-left:10px;color:#909399;font-size:12px">0.1~1.0，1.0 表示不打折</span>
        </el-form-item>
        <el-form-item label="口味">
          <el-input v-model="form.taste" placeholder="如：微辣/中辣/麻辣" />
        </el-form-item>
        <el-form-item label="菜品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <!-- 菜品图片上传 -->
        <el-form-item label="菜品图片">
          <div style="display:flex;align-items:center;gap:15px">
            <el-upload
              :http-request="customUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">上传图片</el-button>
            </el-upload>

            <div v-if="form.imageUrl" style="width:80px;height:80px;border:1px solid #eee;border-radius:6px;overflow:hidden">
              <img :src="form.imageUrl" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <div v-else style="color:#999;font-size:12px">暂无图片</div>
          </div>
        </el-form-item>

        <el-form-item label="库存状态">
          <el-radio-group v-model="form.stockStatus">
            <el-radio :value="1">有库存</el-radio>
            <el-radio :value="0">无库存</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog 
      v-model="previewVisible" 
      title="图片预览" 
      width="600px" 
      align-center
      :close-on-click-modal="true"
    >
      <div style="display:flex;justify-content:center;padding:20px">
        <img :src="previewUrl" style="max-width:100%;max-height:70vh;border-radius:8px" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Search, CircleCheck, CircleClose, Dish } from '@element-plus/icons-vue'
import http from '../../utils/request'

const queryForm = ref({ name: '', category: '' })
const dishList = ref([])
const allDishes = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜品')
const form = ref({})
const selectedIds = ref([])
const activeFilter = ref(null)  // null=全部, 1=在售, 0=已下架
const isDiscounted = computed(() => Number(form.value.discount) > 0 && Number(form.value.discount) < 1)

// 总览统计
const overview = computed(() => {
  const total = allDishes.value.length
  const onSale = allDishes.value.filter(d => d.shelfStatus === 1).length
  const offSale = allDishes.value.filter(d => d.shelfStatus === 0).length
  return { total, onSale, offSale }
})

// 图片预览相关
const previewVisible = ref(false)
const previewUrl = ref('')

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

// 加载全部菜品（用于统计）
const loadAllDishes = async () => {
  try {
    const res = await http.get('/api/merchant/dishes')
    if (res.code === 200) {
      allDishes.value = res.data || []
    }
  } catch (err) {
    console.error('加载全部菜品失败', err)
  }
}

// 加载菜品列表（带筛选）
const loadDishList = async () => {
  try {
    const params = {}
    if (queryForm.value.name) params.name = queryForm.value.name
    if (queryForm.value.category) params.category = queryForm.value.category
    if (activeFilter.value !== null) params.shelfStatus = activeFilter.value

    const res = await http.get('/api/merchant/dishes', { params })
    if (res.code === 200) {
      dishList.value = res.data
    }
  } catch (err) {
    console.error('加载菜品列表失败', err)
  }
}

// 点击总览卡片筛选
const filterByShelf = (status) => {
  activeFilter.value = status
  loadDishList()
}

// 重置查询
const resetQuery = () => {
  queryForm.value = { name: '', category: '' }
  activeFilter.value = null
  loadDishList()
}

// 弹窗操作
const openAddDialog = () => {
  dialogTitle.value = '新增菜品'
  form.value = {
    name: '',
    category: '',
    price: null,
    originalPrice: null,
    discount: 1.0,
    taste: '',
    description: '',
    imageUrl: '',
    stockStatus: 1
  }
  dialogVisible.value = true
}

watch(
  () => [form.value.originalPrice, form.value.discount],
  () => {
    if (isDiscounted.value && form.value.originalPrice) {
      form.value.price = Number((Number(form.value.originalPrice) * Number(form.value.discount)).toFixed(2))
    }
  }
)

const buildDishPayload = () => {
  const payload = { ...form.value }
  const discount = Number(payload.discount || 1)
  if (payload.originalPrice && discount > 0 && discount < 1) {
    payload.price = Number((Number(payload.originalPrice) * discount).toFixed(2))
  }
  return payload
}

const openEditDialog = (row) => {
  dialogTitle.value = '修改菜品'
  form.value = { ...row }
  dialogVisible.value = true
}

// 图片上传
const customUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await http.post('/api/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.code === 200) {
      options.onSuccess(res)
    } else {
      options.onError(new Error(res.message || '上传失败'))
      ElMessage.error(res.message || '上传失败')
    }
  } catch (err) {
    options.onError(err)
    ElMessage.error('上传失败：' + (err.response?.data?.message || err.message || '网络错误'))
  }
}

const handleUploadSuccess = (res) => {
  form.value.imageUrl = res.data
  ElMessage.success('图片上传成功')
}

const handleUploadError = (err) => {
  console.error('上传错误:', err)
}

// 提交表单
const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入菜品名称')
    return
  }
  if (!form.value.category) {
    ElMessage.warning('请输入菜品分类')
    return
  }
  if (!form.value.price || form.value.price <= 0) {
    ElMessage.warning('请输入正确的单价')
    return
  }

  let res
  if (form.value.id) {
    res = await http.put('/api/merchant/dishes', buildDishPayload())
  } else {
    res = await http.post('/api/merchant/dishes', buildDishPayload())
  }

  if (res.code === 200) {
    ElMessage.success(form.value.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadDishList()
    loadAllDishes()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

// 上下架
const toggleShelf = async (row) => {
  const newStatus = row.shelfStatus === 1 ? 0 : 1
  const res = await http.put(`/api/merchant/dishes/${row.id}/shelf/${newStatus}`)
  if (res.code === 200) {
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadDishList()
    loadAllDishes()
  }
}

// 批量操作
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

const batchOnSale = async () => {
  const res = await http.put('/api/merchant/dishes/batch/shelf/1', selectedIds.value)
  if (res.code === 200) {
    ElMessage.success('批量上架成功')
    loadDishList()
    loadAllDishes()
  }
}

const batchOffSale = async () => {
  const res = await http.put('/api/merchant/dishes/batch/shelf/0', selectedIds.value)
  if (res.code === 200) {
    ElMessage.success('批量下架成功')
    loadDishList()
    loadAllDishes()
  }
}

onMounted(() => {
  loadDishList()
  loadAllDishes()
})
</script>

<style scoped>
.mb-4 { 
  margin-bottom: 16px; 
}

/* 总览卡片 */
.overview-card {
  cursor: pointer;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.overview-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 5px;
  border-radius: 8px;
  transition: background 0.3s;
}

.overview-content.active {
  background: #f0f9ff;
  box-shadow: inset 0 0 0 2px #409eff;
}

.overview-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-info {
  flex: 1;
}

.overview-label {
  color: #666;
  font-size: 16px;
  margin-bottom: 5px;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
}
</style>
