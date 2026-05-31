<template>
  <div class="app-container">
    <!-- 顶部标签页 -->
    <el-card class="mb-4">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待接单" name="2" />
        <el-tab-pane label="制作中" name="3" />
        <el-tab-pane label="待取餐" name="4" />
        <el-tab-pane label="已完成" name="5" />
        <el-tab-pane label="已取消" name="6" />
        <el-tab-pane label="全部订单" name="" />
      </el-tabs>
    </el-card>

    <!-- 查询区域：所有标签页都显示，支持按订单编号查询 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="订单编号">
          <el-input 
            v-model="queryForm.orderNo" 
            placeholder="输入订单号查询" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态" v-if="activeTab === ''">
          <!-- 全部订单时显示状态筛选 -->
          <el-select 
            v-model="queryForm.status" 
            placeholder="选择状态" 
            clearable
            style="width: 140px"
          >
            <el-option label="待支付" :value="0" />
            <el-option label="待接单" :value="2" />
            <el-option label="制作中" :value="3" />
            <el-option label="待取餐" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <!-- 全部订单时显示日期筛选 -->
        <el-form-item label="完成日期" v-if="activeTab === ''">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作按钮 -->
    <el-card class="mb-4" v-if="activeTab === '2' || activeTab === '3'">
      <el-button type="success" @click="batchAccept" v-if="activeTab === '2'">批量接单</el-button>
      <el-button type="warning" @click="batchOutMeal" v-if="activeTab === '3'">批量出餐</el-button>
      <span style="margin-left:15px;color:#666">已选中 {{ selectList.length }} 条订单</span>
    </el-card>

    <!-- 订单列表 -->
    <el-card>
      <el-table :data="orderList" border @selection-change="handleSelect">
        <el-table-column type="selection" width="60" />
        <el-table-column label="订单编号" prop="orderNo" width="160" />
        <el-table-column label="实付金额" prop="actualAmount" width="100">
          <template #default="{row}">
            <span style="color:#f56c6c;font-weight:bold">¥{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" effect="light" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="createdAt" width="180" />

        <!-- 已完成和全部订单显示完成时间 -->
        <el-table-column 
          label="完成时间" 
          width="180"
          v-if="showFinishedAtColumn"
        >
          <template #default="{row}">
            <span v-if="row.finishedAt" style="color: #67c23a;">{{ row.finishedAt }}</span>
            <span v-else style="color: #999;">--</span>
          </template>
        </el-table-column>

        <el-table-column label="取消原因" prop="cancelReason" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <div style="display:flex;gap:8px;justify-content:center">
              <el-tooltip content="查看详情" placement="top">
                <el-button circle :icon="View" size="small" @click="openDetail(row)" />
              </el-tooltip>

              <!-- 待支付：只能查看详情 -->
              <!-- 待接单：接单 -->
              <el-tooltip v-if="row.status === 2" content="确认接单" placement="top">
                <el-button circle :icon="Check" type="success" size="small" @click="accept(row)" />
              </el-tooltip>

              <!-- 制作中：出餐 -->
              <el-tooltip v-if="row.status === 3" content="标记出餐" placement="top">
                <el-button circle :icon="Food" type="warning" size="small" @click="outMeal(row)" />
              </el-tooltip>

              <!-- 待接单/制作中：取消 -->
              <el-tooltip v-if="row.status===2 || row.status===3" content="取消订单" placement="top">
                <el-button circle :icon="CircleClose" type="danger" size="small" @click="openCancel(row)" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="650px">
      <div v-if="orderInfo" style="padding:10px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单用户ID">{{ orderInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="订单总金额">
            <span style="color:#f56c6c;font-weight:bold">¥{{ orderInfo.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ orderInfo.discountAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实付金额" :span="2">
            <span style="color:#f56c6c;font-size:18px;font-weight:bold">¥{{ orderInfo.actualAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ orderInfo.payMethod || "未支付" }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ orderInfo.phone || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(orderInfo.status)">{{ getStatusText(orderInfo.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ orderInfo.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="出餐时间">{{ orderInfo.paidAt || "暂无" }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ orderInfo.finishedAt || "暂无" }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ orderInfo.cancelReason || "无" }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="500px">
      <el-form :model="cancelForm">
        <el-form-item label="取消原因">
          <el-select v-model="cancelForm.reason" placeholder="请选择" style="width:100%">
            <el-option label="食材不足" value="食材不足" />
            <el-option label="设备故障" value="设备故障" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" v-if="cancelForm.reason==='其他原因'">
          <el-input v-model="cancelForm.desc" type="textarea" :rows="3" placeholder="请填写具体原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible=false">取消</el-button>
        <el-button type="danger" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import { View, Check, Food, CircleClose, Search } from '@element-plus/icons-vue'
import http from '../../utils/request'

const activeTab = ref('2')
const orderList = ref([])
const selectList = ref([])
const queryForm = ref({
  orderNo:'',
  status:null,
  dateRange:[]
})

// 详情弹窗
const detailVisible = ref(false)
const orderInfo = ref(null)

// 取消弹窗
const cancelVisible = ref(false)
const cancelForm = ref({
  reason:'',
  desc:''
})
const nowOrderId = ref('')

// 计算属性：是否显示完成时间列
const showFinishedAtColumn = computed(() => {
  return activeTab.value === '5' || activeTab.value === ''
})

// 切换标签页时重置查询条件
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  // 切换标签时清空查询条件
  queryForm.value.orderNo = ''
  queryForm.value.status = null
  queryForm.value.dateRange = []
  loadOrderList()
}

// 查询按钮点击
const handleSearch = () => {
  loadOrderList()
}

// 加载订单（支持按订单编号、状态、日期查询）
const loadOrderList = async()=>{
  let params = {}

  // 如果有输入订单编号，按订单编号查询（模糊匹配）
  if(queryForm.value.orderNo && queryForm.value.orderNo.trim()){
    params.orderNo = queryForm.value.orderNo.trim()
  }

  if(activeTab.value){
    // 标签页模式：按状态筛选
    params.status = activeTab.value
  } else {
    // 全部订单模式：支持按状态筛选
    params.status = queryForm.value.status
  }

  // 全部订单时支持按完成日期筛选
  if(activeTab.value === '' && queryForm.value.dateRange && queryForm.value.dateRange.length === 2){
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  }

  const res = await http.get('/api/merchant/orders',{params})
  if(res.code===200){
    orderList.value = res.data
  }
}

// 重置查询
const resetQuery = ()=>{
  queryForm.value.orderNo = ''
  queryForm.value.status = null
  queryForm.value.dateRange = []
  // 重置后重新加载当前标签页数据
  loadOrderList()
}

// 多选
const handleSelect = (val)=>{
  selectList.value = val
}

// 状态文字（自助取餐流程：待支付/待接单/制作中/待取餐/已完成/已取消）
const getStatusText = (s)=>{
  const map={
    0:'待支付',
    2:'待接单',
    3:'制作中',
    4:'待取餐',
    5:'已完成',
    6:'已取消'
  }
  return map[s]||'未知'
}

// 状态标签颜色
const getStatusType = (s)=>{
  const map={
    0:'info',
    2:'primary',
    3:'warning',
    4:'',
    5:'success',
    6:'danger'
  }
  return map[s]||''
}

// 查看详情
const openDetail = (row)=>{
  orderInfo.value = row
  detailVisible.value = true
}

// 单个接单
const accept = async(row)=>{
  try{
    await http.put(`/api/merchant/orders/${row.id}/accept`)
    ElMessage.success('接单成功')
    loadOrderList()
  }catch(e){
    ElMessage.error('接单失败，请稍后重试')
  }
}

// 单个出餐
const outMeal = async(row)=>{
  await ElMessageBox.confirm('确认该订单已出餐吗？标记后将通知学生取餐','提示',{
    confirmButtonText:'确认出餐',
    cancelButtonText:'取消',
    type:'warning'
  })
  try{
    await http.put(`/api/merchant/orders/${row.id}/outmeal`)
    ElMessage.success('已标记出餐，等待学生取餐')
    loadOrderList()
  }catch(e){
    ElMessage.error('操作失败')
  }
}

// 批量接单
const batchAccept = async()=>{
  if(selectList.value.length===0){
    ElMessage.warning('请先勾选订单')
    return
  }
  const ids = selectList.value.map(item=>item.id)
  const res = await http.put('/api/merchant/orders/batch-accept',ids)
  ElMessage.success('批量接单成功')
  loadOrderList()
}

// 批量出餐
const batchOutMeal = async()=>{
  if(selectList.value.length===0){
    ElMessage.warning('请先勾选订单')
    return
  }
  await ElMessageBox.confirm('确认批量出餐吗？','提示',{
    confirmButtonText:'确认',
    cancelButtonText:'取消',
    type:'warning'
  })
  const ids = selectList.value.map(item=>item.id)
  await http.put('/api/merchant/orders/batch-outmeal',ids)
  ElMessage.success('批量出餐成功')
  loadOrderList()
}

// 打开取消弹窗
const openCancel = (row)=>{
  nowOrderId.value = row.id
  cancelForm.value.reason = ''
  cancelForm.value.desc = ''
  cancelVisible.value = true
}

// 提交取消
const submitCancel = async()=>{
  if(!cancelForm.value.reason){
    ElMessage.warning('请选择取消原因')
    return
  }
  let reason = cancelForm.value.reason
  if(cancelForm.value.reason === '其他原因'){
    reason = cancelForm.value.desc
    if(!reason){
      ElMessage.warning('请填写原因')
      return
    }
  }
  await http.put(`/api/merchant/orders/${nowOrderId.value}/cancel`,null,{
    params:{reason:reason}
  })
  ElMessage.success('订单取消成功，已发起退款')
  cancelVisible.value = false
  loadOrderList()
}

onMounted(()=>{
  loadOrderList()
})
</script>

<style scoped>
.app-container{
  padding:20px;
}
</style>
