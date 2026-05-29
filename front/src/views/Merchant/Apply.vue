<template>
  <div class="apply-container">
    <el-card>
      <h2 class="title">商家入驻申请</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="店铺名称">
          <el-input v-model="form.shopName"></el-input>
        </el-form-item>
        <el-form-item label="经营分类">
          <el-select v-model="form.category">
            <el-option label="快餐小吃" value="快餐小吃"></el-option>
            <el-option label="奶茶饮品" value="奶茶饮品"></el-option>
            <el-option label="面食主食" value="面食主食"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" maxlength="11"></el-input>
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="form.address"></el-input>
        </el-form-item>
        
        <!-- 营业执照上传 -->
        <el-form-item label="营业执照">
          <div style="display:flex;align-items:center;gap:15px">
            <el-upload
              :http-request="customUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">上传营业执照</el-button>
            </el-upload>

            <div v-if="form.businessLicense" style="width:120px;height:80px;border:1px solid #eee;border-radius:6px;overflow:hidden;cursor:pointer" @click="previewImage(form.businessLicense)">
              <img :src="form.businessLicense" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <div v-else style="color:#999;font-size:12px">请上传营业执照图片</div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitApply">提交申请</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const router = useRouter()
// 商家入驻申请表单数据
const form = ref({
  shopName: '',
  category: '',
  contactPerson: '',
  phone: '',
  address: '',
  businessLicense: ''
})

// 图片预览相关
const previewVisible = ref(false)
const previewUrl = ref('')

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

// 自定义上传方法 - 使用公共接口（无需token）
const customUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    // 使用公共上传接口，不需要token
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
  form.value.businessLicense = res.data
  ElMessage.success('营业执照上传成功')
}

const handleUploadError = (err) => {
  console.error('上传错误:', err)
}

const submitApply = async () => {
  if (!form.value.shopName) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  if (!form.value.category) {
    ElMessage.warning('请选择经营分类')
    return
  }
  if (!form.value.contactPerson) {
    ElMessage.warning('请输入联系人')
    return
  }
  if (!form.value.phone) {
    ElMessage.warning('请输入联系电话')
    return
  }
  if (!form.value.address) {
    ElMessage.warning('请输入店铺地址')
    return
  }
  
  try {
    const res = await http.post('/api/merchant/apply', form.value)
    if (res.code === 200) {
      ElMessage.success('申请提交成功！审核通过后，您将获得账号和密码，请耐心等待审核结果')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (err) {
    ElMessage.error('网络异常，请稍后重试')
  }
}
</script>

<style scoped>
.apply-container {
  padding: 80px;
  background: #f5f7fa;
  min-height: 80vh;
}
.title {
  text-align: center;
  margin-bottom: 20px;
}
</style>