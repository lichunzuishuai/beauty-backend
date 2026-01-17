<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: currentTab === tab.value }" @click="changeTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
      <div class="search-box">
        <input v-model="searchKeyword" type="text" placeholder="搜索订单号" @keyup.enter="handleSearch" />
        <button @click="handleSearch">搜索</button>
      </div>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>化妆师</th>
            <th>服务</th>
            <th>预约时间</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.orderNo }}</td>
            <td>{{ order.username || order.userId }}</td>
            <td>{{ order.artistName || order.artistId }}</td>
            <td>{{ order.serviceName || order.serviceId }}</td>
            <td>{{ formatDate(order.appointmentDate) }} {{ order.appointmentTime }}</td>
            <td class="price">¥{{ order.totalAmount }}</td>
            <td><span class="status-badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span></td>
            <td>
              <button class="action-btn" @click="handleDetail(order)">详情</button>
              <button v-if="order.status === 1" class="action-btn success" @click="handleConfirm(order)">确认</button>
              <button v-if="order.status === 1" class="action-btn danger" @click="handleReject(order)">拒绝</button>
            </td>
          </tr>
          <tr v-if="orders.length === 0">
            <td colspan="8" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <span class="pagination-info">共 {{ total }} 条记录</span>
        <div class="pagination-controls">
          <button class="page-btn" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
            上一页
          </button>
          <span class="page-current">{{ currentPage }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>预约详情</h3>
          <button class="close-btn" @click="showDetailModal = false">×</button>
        </div>
        <div class="modal-body" v-if="currentOrder">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">订单号</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态</span>
              <span class="value">{{ getStatusText(currentOrder.status) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">用户</span>
              <span class="value">{{ currentOrder.username || currentOrder.userId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">化妆师</span>
              <span class="value">{{ currentOrder.artistName || currentOrder.artistId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">服务项目</span>
              <span class="value">{{ currentOrder.serviceName || currentOrder.serviceId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预约时间</span>
              <span class="value">{{ formatDate(currentOrder.appointmentDate) }} {{ currentOrder.appointmentTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">服务地址</span>
              <span class="value">{{ currentOrder.address || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单金额</span>
              <span class="value price">¥{{ currentOrder.totalAmount }}</span>
            </div>
            <div class="detail-item full-width" v-if="currentOrder.remark">
              <span class="label">备注</span>
              <span class="value">{{ currentOrder.remark }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-ghost" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const loading = ref(false)
const searchKeyword = ref('')
const currentTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref([])
const showDetailModal = ref(false)
const currentOrder = ref(null)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: '0' },
  { label: '待确认', value: '1' },
  { label: '已确认', value: '2' },
  { label: '已完成', value: '4' },
  { label: '已取消', value: '5' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchOrders = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (currentTab.value !== 'all') {
      params.status = parseInt(currentTab.value)
    }
    if (searchKeyword.value) {
      params.orderNo = searchKeyword.value
    }
    
    const data = await adminApi.getAppointmentList(params)
    if (data && data.records) {
      orders.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    alert('获取预约列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  if (date.includes('T')) {
    date = date.split('T')[0]
  }
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

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const handleDetail = async (order) => {
  try {
    const detail = await adminApi.getAppointmentDetail(order.id)
    currentOrder.value = detail || order
    showDetailModal.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    currentOrder.value = order
    showDetailModal.value = true
  }
}

const handleConfirm = async (order) => {
  // TODO: 实现确认API调用
  try {
    // await adminApi.confirmAppointment(order.id)
    order.status = 2
    console.log('已确认订单:', order.orderNo)
  } catch (error) {
    console.error('确认失败:', error)
  }
}

const handleReject = async (order) => {
  // TODO: 实现拒绝API调用（可以使用内联输入获取原因）
  try {
    // await adminApi.rejectAppointment(order.id, reason)
    order.status = 3
    console.log('已拒绝订单:', order.orderNo)
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.admin-page { display: flex; flex-direction: column; gap: 20px; }
.page-toolbar { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.filter-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.filter-tabs button { padding: 8px 20px; border: 1px solid #ddd; background: #fff; font-size: 13px; cursor: pointer; border-radius: 4px; }
.filter-tabs button.active { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.search-box { display: flex; gap: 8px; }
.search-box input { padding: 8px 16px; border: 1px solid #ddd; width: 200px; font-size: 13px; border-radius: 4px; }
.search-box button { padding: 8px 20px; background: #1a1a2e; color: #fff; font-size: 13px; border: none; border-radius: 4px; cursor: pointer; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }

.loading-container { display: flex; justify-content: center; align-items: center; gap: 12px; padding: 60px 0; color: #666; }
.loading-spinner { width: 24px; height: 24px; border: 2px solid #ddd; border-top-color: #C1B0A1; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th, .admin-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.admin-table th { background: #fafafa; color: #666; font-weight: 500; }
.price { color: #e74c3c; font-weight: 500; }
.empty-row { text-align: center !important; color: #999; padding: 40px 16px !important; }

.status-badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.confirmed { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.cancelled { background: #fafafa; color: #757575; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.action-btn { padding: 4px 12px; font-size: 12px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; margin-right: 8px; }
.action-btn:hover { background: #f5f5f5; }
.action-btn.success { color: #2e7d32; border-color: #2e7d32; }
.action-btn.success:hover { background: #e8f5e9; }
.action-btn.danger { color: #e74c3c; border-color: #e74c3c; }
.action-btn.danger:hover { background: #ffebee; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-top: 1px solid #eee; }
.pagination-info { font-size: 13px; color: #666; }
.pagination-controls { display: flex; align-items: center; gap: 12px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: #fff; font-size: 13px; border-radius: 4px; cursor: pointer; }
.page-btn:hover:not(:disabled) { background: #f5f5f5; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-current { font-size: 13px; color: #666; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: #fff; border-radius: 8px; width: 600px; max-width: 90vw; max-height: 90vh; overflow: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #eee; }
.modal-header h3 { margin: 0; font-size: 16px; font-weight: 500; }
.close-btn { width: 32px; height: 32px; border: none; background: transparent; font-size: 24px; color: #999; cursor: pointer; }
.close-btn:hover { color: #333; }
.modal-body { padding: 24px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid #eee; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item.full-width { grid-column: 1 / -1; }
.detail-item .label { font-size: 12px; color: #999; }
.detail-item .value { font-size: 14px; color: #333; }
.detail-item .value.price { color: #e74c3c; font-weight: 500; }

.btn-ghost { padding: 10px 24px; border: 1px solid #ddd; background: #fff; font-size: 13px; border-radius: 4px; cursor: pointer; }
.btn-ghost:hover { background: #f5f5f5; }
</style>
