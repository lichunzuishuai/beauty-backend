<template>
  <div class="page">
    <div class="container">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
      </div>

      <div v-else-if="paymentInfo" class="payment-card">
        <div class="payment-header">
          <h1>订单支付</h1>
        </div>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ paymentInfo.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">服务项目</span>
            <span class="value">{{ paymentInfo.serviceName }}</span>
          </div>
          <div class="info-row">
            <span class="label">化妆师</span>
            <span class="value">{{ paymentInfo.artistName }}</span>
          </div>
        </div>

        <div class="amount-section">
          <span class="amount-label">支付金额</span>
          <span class="amount-value">¥{{ paymentInfo.amount }}</span>
        </div>

        <div class="payment-methods">
          <h3>选择支付方式</h3>
          <div class="method-list">
            <div 
              class="method-item" 
              :class="{ active: paymentMethod === 'wechat' }"
              @click="paymentMethod = 'wechat'"
            >
              <span class="method-icon">💚</span>
              <span class="method-name">微信支付</span>
            </div>
            <div 
              class="method-item" 
              :class="{ active: paymentMethod === 'alipay' }"
              @click="paymentMethod = 'alipay'"
            >
              <span class="method-icon">💙</span>
              <span class="method-name">支付宝</span>
            </div>
          </div>
        </div>

        <div class="payment-actions">
          <button class="btn-solid btn-large" @click="handlePay" :disabled="paying">
            {{ paying ? '支付中...' : '立即支付' }}
          </button>
          <button class="btn-ghost" @click="goBack">返回</button>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>订单不存在或已支付</p>
        <router-link to="/my/appointments" class="btn-ghost">返回我的预约</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paymentApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const paying = ref(false)
const paymentInfo = ref(null)
const paymentMethod = ref('wechat')
const appointmentId = route.params.id

const fetchPaymentInfo = async () => {
  try {
    loading.value = true
    // 调用支付信息接口获取包含paymentNo的信息
    const info = await paymentApi.getInfo(appointmentId)
    if (info) {
      paymentInfo.value = info
    }
  } catch (error) {
    console.error('获取支付信息失败:', error)
    alert('获取支付信息失败: ' + (error.message || '请稍后再试'))
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  if (date.includes('T')) {
    date = date.split('T')[0]
  }
  return date.replace(/-/g, '/')
}

const handlePay = async () => {
  if (!paymentInfo.value || !paymentInfo.value.paymentNo) {
    alert('支付信息不完整，请刷新重试')
    return
  }
  
  try {
    paying.value = true
    
    // 调用支付接口，传入paymentNo
    const result = await paymentApi.pay({
      paymentNo: paymentInfo.value.paymentNo,
      paymentMethod: paymentMethod.value === 'wechat' ? 1 : 2
    })
    
    if (result) {
      alert('支付成功！')
      router.push('/my/appointments')
    }
  } catch (error) {
    console.error('支付失败:', error)
    alert('支付失败: ' + (error.message || '请稍后再试'))
  } finally {
    paying.value = false
  }
}

const goBack = () => {
  router.push('/my/appointments')
}

onMounted(() => {
  fetchPaymentInfo()
})
</script>

<style scoped>
.page {
  padding-top: 120px;
  padding-bottom: 60px;
  min-height: 100vh;
  background: #f9f9f9;
}

.payment-card {
  max-width: 500px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.payment-header {
  padding: 24px;
  border-bottom: 1px solid #eee;
  text-align: center;
}

.payment-header h1 {
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 2px;
}

.order-info {
  padding: 24px;
  border-bottom: 1px solid #eee;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-size: 13px;
  color: #999;
}

.info-row .value {
  font-size: 13px;
  color: #333;
}

.amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: #faf8f6;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  font-size: 32px;
  color: #C1B0A1;
  font-weight: 600;
}

.payment-methods {
  padding: 24px;
}

.payment-methods h3 {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.method-item:hover {
  border-color: #ddd;
}

.method-item.active {
  border-color: #C1B0A1;
  background: #faf8f6;
}

.method-icon {
  font-size: 24px;
}

.method-name {
  font-size: 14px;
  color: #333;
}

.payment-actions {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-large {
  padding: 16px 32px;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
</style>
