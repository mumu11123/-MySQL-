<template>
  <el-container style="height:98vh; overflow:hidden">
    <el-aside width="220px" style="background:#1f2937; display:flex; flex-direction:column; height:98vh">
      <div style="color:#fff;text-align:center;padding:20px;font-size:18px;border-bottom:1px solid #374151">
        管理员后台
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        text-color="#9ca3af"
        active-text-color="#fff"
        background-color="#1f2937"
        style="flex:1; overflow-y:auto; border-right:none"
      >
        <el-menu-item index="/admin/apply">入驻审核</el-menu-item>
        <el-menu-item index="/admin/merchants">商家管理</el-menu-item>
        <el-menu-item index="/admin/dishes">菜品销量</el-menu-item>
        <el-menu-item index="/admin/blacklist">黑名单管理</el-menu-item>
        <el-menu-item index="/admin/system">系统维护</el-menu-item>
      </el-menu>

      <div style="padding:15px; text-align:center; border-top:1px solid #374151">
        <el-button type="danger" @click="logout" style="width:80%">退出登录</el-button>
      </div>
    </el-aside>

    <el-main style="background:#f5f7fa;padding:20px">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('adminToken')
    ElMessage.success('退出成功')
    router.push('/admin/login')
  }).catch(() => {
    ElMessage.info('已取消')
  })
}
</script>
