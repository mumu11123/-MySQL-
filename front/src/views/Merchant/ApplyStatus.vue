<template>
  <div class="status-page" style="padding:100px;background:#f5f7fa;min-height:76vh;">
    <el-card>
      <div style="position: relative; text-align: center; margin-bottom: 40px;">
        <h2 style="margin: 0;">入驻申请进度查询</h2>
        <el-button 
          @click="$router.push('/login')" 
          style="position: absolute; right: 0; top: 0;"
        >
          返回登录
        </el-button>
      </div>
      <div style="display: flex; justify-content: center;">
        <el-form inline>
          <el-form-item label="填写申请手机号">
            <el-input 
              v-model="phone" 
              placeholder="请输入申请时填写的手机号" 
              style="width:260px"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchStatus">查询进度</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="applyInfo" style="margin-top:30px; display: flex; justify-content: center;">
        <div style="text-align: left;">
          <p>店铺名称：{{applyInfo.shopName}}</p>
          <p>联系人：{{applyInfo.contactPerson}}</p>
          <p>提交时间：{{applyInfo.appliedAt}}</p>
          <p>审核意见：{{applyInfo.reviewComment || '暂无'}}</p>
          <div style="margin:15px 0; display: flex; justify-content: center;">
            <el-tag v-if="applyInfo.status===0" type="warning" size="large">🔸 待管理员审核，请耐心等待</el-tag>
            <el-tag v-else-if="applyInfo.status===1" type="success" size="large">✅ 审核已通过
            <span style="margin-left:10px">登录账号：{{applyInfo.merchantUsername}}，初始密码：123456</span>
            </el-tag>
            <el-tag v-else-if="applyInfo.status===2" type="danger" size="large">❌ 申请已被驳回</el-tag>
          </div>
        </div>
      </div>

      <div v-else-if="searched" style="margin-top:30px; color:#999; text-align: center;">
      暂无该手机号对应的入驻申请记录，请确认手机号填写正确
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'
const phone = ref('')
const applyInfo = ref(null)
const searched = ref(false)

const searchStatus = async ()=>{
  if(!phone.value){
    return ElMessage.warning('请输入手机号')
  }
  searched.value = true
  try {
    const res = await http.get('/api/merchant/apply/check',{params:{phone:phone.value}})
    if (res.code === 200) {
      applyInfo.value = res.data
      if (!res.data) {
        ElMessage.info('未找到相关申请记录')
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (err) {
    ElMessage.error('网络异常，请稍后重试')
    applyInfo.value = null
  }
}
</script>