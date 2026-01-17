<template>
  <div class="page">
    <div class="container">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
      </div>

      <div v-else-if="appointment" class="detail-card">
        <div class="detail-header">
          <h1>预约详情</h1>
          <span class="status-badge" :class="getStatusClass(appointment.status)">
            {{ getStatusText(appointment.status) }}
          </span>
        </div>

        <div class="detail-section">
          <h3>订单信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号</span>
              <span class="value">{{ appointment.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatDate(appointment.createTime) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>服务信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">化妆师</span>
              <span class="value">{{ appointment.artistName }}</span>
            </div>
            <div class="info-item">
              <span class="label">服务项目</span>
              <span class="value">{{ appointment.serviceName }}</span>
            </div>
            <div class="info-item">
              <span class="label">预约日期</span>
              <span class="value">{{ formatAppointmentDate(appointment.appointmentDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">预约时间</span>
              <span class="value">{{ appointment.appointmentTime }}</span>
            </div>
            <div class="info-item" v-if="appointment.address">
              <span class="label">服务地址</span>
              <span class="value">{{ appointment.address }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>费用明细</h3>
          <div class="price-info">
            <span class="price-label">服务金额</span>
            <span class="price-value">¥{{ appointment.totalAmount }}</span>
          </div>
        </div>

        <div class="detail-actions">
          <button v-if="appointment.status === 0" class="btn-solid" @click="handlePay">
            去支付
          </button>
          <button v-if="canCancel" class="btn-ghost" @click="handleCancel">
            取消预约
          </button>
          <button class="btn-ghost" @click="goBack">返回</button>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>预约不存在</p>
        <router-link to="/my/appointments" class="btn-ghost">返回我的预约</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { appointmentApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const appointment = ref(null)
const appointmentId = route.params.id

const canCancel = computed(() => {
  if (!appointment.value) return false
  return [0, 1, 2].includes(appointment.value.status)
})

const fetchAppointment = async () => {
  try {
    loading.value = true
    const data = await appointmentApi.getDetail(appointmentId)
    appointment.value = data
  } catch (error) {
    console.error('获取预约详情失败:', error)
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

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatAppointmentDate = (date) => {
  if (!date) return '-'
  if (date.includes('T')) {
    date = date.split('T')[0]
  }
  const parts = date.split('-')
  if (parts.length === 3) {
    return `${parts[0]}年${parts[1]}月${parts[2]}日`
  }
  return date
}

const handlePay = () => {
  router.push(`/payment/${appointmentId}`)
}

const handleCancel = async () => {
  if (!confirm('确定要取消此预约吗？')) return
  
  try {
    await appointmentApi.cancel(appointmentId, { reason: '用户主动取消' })
    alert('取消成功')
    fetchAppointment()
  } catch (error) {
    console.error('取消失败:', error)
    alert('取消失败: ' + (error.message || '请稍后再试'))
  }
}

const goBack = () => {
  router.push('/my/appointments')
}

onMounted(() => {
  fetchAppointment()
})
</script>

<style scoped>
.page {
  padding-top: 120px;
  padding-bottom: 60px;
  min-height: 100vh;
}

.detail-card {
  max-width: 700px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #eee;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #eee;
}

.detail-header h1 {
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 2px;
}

.status-badge {
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 4px;
}

.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.confirmed { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.cancelled { background: #fafafa; color: #757575; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.detail-section {
  padding: 24px;
  border-bottom: 1px solid #eee;
}

.detail-section h3 {
  font-size: 14px;
  color: #999;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #999;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price-value {
  font-size: 24px;
  color: #C1B0A1;
  font-weight: 500;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
</style>
