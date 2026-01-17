<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <div class="filter-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          :class="{ active: currentTab === tab.value }"
          @click="changeTab(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.value === 0 && pendingCount > 0" class="badge">{{ pendingCount }}</span>
        </button>
      </div>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else-if="applications.length > 0" class="applications-list">
        <div v-for="app in applications" :key="app.id" class="application-card">
          <div class="app-header">
            <div class="app-header-left">
              <h3>{{ app.realName }}</h3>
              <span class="user-id">用户ID: {{ app.userId }}</span>
            </div>
            <span class="status-badge" :class="getStatusClass(app.status)">{{ getStatusText(app.status) }}</span>
          </div>
          <div class="app-body">
            <div class="info-grid">
              <div class="info-item">
                <label>手机号</label>
                <span class="phone-number">{{ app.phone }}</span>
              </div>
              <div class="info-item">
                <label>身份证号</label>
                <span>{{ app.idCard || '未填写' }}</span>
              </div>
              <div class="info-item">
                <label>擅长领域</label>
                <span>{{ app.specialties || '-' }}</span>
              </div>
              <div class="info-item">
                <label>从业年限</label>
                <span>{{ app.experienceYears || 0 }}年</span>
              </div>
              <div class="info-item">
                <label>申请时间</label>
                <span>{{ formatDate(app.createTime) }}</span>
              </div>
              <div class="info-item">
                <label>申请ID</label>
                <span>#{{ app.id }}</span>
              </div>
            </div>
            <div class="info-item full">
              <label>自我介绍</label>
              <p>{{ app.introduction || '无' }}</p>
            </div>
            
            <!-- 作品集图片 -->
            <div v-if="app.portfolioImages" class="info-item full">
              <label>作品集</label>
              <div class="portfolio-images">
                <img 
                  v-for="(img, idx) in parseImages(app.portfolioImages)" 
                  :key="idx" 
                  :src="img" 
                  @click="previewImage(img)"
                  class="portfolio-img"
                />
              </div>
            </div>
            
            <!-- 资质证书 -->
            <div v-if="app.certificateImages" class="info-item full">
              <label>资质证书</label>
              <div class="portfolio-images">
                <img 
                  v-for="(img, idx) in parseImages(app.certificateImages)" 
                  :key="idx" 
                  :src="img" 
                  @click="previewImage(img)"
                  class="portfolio-img"
                />
              </div>
            </div>

            <div v-if="app.status === 2 && app.rejectReason" class="info-item full reject-reason">
              <label>拒绝原因</label>
              <p>{{ app.rejectReason }}</p>
            </div>
          </div>
          <div v-if="app.status === 0" class="app-actions">
            <button 
              class="btn-approve" 
              @click.stop.prevent="handleApprove(app)"
            >
              通过
            </button>
            <button 
              class="btn-reject" 
              @click.stop.prevent="handleReject(app)"
            >
              拒绝
            </button>
          </div>
        </div>
      </div>
      <div v-else class="empty-msg">
        暂无{{ tabs.find(t => t.value === currentTab)?.label }}
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <span class="pagination-info">共 {{ total }} 条记录</span>
        <div class="pagination-controls">
          <button 
            class="page-btn" 
            :disabled="currentPage <= 1" 
            @click="changePage(currentPage - 1)"
          >
            上一页
          </button>
          <span class="page-current">{{ currentPage }} / {{ totalPages }}</span>
          <button 
            class="page-btn" 
            :disabled="currentPage >= totalPages" 
            @click="changePage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal">
        <div class="modal-header">
          <h3>拒绝申请 - {{ rejectingApp?.realName }}</h3>
          <button class="close-btn" @click="closeRejectModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">拒绝原因 <span class="required">*</span></label>
            <textarea 
              v-model="rejectReason" 
              class="form-input" 
              rows="4"
              placeholder="请输入拒绝原因"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeRejectModal">取消</button>
          <button class="btn-confirm-reject" @click="confirmReject" :disabled="actionLoading">
            {{ actionLoading ? '处理中...' : '确认拒绝' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 通过确认弹窗 -->
    <div v-if="showApproveModal" class="modal-overlay" @click.self="closeApproveModal">
      <div class="modal">
        <div class="modal-header">
          <h3>确认通过申请</h3>
          <button class="close-btn" @click="closeApproveModal">×</button>
        </div>
        <div class="modal-body">
          <div class="confirm-content">
            <p>确定要通过 <strong>{{ approvingApp?.realName }}</strong> 的入驻申请吗？</p>
            <p class="confirm-tip">通过后，该用户将成为平台认证化妆师。</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeApproveModal">取消</button>
          <button class="btn-confirm-approve" @click="confirmApprove" :disabled="actionLoading">
            {{ actionLoading ? '处理中...' : '确认通过' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

const loading = ref(false)
const actionLoading = ref(false)
const currentTab = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pendingCount = ref(0)

const tabs = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 }
]

const applications = ref([])

// 通过弹窗
const showApproveModal = ref(false)
const approvingApp = ref(null)

// 拒绝弹窗
const showRejectModal = ref(false)
const rejectReason = ref('')
const rejectingApp = ref(null)

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 解析 JSON 图片数组
const parseImages = (jsonStr) => {
  if (!jsonStr) return []
  try {
    const parsed = JSON.parse(jsonStr)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

// 预览图片
const previewImage = (url) => {
  window.open(url, '_blank')
}

// 获取申请列表
const fetchApplications = async () => {
  try {
    loading.value = true
    const params = {
      status: currentTab.value,
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    const data = await adminApi.getApplicationList(params)
    if (data && data.records) {
      applications.value = data.records
      total.value = data.total
    } else {
      applications.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取申请列表失败:', error)
    applications.value = []
    alert('获取申请列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 获取待审核数量
const fetchPendingCount = async () => {
  try {
    const data = await adminApi.getApplicationList({ status: 0, current: 1, pageSize: 1 })
    if (data) {
      pendingCount.value = data.total || 0
    }
  } catch (error) {
    console.error('获取待审核数量失败:', error)
  }
}

// 切换标签
const changeTab = (tab) => {
  currentTab.value = tab
  currentPage.value = 1
  fetchApplications()
}

// 分页
const changePage = (page) => {
  currentPage.value = page
  fetchApplications()
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'approved', 2: 'rejected' }
  return map[status] || ''
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 通过申请 - 打开确认弹窗
const handleApprove = (app) => {
  console.log('handleApprove called:', app)
  approvingApp.value = app
  showApproveModal.value = true
}

const closeApproveModal = () => {
  showApproveModal.value = false
  approvingApp.value = null
}

// 确认通过
const confirmApprove = async () => {
  if (!approvingApp.value) return

  try {
    actionLoading.value = true
    await adminApi.approveApplication(approvingApp.value.id)
    alert('审核通过！该用户已成为化妆师')
    closeApproveModal()
    fetchApplications()
    fetchPendingCount()
  } catch (error) {
    console.error('审核失败:', error)
    alert('审核失败: ' + (error.message || '未知错误'))
  } finally {
    actionLoading.value = false
  }
}

// 拒绝申请
const handleReject = (app) => {
  console.log('handleReject called:', app)
  rejectingApp.value = app
  rejectReason.value = ''
  showRejectModal.value = true
}

const closeRejectModal = () => {
  showRejectModal.value = false
  rejectingApp.value = null
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('请输入拒绝原因')
    return
  }

  try {
    actionLoading.value = true
    await adminApi.rejectApplication(rejectingApp.value.id, rejectReason.value)
    alert('已拒绝申请')
    closeRejectModal()
    fetchApplications()
    fetchPendingCount()
  } catch (error) {
    console.error('拒绝失败:', error)
    alert('拒绝失败: ' + (error.message || '未知错误'))
  } finally {
    actionLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchApplications()
  fetchPendingCount()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tabs button {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-tabs button.active {
  background: #1a1a2e;
  color: #fff;
  border-color: #1a1a2e;
}

.badge {
  background: #e74c3c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.filter-tabs button.active .badge {
  background: #fff;
  color: #1a1a2e;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: #666;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #ddd;
  border-top-color: #C1B0A1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.application-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.app-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header h3 {
  font-size: 15px;
  font-weight: 500;
  margin: 0;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.approved { background: #e8f5e9; color: #2e7d32; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.app-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.info-item label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-item span,
.info-item p {
  font-size: 14px;
  color: #333;
}

.phone-number {
  font-weight: 500;
  color: #1a1a2e !important;
}

.info-item.full {
  grid-column: 1 / -1;
  margin-top: 12px;
}

.info-item.full p {
  line-height: 1.6;
  margin: 0;
}

.reject-reason {
  background: #fff8e1;
  padding: 12px;
  border-radius: 4px;
  margin-top: 12px;
}

.reject-reason label {
  color: #e65100 !important;
}

/* 作品集图片 */
.portfolio-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.portfolio-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #eee;
  transition: transform 0.2s;
}

.portfolio-img:hover {
  transform: scale(1.05);
}

.app-actions {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.btn-approve {
  padding: 10px 32px;
  background: #2e7d32;
  color: #fff;
  font-size: 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-approve:hover {
  background: #1b5e20;
}

.btn-reject {
  padding: 10px 32px;
  background: #fff;
  color: #c62828;
  font-size: 14px;
  border: 1px solid #c62828;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reject:hover {
  background: #ffebee;
}

.empty-msg {
  text-align: center;
  color: #999;
  padding: 48px;
  font-size: 14px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 0;
  border-top: 1px solid #eee;
  margin-top: 16px;
}

.pagination-info {
  font-size: 13px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-btn {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-current {
  font-size: 13px;
  color: #666;
}

/* 弹窗 */
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
  width: 480px;
  max-width: 90vw;
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
  font-size: 16px;
  font-weight: 500;
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

.confirm-content {
  text-align: center;
}

.confirm-content p {
  margin: 0 0 8px;
  font-size: 15px;
}

.confirm-content strong {
  color: #1a1a2e;
}

.confirm-tip {
  color: #666;
  font-size: 13px !important;
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

.required {
  color: #e74c3c;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
}

.form-input:focus {
  outline: none;
  border-color: #C1B0A1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm-approve {
  padding: 10px 24px;
  border: none;
  background: #2e7d32;
  color: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-confirm-approve:hover:not(:disabled) {
  background: #1b5e20;
}

.btn-confirm-approve:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-confirm-reject {
  padding: 10px 24px;
  border: none;
  background: #c62828;
  color: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-confirm-reject:hover:not(:disabled) {
  background: #b71c1c;
}

.btn-confirm-reject:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
