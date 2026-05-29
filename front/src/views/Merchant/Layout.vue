<template>
  <el-container style="height:98vh;border:0; margin:0; padding:0; overflow:hidden">
    <el-aside width="220px" style="background:#304156; display:flex; flex-direction:column; height:98vh">
      <div style="color:#fff;text-align:center;padding:20px;font-size:18px;border-bottom:1px solid #48576a">
        校园点餐商家后台
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        text-color="#fff"
        active-text-color="#ffd04b"
        background-color="#304156"
        style="flex:1; overflow-y:auto"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/merchant/info">店铺管理</el-menu-item>
        <el-menu-item index="/dish">菜品管理</el-menu-item>
        <el-menu-item index="/order">订单管理</el-menu-item>
        <el-menu-item index="/message">消息聊天</el-menu-item>
        <el-sub-menu index="/statistics" :class="{ 'is-active': isStatisticsActive }">
          <template #title>
            <span>销售统计</span>
          </template>
          <el-menu-item index="/statistics">销售总览</el-menu-item>
          <el-menu-item index="/dish-rank">菜品排行</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div style="padding:15px; text-align:center; border-top:1px solid #48576a">
        <el-button type="danger" @click="logout" style="width:80%">退出登录</el-button>
      </div>
    </el-aside>

    <el-main style="background:#f5f7fa;padding:20px">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  // 销售统计子菜单展开高亮
  if (['/statistics', '/dish-rank'].includes(path)) {
    return path
  }
  return path
})

// 判断销售统计父菜单是否高亮
const isStatisticsActive = computed(() => {
  return ['/statistics', '/dish-rank'].includes(route.path)
})

const logout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    localStorage.removeItem('merchantToken')
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {
    ElMessage.info('已取消')
  })
}
</script>

<style scoped>
:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #ffd04b !important;
}
</style>