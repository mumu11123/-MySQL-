<template>
  <div class="login-wrap">
    <el-card shadow="hover" style="width:400px;padding:30px">
      <h2 style="text-align:center;margin-bottom:35px">商家后台登录</h2>
      <el-form :model="loginForm">
        <el-form-item label="登录账号">
          <el-input v-model="loginForm.username"></el-input>
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="loginForm.password" show-password></el-input>
        </el-form-item>
        
        <el-button type="primary" style="width:100%" @click="login">立即登录</el-button>

        <div style="text-align:center;margin-top:15px;display:flex;justify-content:space-between">
          <span>还未入驻？
          <span style="color:#409eff;cursor:pointer" @click="goApply">
            申请入驻
          </span>
          </span>
          <span style="color:#409eff;cursor:pointer" @click="goApplyStatus">
            查询入驻进度
          </span>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const router = useRouter()
const loginForm = ref({
  username: '',
  password: ''
})

const login = async () => {
  if (!loginForm.value.username.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!loginForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  try {
    const res = await http.post('/api/auth/login/merchant', loginForm.value)
    if (res.code === 200) {
      localStorage.setItem('merchantToken', res.data)
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(res.message || '用户名或密码错误')
    }
  } catch (err) {
    ElMessage.error('网络异常，请稍后重试')
  }
}

const goApply = () => {
  router.push('/apply')
}

const goApplyStatus = () => {
  router.push('/apply-status')
}
</script>

<style scoped>
.login-wrap{
  height: 98vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}
</style>