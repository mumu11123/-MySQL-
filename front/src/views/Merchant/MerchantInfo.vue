<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>店铺基本信息</span>
      </template>
      
      <el-form :model="merchantForm" label-width="100px">
        <el-form-item label="店铺名称">
          <el-input v-model="merchantForm.shopName"></el-input>
        </el-form-item>
        <el-form-item label="经营分类">
          <el-input v-model="merchantForm.category"></el-input>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="merchantForm.contactPerson"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="merchantForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="merchantForm.address"></el-input>
        </el-form-item>
        <el-form-item label="店铺简介">
          <el-input v-model="merchantForm.description" type="textarea"></el-input>
        </el-form-item>
        
        <!-- Logo上传 -->
        <el-form-item label="店铺Logo">
          <div style="display:flex;align-items:center;gap:15px">
            <el-upload
              :http-request="customUpload"
              :on-success="handleLogoSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">上传Logo</el-button>
            </el-upload>

            <div v-if="merchantForm.logo" style="width:80px;height:80px;border:1px solid #eee;border-radius:6px;overflow:hidden;cursor:pointer" @click="previewImage(merchantForm.logo)">
              <img :src="merchantForm.logo" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <div v-else style="color:#999;font-size:12px">暂无Logo</div>
          </div>
        </el-form-item>

        <el-form-item label="营业执照">
          <div v-if="merchantForm.businessLicense" class="license-preview" @click="previewImage(merchantForm.businessLicense)">
            <img :src="merchantForm.businessLicense" />
          </div>
          <div v-else style="color:#999;font-size:12px">暂无营业执照</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveMerchant">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const merchantForm = ref({})

// 图片预览相关
const previewVisible = ref(false)
const previewUrl = ref('')

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const getMerchantInfo = async () => {
  const res = await http.get('/api/merchant/info')
  if (res.code === 200) {
    merchantForm.value = res.data
  }
}

// 使用公共上传接口
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

const handleLogoSuccess = (res) => {
  merchantForm.value.logo = res.data
  ElMessage.success('Logo上传成功')
}

const handleUploadError = (err) => {
  console.error('上传错误:', err)
}

const saveMerchant = async () => {
  try {
    const res = await http.put('/api/merchant/info', merchantForm.value)
    if (res.code === 200) {
      ElMessage.success('修改成功')
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (err) {
    ElMessage.error('网络异常，请稍后重试')
  }
}

onMounted(() => {
  getMerchantInfo()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.license-preview {
  width: 180px;
  height: 120px;
  border: 1px solid #eee;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
}

.license-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
