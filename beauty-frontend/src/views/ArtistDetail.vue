<template>
  <div class="detail-page">
    <div class="container">
      <div v-if="!loading && artist" class="artist-detail">
        <!-- Artist Header -->
        <div class="artist-header">
          <div class="artist-avatar">
            <img :src="artist.avatar || defaultAvatar" :alt="artist.realName" />
          </div>
          <div class="artist-info">
            <h1 class="artist-name">{{ artist.realName }}</h1>
            <div class="artist-specialties" v-if="specialtiesList.length > 0">
              <span class="specialty-tag" v-for="(s, i) in specialtiesList" :key="i">{{ s }}</span>
            </div>
            <div class="artist-meta">
              <span class="artist-rating">★ {{ parseFloat(artist.rating || 5).toFixed(1) }}</span>
              <span class="artist-exp">{{ artist.experienceYears || 0 }}年经验</span>
              <span v-if="artist.isRecommend === 1" class="recommend-tag">推荐化妆师</span>
            </div>
            <p class="artist-intro">{{ artist.description || artist.introduction || '专业化妆师，为您提供优质服务' }}</p>
            <div class="artist-actions">
              <button class="btn-solid" @click="openBookingModal">立即预约</button>
              <button 
                class="btn-ghost" 
                :class="{ 'is-favorited': isFavorited }"
                :disabled="favoriteLoading"
                @click="toggleFavorite"
              >
                <span v-if="favoriteLoading">处理中...</span>
                <span v-else>{{ isFavorited ? '❤ 已收藏' : '♡ 收藏' }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Services -->
        <section class="section">
          <h2 class="section-title">服务项目</h2>
          <div class="service-list">
            <div 
              v-for="service in services" 
              :key="service.id" 
              class="service-card"
              :class="{ selected: selectedService?.id === service.id }"
              @click="selectService(service)"
            >
              <div class="service-image">
                <img :src="service.coverImage || 'https://via.placeholder.com/300x200'" alt="" />
                <div v-if="selectedService?.id === service.id" class="selected-badge">已选</div>
              </div>
              <div class="service-content">
                <h3 class="service-name">{{ service.name }}</h3>
                <p class="service-desc">{{ service.description }}</p>
                <div class="service-footer">
                  <span class="service-price">¥{{ service.price }}</span>
                  <span class="service-duration">{{ service.duration }}分钟</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Reviews -->
        <section class="section">
          <h2 class="section-title">用户评价</h2>
          <div v-if="reviews.length > 0" class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <img :src="review.userAvatar || defaultAvatar" class="review-avatar" />
                <div class="review-user">
                  <span class="review-name">{{ review.username }}</span>
                  <span class="review-rating">★ {{ review.rating }}</span>
                </div>
                <span class="review-time">{{ formatDate(review.createTime) }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <div v-else class="empty-state">
            <p>暂无评价</p>
          </div>
        </section>
      </div>

      <div v-else-if="loading" class="loading">
        <div class="loading-spinner"></div>
      </div>

      <div v-else class="empty-state">
        <p>化妆师不存在</p>
      </div>
    </div>

    <!-- 预约弹窗 -->
    <div v-if="showBookingModal" class="modal-overlay" @click.self="closeBookingModal">
      <div class="modal booking-modal">
        <div class="modal-header">
          <h3>预约化妆师</h3>
          <button class="close-btn" @click="closeBookingModal">×</button>
        </div>
        <div class="modal-body">
          <!-- 选择的服务 -->
          <div class="booking-section">
            <label class="booking-label">选择服务 <span class="required">*</span></label>
            <div class="service-select">
              <div 
                v-for="service in services" 
                :key="service.id"
                class="service-option"
                :class="{ selected: bookingForm.serviceId === service.id }"
                @click="bookingForm.serviceId = service.id"
              >
                <div class="service-option-info">
                  <span class="service-option-name">{{ service.name }}</span>
                  <span class="service-option-price">¥{{ service.price }}</span>
                </div>
                <span class="service-option-duration">{{ service.duration }}分钟</span>
              </div>
            </div>
          </div>

          <!-- 预约日期 -->
          <div class="form-group">
            <label class="form-label">预约日期 <span class="required">*</span></label>
            <input 
              v-model="bookingForm.appointmentDate" 
              type="date" 
              class="form-input"
              :min="minDate"
            />
          </div>

          <!-- 预约时间 -->
          <div class="form-group">
            <label class="form-label">预约时间 <span class="required">*</span></label>
            <select v-model="bookingForm.appointmentTime" class="form-input">
              <option value="">请选择时间</option>
              <option v-for="time in timeSlots" :key="time" :value="time">{{ time }}</option>
            </select>
          </div>

          <!-- 服务地址 -->
          <div class="form-group">
            <label class="form-label">服务地址</label>
            <input 
              v-model="bookingForm.address" 
              type="text" 
              class="form-input"
              placeholder="请输入服务地址"
            />
          </div>

          <!-- 联系人 -->
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">联系人姓名</label>
              <input 
                v-model="bookingForm.contactName" 
                type="text" 
                class="form-input"
                placeholder="请输入联系人姓名"
              />
            </div>
            <div class="form-group">
              <label class="form-label">联系电话</label>
              <input 
                v-model="bookingForm.contactPhone" 
                type="tel" 
                class="form-input"
                placeholder="请输入联系电话"
              />
            </div>
          </div>

          <!-- 备注 -->
          <div class="form-group">
            <label class="form-label">备注</label>
            <textarea 
              v-model="bookingForm.remark" 
              class="form-input"
              rows="3"
              placeholder="请输入备注信息（可选）"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-ghost" @click="closeBookingModal">取消</button>
          <button class="btn-solid" @click="submitBooking" :disabled="bookingLoading">
            {{ bookingLoading ? '提交中...' : '确认预约' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { artistApi, reviewApi, favoriteApi, appointmentApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://via.placeholder.com/200?text=Avatar'
const loading = ref(true)
const artist = ref(null)
const services = ref([])
const reviews = ref([])
const isFavorited = ref(false)
const favoriteLoading = ref(false)
const selectedService = ref(null)

// 预约相关
const showBookingModal = ref(false)
const bookingLoading = ref(false)
const bookingForm = reactive({
  serviceId: null,
  appointmentDate: '',
  appointmentTime: '',
  address: '',
  contactName: '',
  contactPhone: '',
  remark: ''
})

const artistId = route.params.id

// 计算最小可预约日期（明天）
const minDate = computed(() => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  return tomorrow.toISOString().split('T')[0]
})

// 时间段选项
const timeSlots = [
  '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
  '13:00', '13:30', '14:00', '14:30', '15:00', '15:30',
  '16:00', '16:30', '17:00', '17:30', '18:00'
]

// Mock data for demo
const mockArtist = {
  id: artistId,
  realName: '李梦琪',
  specialty: '新娘妆',
  rating: 4.9,
  experienceYears: 8,
  avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=400',
  introduction: '专注新娘妆容设计8年，打造您人生中最美的一天。擅长各类新娘妆、晚宴妆和日常妆容，细致入微，追求完美。'
}

const mockServices = [
  { id: 1, name: '新娘跟妆', description: '全天跟妆服务，包含早妆、仪式妆、敬酒妆', price: 2999, duration: 480, coverImage: 'https://images.unsplash.com/photo-1596704017254-9b121068fb31?w=300' },
  { id: 2, name: '单次化妆', description: '单次精致妆容，适合宴会、拍摄等场合', price: 599, duration: 90, coverImage: 'https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?w=300' },
  { id: 3, name: '化妆教学', description: '一对一化妆技巧教学，让您学会日常妆容', price: 399, duration: 120, coverImage: 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=300' }
]

const mockReviews = [
  { id: 1, username: '王小姐', userAvatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100', rating: 5, content: '化妆师非常专业，妆容持久一整天都没有脱妆，非常满意！', createTime: '2026-01-10' },
  { id: 2, username: '张女士', userAvatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100', rating: 5, content: '手法很温柔，妆容很自然，完全符合我想要的效果。', createTime: '2026-01-05' }
]

const fetchArtistDetail = async () => {
  try {
    loading.value = true
    const data = await artistApi.getDetail(artistId)
    if (data) {
      artist.value = data
    } else {
      artist.value = mockArtist
    }
    
    // 获取该化妆师的真实服务套餐
    try {
      const serviceData = await artistApi.getServices(artistId)
      if (serviceData && serviceData.length > 0) {
        services.value = serviceData
      } else {
        services.value = mockServices
      }
    } catch (e) {
      console.error('获取服务套餐失败:', e)
      services.value = mockServices
    }
    
    // 获取真实评价
    try {
      const reviewData = await reviewApi.getArtistReviews(artistId)
      if (reviewData && reviewData.records && reviewData.records.length > 0) {
        reviews.value = reviewData.records
      } else {
        reviews.value = mockReviews
      }
    } catch (e) {
      reviews.value = mockReviews
    }
  } catch (error) {
    console.error('获取化妆师详情失败:', error)
    artist.value = mockArtist
    services.value = mockServices
    reviews.value = mockReviews
  } finally {
    loading.value = false
  }
}

const checkFavorite = async () => {
  // 确保用户已登录且有有效 Token
  if (!userStore.isLoggedIn || !userStore.user?.token) return
  try {
    const result = await favoriteApi.check(1, artistId)
    isFavorited.value = !!result
  } catch (error) {
    // 如果是未登录错误，静默处理不做任何操作
    console.error('检查收藏状态失败:', error)
  }
}

// 获取擅长领域（计算属性）
const specialtiesList = computed(() => {
  try {
    if (!artist.value) return []
    const val = artist.value.specialties || artist.value.specialty
    if (!val) return []
    // 如果是数组直接返回
    if (Array.isArray(val)) {
      return val.filter(s => s && String(s).trim())
    }
    // 转为字符串处理
    const strVal = String(val).trim()
    if (!strVal || strVal === '[]' || strVal === '[""]' || strVal === 'null') return []
    // 如果是JSON数组格式的字符串
    if (strVal.startsWith('[')) {
      try {
        const parsed = JSON.parse(strVal)
        return Array.isArray(parsed) ? parsed.filter(s => s && String(s).trim()) : []
      } catch {
        return []
      }
    }
    // 逗号分隔的字符串
    return strVal.split(',').map(s => s.trim()).filter(s => s)
  } catch (e) {
    console.error('解析擅长领域失败:', e)
    return []
  }
})

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    favoriteLoading.value = true
    if (isFavorited.value) {
      await favoriteApi.removeArtist(artistId)
      isFavorited.value = false
      alert('已取消收藏')
    } else {
      await favoriteApi.addArtist(artistId)
      isFavorited.value = true
      alert('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    alert('操作失败: ' + (error.message || '请稍后再试'))
  } finally {
    favoriteLoading.value = false
  }
}

const selectService = (service) => {
  selectedService.value = service
  bookingForm.serviceId = service.id
}

const openBookingModal = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  // 预填联系信息
  if (userStore.user) {
    bookingForm.contactName = userStore.user.nickname || userStore.user.username || ''
    bookingForm.contactPhone = userStore.user.phone || ''
  }
  
  // 如果已选择服务，预填
  if (selectedService.value) {
    bookingForm.serviceId = selectedService.value.id
  } else if (services.value.length > 0) {
    bookingForm.serviceId = services.value[0].id
  }
  
  showBookingModal.value = true
}

const closeBookingModal = () => {
  showBookingModal.value = false
}

const submitBooking = async () => {
  // 表单验证
  if (!bookingForm.serviceId) {
    alert('请选择服务项目')
    return
  }
  if (!bookingForm.appointmentDate) {
    alert('请选择预约日期')
    return
  }
  if (!bookingForm.appointmentTime) {
    alert('请选择预约时间')
    return
  }

  try {
    bookingLoading.value = true
    
    const requestData = {
      artistId: parseInt(artistId),
      serviceId: bookingForm.serviceId,
      appointmentDate: bookingForm.appointmentDate,
      appointmentTime: bookingForm.appointmentTime,
      address: bookingForm.address,
      contactName: bookingForm.contactName,
      contactPhone: bookingForm.contactPhone,
      remark: bookingForm.remark
    }
    
    const result = await appointmentApi.create(requestData)
    
    if (result) {
      alert('预约成功！请前往"我的预约"查看详情')
      closeBookingModal()
      router.push('/my/appointments')
    }
  } catch (error) {
    console.error('预约失败:', error)
    alert('预约失败: ' + (error.message || '请稍后再试'))
  } finally {
    bookingLoading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchArtistDetail()
  checkFavorite()
})
</script>

<style scoped>
.detail-page {
  padding-top: 100px;
  padding-bottom: var(--spacing-3xl);
}

/* Artist Header */
.artist-header {
  display: flex;
  gap: var(--spacing-xl);
  padding: var(--spacing-xl);
  background: var(--color-secondary-bg);
  margin-bottom: var(--spacing-xl);
}

.artist-avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.artist-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.artist-info {
  flex: 1;
}

.artist-name {
  font-size: var(--font-size-2xl);
  letter-spacing: 4px;
  margin-bottom: var(--spacing-xs);
}

.artist-specialty {
  font-size: var(--font-size-lg);
  color: var(--color-accent);
  margin-bottom: var(--spacing-sm);
}

.artist-specialties {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: var(--spacing-sm);
}

.specialty-tag {
  background: linear-gradient(135deg, #f5f2ef 0%, #e8e2dc 100%);
  color: #5d4e41;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  border: 1px solid #d4c9be;
}

.artist-meta {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  align-items: center;
}

.artist-rating {
  color: var(--color-accent);
}

.recommend-tag {
  background: linear-gradient(135deg, #f5a623 0%, #e67e22 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.artist-intro {
  color: var(--color-text-light);
  line-height: 1.8;
  margin-bottom: var(--spacing-lg);
}

.artist-actions {
  display: flex;
  gap: var(--spacing-md);
}

.btn-ghost.is-favorited {
  color: #e74c3c;
  border-color: #e74c3c;
}

/* Section */
.section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-xl);
  letter-spacing: 3px;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

/* Services */
.service-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
}

.service-card {
  border: 1px solid var(--color-border);
  transition: all var(--transition-base);
  cursor: pointer;
  position: relative;
}

.service-card:hover {
  box-shadow: var(--shadow-md);
}

.service-card.selected {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 2px rgba(193, 176, 161, 0.2);
}

.service-image {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--color-accent);
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.service-content {
  padding: var(--spacing-md);
}

.service-name {
  font-size: var(--font-size-base);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.service-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-md);
  line-height: 1.6;
}

.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-price {
  font-size: var(--font-size-lg);
  color: var(--color-accent);
  font-weight: 500;
}

.service-duration {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

/* Reviews */
.review-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.review-item {
  padding: var(--spacing-lg);
  background: var(--color-secondary-bg);
}

.review-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.review-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.review-user {
  flex: 1;
}

.review-name {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.review-rating {
  color: var(--color-accent);
  font-size: var(--font-size-sm);
}

.review-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
}

.review-content {
  font-size: var(--font-size-sm);
  line-height: 1.8;
  color: var(--color-text);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--color-text-light);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 8px;
  width: 560px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 2px;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

/* Booking Modal */
.booking-section {
  margin-bottom: 20px;
}

.booking-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.required {
  color: #e74c3c;
}

.service-select {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.service-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.service-option:hover {
  border-color: var(--color-accent);
}

.service-option.selected {
  border-color: var(--color-accent);
  background: #f9f7f5;
}

.service-option-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.service-option-name {
  font-size: 14px;
  font-weight: 500;
}

.service-option-price {
  color: var(--color-accent);
  font-weight: 500;
}

.service-option-duration {
  font-size: 12px;
  color: #999;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-accent);
}

textarea.form-input {
  resize: vertical;
  font-family: inherit;
}

@media (max-width: 768px) {
  .artist-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .service-list {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
