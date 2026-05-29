<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">校园点餐管理系统</h1>
      <h3 class="subtitle">管理员登录</h3>
      <el-form :model="form" label-width="0" size="large">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
            show-password @keyup.enter="login" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="login">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer-link">
        <router-link to="/login">商家端登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const login = async () => {
  if (!form.value.username.trim()) {
    ElMessage.warning('请输入管理员账号')
    return
  }
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('/api/auth/admin/login', null, {
      params: {
        username: form.value.username,
        password: form.value.password
      }
    })
    if (res.data.code === 200) {
      localStorage.setItem('adminToken', res.data.data.token)
      ElMessage.success('登录成功')
      router.push('/admin/apply')
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (err) {
    ElMessage.error('登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1f2937 0%, #374151 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 0;
}

.subtitle {
  text-align: center;
  font-weight: normal;
  color: #909399;
  margin-top: 8px;
  margin-bottom: 30px;
}

.footer-link {
  text-align: center;
  margin-top: 15px;
}

.footer-link a {
  color: #909399;
  font-size: 14px;
  text-decoration: none;
}

.footer-link a:hover {
  color: #409eff;
}
</style>
