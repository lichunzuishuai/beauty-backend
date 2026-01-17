<template>
  <div class="page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">我的预约</h1>
        <p class="page-subtitle">查看和管理您的预约订单</p>
      </div>
    </div>

    <div class="container">
      <!-- Tabs -->
      <div class="tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-btn"
          :class="{ active: currentTab === tab.value }"
          @click="currentTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Appointments List -->
      <div v-if="!loading" class="appointments-list">
        <div v-if="filteredAppointments.length > 0">
          <div v-for="appointment in filteredAppointments" :key="appointment.id" class="appointment-card">
            <div class="appointment-header">
              <span class="order-no">订单号：{{ appointment.orderNo }}</span>
              <span class="status-badge" :class="getStatusClass(appointment.status)">
                {{ getStatusText(appointment.status) }}
              </span>
            </div>
            <div class="appointment-body">
              <div class="artist-info">
                <img :src="appointment.artistAvatar || defaultAvatar" class="artist-avatar" />
                <div class="artist-detail">
                  <h3 class="artist-name">{{ appointment.artistName }}</h3>
                  <p class="service-name">{{ appointment.serviceName }}</p>
                </div>
              </div>
              <div class="appointment-info">
                <div class="info-row">
                  <span class="info-label">预约时间</span>
                  <span class="info-value">{{ formatAppointmentTime(appointment) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">服务金额</span>
                  <span class="info-value price">¥{{ appointment.totalAmount }}</span>
                </div>
              </div>
            </div>
            <div class="appointment-actions">
              <button v-if="appointment.status === 0" class="btn-solid btn-sm" @click="handlePay(appointment)">
                去支付
              </button>
              <button v-if="canCancel(appointment.status)" class="btn-ghost btn-sm" @click="handleCancel(appointment)">
                取消预约
              </button>
              <button v-if="appointment.status === 4" class="btn-ghost btn-sm" @click="handleReview(appointment)">
                去评价
              </button>
              <button class="btn-ghost btn-sm" @click="handleDetail(appointment)">
                查看详情
              </button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无{{ tabs.find(t => t.value === currentTab)?.label }}订单</p>
          <router-link to="/artists" class="btn-ghost">去预约</router-link>
        </div>
      </div>

      <div v-else class="loading">
        <div class="loading-spinner"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { appointmentApi } from '../api'

const router = useRouter()
const defaultAvatar = 'https://via.placeholder.com/100?text=Avatar'

const loading = ref(true)
const currentTab = ref('all')
const appointments = ref([])

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: '0' },
  { label: '待确认', value: '1' },
  { label: '已确认', value: '2' },
  { label: '已完成', value: '4' },
  { label: '已取消', value: '5' }
]

// Mock data
const mockAppointments = [
  { id: 1, orderNo: 'AP202601150001', artistName: '李梦琪', artistAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=100', serviceName: '新娘跟妆', appointmentDate: '2026-02-14', appointmentTime: '08:00', totalAmount: 2999, status: 2 },
  { id: 2, orderNo: 'AP202601140002', artistName: '王艺涵', artistAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100', serviceName: '单次化妆', appointmentDate: '2026-01-20', appointmentTime: '14:00', totalAmount: 599, status: 4 },
  { id: 3, orderNo: 'AP202601130003', artistName: '张思雨', artistAvatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=100', serviceName: '晚宴妆', appointmentDate: '2026-01-18', appointmentTime: '18:00', totalAmount: 899, status: 0 }
]

const filteredAppointments = computed(() => {
  if (currentTab.value === 'all') {
    return appointments.value
  }
  return appointments.value.filter(a => a.status === parseInt(currentTab.value))
})

const fetchAppointments = async () => {
  try {
    loading.value = true
    console.log('正在获取预约列表...')
    const data = await appointmentApi.getMyList({ current: 1, pageSize: 20 })
    console.log('预约列表返回数据:', data)
    if (data && data.records && data.records.length > 0) {
      appointments.value = data.records
    } else if (data && data.records) {
      // 有数据结构但没有记录，显示空列表而不是mock数据
      appointments.value = []
    } else {
      console.log('使用mock数据')
      appointments.value = mockAppointments
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    appointments.value = mockAppointments
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待确认', 2: '已确认', 3: '已拒绝', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'pending', 2: 'confirmed', 3: 'rejected', 4: 'completed', 5: 'cancelled' }
  return map[status] || ''
}

const canCancel = (status) => [0, 1, 2].includes(status)

const formatAppointmentTime = (appointment) => {
  let date = appointment.appointmentDate || ''
  let time = appointment.appointmentTime || ''
  
  // 处理 ISO 格式的日期
  if (date && date.includes('T')) {
    date = date.split('T')[0]
  }
  // 格式化日期显示
  if (date) {
    const parts = date.split('-')
    if (parts.length === 3) {
      date = `${parts[0]}年${parts[1]}月${parts[2]}日`
    }
  }
  return `${date} ${time}`
}

const handlePay = (appointment) => {
  // 跳转到支付页面，带上订单ID
  router.push(`/payment/${appointment.id}`)
}

const handleCancel = async (appointment) => {
  if (!confirm('确定要取消此预约吗？')) return
  
  try {
    await appointmentApi.cancel(appointment.id, { reason: '用户主动取消' })
    alert('取消成功')
    fetchAppointments()
  } catch (error) {
    console.error('取消失败:', error)
    alert('取消失败: ' + (error.message || '请稍后再试'))
  }
}

const handleReview = (appointment) => {
  router.push(`/review/${appointment.id}`)
}

const handleDetail = (appointment) => {
  router.push(`/appointment/${appointment.id}`)
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.page {
  padding-top: 100px;
  padding-bottom: var(--spacing-3xl);
  min-height: 100vh;
}

.page-header {
  background: var(--color-secondary-bg);
  padding: var(--spacing-xl) 0;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: var(--font-size-2xl);
  letter-spacing: 4px;
  margin-bottom: var(--spacing-xs);
}

.page-subtitle {
  color: var(--color-text-light);
  letter-spacing: 1px;
}

/* Tabs */
.tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--spacing-sm);
}

.tab-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  letter-spacing: 1px;
  transition: all var(--transition-fast);
}

.tab-btn:hover,
.tab-btn.active {
  color: var(--color-primary);
}

.tab-btn.active {
  border-bottom: 2px solid var(--color-accent);
}

/* Appointment Card */
.appointment-card {
  border: 1px solid var(--color-border);
  margin-bottom: var(--spacing-lg);
  transition: all var(--transition-base);
}

.appointment-card:hover {
  box-shadow: var(--shadow-sm);
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-secondary-bg);
  border-bottom: 1px solid var(--color-border);
}

.order-no {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.status-badge {
  padding: 4px 12px;
  font-size: var(--font-size-xs);
  border-radius: 2px;
}

.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.confirmed { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.cancelled { background: #fafafa; color: #757575; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.appointment-body {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-lg);
}

.artist-info {
  display: flex;
  gap: var(--spacing-md);
}

.artist-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.artist-name {
  font-size: var(--font-size-base);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.service-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.appointment-info {
  text-align: right;
}

.info-row {
  margin-bottom: var(--spacing-xs);
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
  margin-right: var(--spacing-sm);
}

.info-value {
  font-size: var(--font-size-sm);
}

.info-value.price {
  color: var(--color-accent);
  font-size: var(--font-size-lg);
  font-weight: 500;
}

.appointment-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.btn-sm {
  padding: 8px 20px;
  font-size: var(--font-size-xs);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--color-text-light);
}

.empty-state .btn-ghost {
  margin-top: var(--spacing-md);
}
</style>
