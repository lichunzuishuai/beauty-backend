<template>
  <div class="page">
    <div class="toolbar">
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: currentTab === tab.value }" @click="changeTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>服务项目</th>
            <th>预约时间</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.orderNo }}</td>
            <td>{{ order.serviceName || order.serviceId }}</td>
            <td>{{ formatDate(order.appointmentDate) }} {{ order.appointmentTime }}</td>
            <td class="price">¥{{ order.totalAmount }}</td>
            <td><span class="status-badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span></td>
            <td class="actions-cell">
              <template v-if="order.status === 1">
                <!-- 内联确认UI -->
                <template v-if="confirmingOrderId === order.id">
                  <span class="confirm-text">确定确认？</span>
                  <button class="btn-yes" @click="doConfirm(order)" :disabled="processing">是</button>
                  <button class="btn-no" @click="cancelAction">否</button>
                </template>
                <template v-else-if="rejectingOrderId === order.id">
                  <input v-model="rejectReason" class="reject-input" placeholder="拒绝原因" />
                  <button class="btn-yes" @click="doReject(order)" :disabled="processing || !rejectReason">确定</button>
                  <button class="btn-no" @click="cancelAction">取消</button>
                </template>
                <template v-else>
                  <button class="btn-confirm" @click="startConfirm(order)" :disabled="processing">确认</button>
                  <button class="btn-reject" @click="startReject(order)" :disabled="processing">拒绝</button>
                </template>
              </template>
              <span v-else class="no-action">-</span>
            </td>
          </tr>
          <tr v-if="orders.length === 0">
            <td colspan="6" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="total > pageSize">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { artistPortalApi } from '../../api'

const loading = ref(false)
const orders = ref([])
const currentTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 内联确认状态
const processing = ref(false)
const confirmingOrderId = ref(null)
const rejectingOrderId = ref(null)
const rejectReason = ref('')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待确认', value: '1' },
  { label: '已确认', value: '2' },
  { label: '已完成', value: '4' },
  { label: '已取消', value: '5' }
]

const fetchOrders = async () => {
  try {
    loading.value = true
    const params = { current: currentPage.value, pageSize: pageSize.value }
    if (currentTab.value !== 'all') {
      params.status = parseInt(currentTab.value)
    }
    const data = await artistPortalApi.getAppointments(params)
    if (data && data.records) {
      orders.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取预约失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  if (date.includes('T')) date = date.split('T')[0]
  return date
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待确认', 2: '已确认', 3: '已拒绝', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'pending', 2: 'confirmed', 3: 'rejected', 4: 'completed', 5: 'cancelled' }
  return map[status] || ''
}

const changeTab = (tab) => {
  currentTab.value = tab
  currentPage.value = 1
  fetchOrders()
}

const changePage = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 开始确认流程
const startConfirm = (order) => {
  cancelAction()
  confirmingOrderId.value = order.id
}

// 开始拒绝流程
const startReject = (order) => {
  cancelAction()
  rejectingOrderId.value = order.id
}

// 取消当前操作
const cancelAction = () => {
  confirmingOrderId.value = null
  rejectingOrderId.value = null
  rejectReason.value = ''
}

// 执行确认
const doConfirm = async (order) => {
  processing.value = true
  try {
    await artistPortalApi.confirmAppointment(order.id)
    cancelAction()
    fetchOrders()
  } catch (error) {
    console.error('确认失败:', error.message || error)
  } finally {
    processing.value = false
  }
}

// 执行拒绝
const doReject = async (order) => {
  processing.value = true
  try {
    await artistPortalApi.rejectAppointment(order.id, rejectReason.value)
    cancelAction()
    fetchOrders()
  } catch (error) {
    console.error('拒绝失败:', error.message || error)
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; justify-content: space-between; }
.filter-tabs { display: flex; gap: 8px; }
.filter-tabs button { padding: 8px 20px; border: 1px solid #ddd; background: #fff; font-size: 13px; border-radius: 4px; cursor: pointer; }
.filter-tabs button.active { background: #2c3e50; color: #fff; border-color: #2c3e50; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }
.loading-container { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 24px; height: 24px; border: 2px solid #ddd; border-top-color: #C1B0A1; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #fafafa; color: #666; font-weight: 500; }
.price { color: #e74c3c; font-weight: 500; }
.empty { text-align: center; color: #999; padding: 40px !important; }
.status-badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.confirmed { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.cancelled { background: #fafafa; color: #757575; }
.status-badge.rejected { background: #ffebee; color: #c62828; }
.btn-confirm, .btn-reject { padding: 4px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; margin-right: 8px; border: none; }
.btn-confirm { background: #2e7d32; color: #fff; }
.btn-confirm:disabled { background: #a5d6a7; cursor: not-allowed; }
.btn-reject { background: #fff; color: #e74c3c; border: 1px solid #e74c3c; }
.btn-reject:disabled { opacity: 0.5; cursor: not-allowed; }
.no-action { color: #999; }

/* 内联确认UI样式 */
.actions-cell { display: flex; align-items: center; gap: 6px; }
.confirm-text { font-size: 11px; color: #e65100; font-weight: 500; white-space: nowrap; }
.btn-yes, .btn-no { padding: 3px 10px; font-size: 11px; border-radius: 4px; cursor: pointer; border: none; }
.btn-yes { background: #2e7d32; color: #fff; }
.btn-yes:hover { background: #1b5e20; }
.btn-yes:disabled { background: #a5d6a7; cursor: not-allowed; }
.btn-no { background: #f5f5f5; color: #666; border: 1px solid #ddd; }
.btn-no:hover { background: #eee; }
.reject-input { padding: 3px 6px; font-size: 11px; border: 1px solid #ddd; border-radius: 4px; width: 80px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; padding: 16px; border-top: 1px solid #eee; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
