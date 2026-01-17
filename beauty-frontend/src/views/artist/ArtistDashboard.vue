<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pendingOrders }}</span>
          <span class="stat-label">待确认预约</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayOrders }}</span>
          <span class="stat-label">今日预约</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💄</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.serviceCount }}</span>
          <span class="stat-label">服务套餐</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.newReviews }}</span>
          <span class="stat-label">新评价</span>
        </div>
      </div>
    </div>

    <div class="section">
      <h3>待确认的预约</h3>
      <div class="order-list" v-if="pendingOrders.length > 0">
        <div v-for="order in pendingOrders" :key="order.id" class="order-item">
          <div class="order-info">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-service">{{ order.serviceName || '服务' }}</span>
          </div>
          <div class="order-time">{{ formatDate(order.appointmentDate) }} {{ order.appointmentTime }}</div>
          <div class="order-actions">
            <!-- 使用内联确认代替原生对话框 -->
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
          </div>
        </div>
      </div>
      <div v-else class="empty-state">暂无待确认的预约</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { artistPortalApi } from '../../api'

const stats = ref({
  pendingOrders: 0,
  todayOrders: 0,
  serviceCount: 0,
  newReviews: 0
})

const pendingOrders = ref([])
const processing = ref(false)
const confirmingOrderId = ref(null)
const rejectingOrderId = ref(null)
const rejectReason = ref('')

const fetchData = async () => {
  try {
    // 获取待确认预约
    const ordersData = await artistPortalApi.getAppointments({ status: 1, pageSize: 5 })
    if (ordersData && ordersData.records) {
      pendingOrders.value = ordersData.records
      stats.value.pendingOrders = ordersData.total
    }
    
    // 获取服务数量
    const services = await artistPortalApi.getServices()
    if (services) {
      stats.value.serviceCount = services.length
    }
    
    // 获取评价数量
    const reviews = await artistPortalApi.getReviews({ pageSize: 1 })
    if (reviews) {
      stats.value.newReviews = reviews.total
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  if (date.includes('T')) date = date.split('T')[0]
  return date
}

// 开始确认流程（显示内联确认UI）
const startConfirm = (order) => {
  cancelAction()
  confirmingOrderId.value = order.id
}

// 开始拒绝流程（显示内联输入UI）
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
    fetchData()
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
    fetchData()
  } catch (error) {
    console.error('拒绝失败:', error.message || error)
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 24px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-icon { font-size: 32px; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 600; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }

.section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.section h3 {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 16px 0;
  color: #333;
}

.order-list { display: flex; flex-direction: column; gap: 12px; }

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.order-info { display: flex; flex-direction: column; gap: 4px; }
.order-no { font-size: 13px; color: #666; }
.order-service { font-size: 14px; color: #333; font-weight: 500; }
.order-time { font-size: 13px; color: #999; }

.order-actions { display: flex; gap: 8px; }

.btn-confirm, .btn-reject {
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}

.btn-confirm { background: #2e7d32; color: #fff; }
.btn-confirm:hover { background: #1b5e20; }
.btn-confirm:disabled { background: #a5d6a7; cursor: not-allowed; }
.btn-reject { background: #fff; color: #e74c3c; border: 1px solid #e74c3c; }
.btn-reject:hover { background: #ffebee; }
.btn-reject:disabled { opacity: 0.5; cursor: not-allowed; }

/* 内联确认UI样式 */
.confirm-text { font-size: 12px; color: #e65100; font-weight: 500; }
.btn-yes, .btn-no { padding: 4px 12px; font-size: 11px; border-radius: 4px; cursor: pointer; border: none; }
.btn-yes { background: #2e7d32; color: #fff; }
.btn-yes:hover { background: #1b5e20; }
.btn-yes:disabled { background: #a5d6a7; cursor: not-allowed; }
.btn-no { background: #f5f5f5; color: #666; border: 1px solid #ddd; }
.btn-no:hover { background: #eee; }
.reject-input { padding: 4px 8px; font-size: 11px; border: 1px solid #ddd; border-radius: 4px; width: 100px; }
.order-actions { display: flex; gap: 8px; align-items: center; }

.empty-state { text-align: center; padding: 40px; color: #999; }

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
