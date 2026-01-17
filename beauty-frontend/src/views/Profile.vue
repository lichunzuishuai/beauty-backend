<template>
  <div class="page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的账户信息</p>
      </div>
    </div>

    <div class="container">
      <div class="profile-layout">
        <!-- Sidebar -->
        <aside class="profile-sidebar">
          <div class="user-card">
            <div class="user-avatar">
              <img :src="user?.avatar || defaultAvatar" alt="头像" />
            </div>
            <h2 class="user-name">{{ user?.nickname || user?.username || '用户' }}</h2>
            <p class="user-id">ID: {{ user?.id }}</p>
            <p class="user-role">
              <span class="role-tag" :class="getRoleClass(user?.role)">
                {{ getRoleText(user?.role) }}
              </span>
            </p>
          </div>
          <nav class="profile-menu">
            <button 
              v-for="item in menuItems" 
              :key="item.key"
              class="menu-item"
              :class="{ active: currentMenu === item.key }"
              @click="currentMenu = item.key"
            >
              {{ item.label }}
            </button>
          </nav>
        </aside>

        <!-- Content -->
        <main class="profile-content">
          <!-- 基本信息 -->
          <div v-if="currentMenu === 'info'" class="content-section">
            <h3 class="section-title">基本信息</h3>
            <form class="profile-form" @submit.prevent="handleUpdateProfile">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <input type="text" class="form-input" :value="user?.username" disabled />
              </div>
              <div class="form-group">
                <label class="form-label">昵称</label>
                <input v-model="form.nickname" type="text" class="form-input" placeholder="请输入昵称" />
              </div>
              <div class="form-group">
                <label class="form-label">手机号</label>
                <input v-model="form.phone" type="tel" class="form-input" placeholder="请输入手机号" />
              </div>
              
              <!-- 头像上传 -->
              <div class="form-group">
                <label class="form-label">头像</label>
                <div class="avatar-upload">
                  <div class="avatar-preview">
                    <img :src="form.avatar || defaultAvatar" alt="头像预览" />
                  </div>
                  <div class="avatar-actions">
                    <label class="btn-ghost btn-sm upload-btn">
                      <input 
                        ref="fileInput"
                        type="file" 
                        accept="image/jpeg,image/png,image/gif,image/webp"
                        @change="handleFileChange"
                        hidden
                      />
                      {{ uploading ? '上传中...' : '选择图片' }}
                    </label>
                    <p class="upload-tip">支持 jpg、png、gif、webp 格式，最大 5MB</p>
                  </div>
                </div>
              </div>
              
              <button type="submit" class="btn-solid" :disabled="uploading">保存修改</button>
            </form>
          </div>

          <!-- 安全设置 -->
          <div v-if="currentMenu === 'security'" class="content-section">
            <h3 class="section-title">安全设置</h3>
            <div class="security-item">
              <div class="security-info">
                <h4>登录密码</h4>
                <p>用于保护账号安全</p>
              </div>
              <button class="btn-ghost btn-sm" @click="showPasswordModal = true">修改密码</button>
            </div>
            <div class="security-item">
              <div class="security-info">
                <h4>绑定手机</h4>
                <p>{{ user?.phone ? maskPhone(user.phone) : '未绑定' }}</p>
              </div>
              <button class="btn-ghost btn-sm">{{ user?.phone ? '更换' : '绑定' }}</button>
            </div>
          </div>

          <!-- 入驻申请 -->
          <div v-if="currentMenu === 'artist'" class="content-section">
            <h3 class="section-title">化妆师入驻</h3>
            
            <!-- 未申请或已拒绝状态 -->
            <div v-if="!applicationStatus || applicationStatus.status === 2" class="apply-section">
              <div v-if="applicationStatus?.status === 2" class="status-card rejected">
                <h4>❌ 申请已被拒绝</h4>
                <p>拒绝原因：{{ applicationStatus.rejectReason || '审核未通过' }}</p>
                <p>提交时间：{{ formatDate(applicationStatus.createTime) }}</p>
              </div>
              
              <div class="apply-intro">
                <h4>成为平台认证化妆师</h4>
                <p>展示您的专业技能，接受预约订单，开启美丽事业。</p>
                <div class="apply-benefits">
                  <div class="benefit-item">
                    <span class="benefit-icon">💄</span>
                    <span>官方认证标识</span>
                  </div>
                  <div class="benefit-item">
                    <span class="benefit-icon">📈</span>
                    <span>平台流量曝光</span>
                  </div>
                  <div class="benefit-item">
                    <span class="benefit-icon">💰</span>
                    <span>便捷收款结算</span>
                  </div>
                </div>
              </div>

              <!-- 申请表单 -->
              <form class="apply-form" @submit.prevent="handleApply">
                <h4 class="form-section-title">填写入驻信息</h4>
                
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">真实姓名 <span class="required">*</span></label>
                    <input v-model="applyForm.realName" type="text" class="form-input" placeholder="请输入真实姓名" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">联系电话 <span class="required">*</span></label>
                    <input v-model="applyForm.phone" type="tel" class="form-input" placeholder="请输入联系电话" />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">身份证号</label>
                    <input v-model="applyForm.idCard" type="text" class="form-input" placeholder="请输入身份证号" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">从业年限 <span class="required">*</span></label>
                    <input v-model.number="applyForm.experienceYears" type="number" min="0" class="form-input" placeholder="请输入从业年限" />
                  </div>
                </div>

                <div class="form-group">
                  <label class="form-label">擅长领域 <span class="required">*</span></label>
                  <div class="specialty-options">
                    <label 
                      v-for="specialty in specialtyOptions" 
                      :key="specialty"
                      class="specialty-option"
                      :class="{ selected: selectedSpecialties.includes(specialty) }"
                    >
                      <input 
                        type="checkbox" 
                        :value="specialty" 
                        v-model="selectedSpecialties"
                        hidden
                      />
                      {{ specialty }}
                    </label>
                  </div>
                </div>

                <div class="form-group">
                  <label class="form-label">个人介绍 <span class="required">*</span></label>
                  <textarea 
                    v-model="applyForm.introduction" 
                    class="form-input" 
                    rows="4"
                    placeholder="请介绍您的从业经历、专业技能、服务特色等"
                  ></textarea>
                </div>

                <button type="submit" class="btn-solid btn-lg" :disabled="applyLoading">
                  {{ applyLoading ? '提交中...' : '提交申请' }}
                </button>
              </form>
            </div>

            <!-- 待审核状态 -->
            <div v-else-if="applicationStatus.status === 0" class="status-section">
              <div class="status-card pending">
                <div class="status-icon">⏳</div>
                <h4>申请审核中</h4>
                <p>您的入驻申请正在审核中，请耐心等待</p>
                <p class="status-time">提交时间：{{ formatDate(applicationStatus.createTime) }}</p>
              </div>
            </div>

            <!-- 已通过状态 -->
            <div v-else-if="applicationStatus.status === 1" class="status-section">
              <div class="status-card approved">
                <div class="status-icon">✅</div>
                <h4>恭喜您已成为认证化妆师</h4>
                <p>您现在可以接收预约订单，开始您的美丽事业</p>
                <p class="status-time">通过时间：{{ formatDate(applicationStatus.updateTime || applicationStatus.createTime) }}</p>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { userApi, uploadApi } from '../api'

const userStore = useUserStore()
const defaultAvatar = 'https://via.placeholder.com/120?text=Avatar'

const user = ref(null)
const currentMenu = ref('info')
const showPasswordModal = ref(false)
const applicationStatus = ref(null)
const uploading = ref(false)
const applyLoading = ref(false)
const fileInput = ref(null)

const form = reactive({
  nickname: '',
  phone: '',
  avatar: ''
})

const applyForm = reactive({
  realName: '',
  phone: '',
  idCard: '',
  experienceYears: 0,
  specialties: '',
  introduction: ''
})

const selectedSpecialties = ref([])

const specialtyOptions = ['新娘妆', '日常妆', '晚宴妆', '古风妆', '时尚妆', '影视妆', '舞台妆', '特效妆']

const menuItems = [
  { key: 'info', label: '基本信息' },
  { key: 'security', label: '安全设置' },
  { key: 'artist', label: '化妆师入驻' }
]

const getRoleText = (role) => {
  const map = { 0: '普通用户', 1: '化妆师', 2: '管理员' }
  return map[role] || '普通用户'
}

const getRoleClass = (role) => {
  const map = { 0: 'user', 1: 'artist', 2: 'admin' }
  return map[role] || 'user'
}

const fetchUserInfo = async () => {
  try {
    const data = await userApi.getCurrent()
    if (data) {
      user.value = data
      form.nickname = data.nickname || ''
      form.phone = data.phone || ''
      form.avatar = data.avatar || ''
      
      // 预填申请表单
      applyForm.realName = data.nickname || ''
      applyForm.phone = data.phone || ''
    } else {
      user.value = userStore.user
      if (user.value) {
        form.nickname = user.value.nickname || ''
        form.phone = user.value.phone || ''
        form.avatar = user.value.avatar || ''
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    user.value = userStore.user
  }
}

const fetchApplicationStatus = async () => {
  try {
    const data = await userApi.getApplicationStatus()
    applicationStatus.value = data
  } catch (error) {
    console.log('未找到申请记录')
    applicationStatus.value = null
  }
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 校验文件大小
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  try {
    uploading.value = true
    const url = await uploadApi.uploadAvatar(file)
    form.avatar = url
    alert('头像上传成功！')
  } catch (error) {
    alert(error.message || '头像上传失败')
  } finally {
    uploading.value = false
    // 重置file input
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

const handleUpdateProfile = async () => {
  try {
    await userApi.updateProfile(form)
    alert('保存成功！')
    fetchUserInfo()
  } catch (error) {
    alert(error.message || '保存失败')
  }
}

const handleApply = async () => {
  // 验证必填字段
  if (!applyForm.realName) {
    alert('请输入真实姓名')
    return
  }
  if (!applyForm.phone) {
    alert('请输入联系电话')
    return
  }
  if (selectedSpecialties.value.length === 0) {
    alert('请选择擅长领域')
    return
  }
  if (!applyForm.introduction) {
    alert('请输入个人介绍')
    return
  }

  try {
    applyLoading.value = true
    
    const requestData = {
      realName: applyForm.realName,
      phone: applyForm.phone,
      idCard: applyForm.idCard,
      experienceYears: applyForm.experienceYears,
      specialties: selectedSpecialties.value.join(','),
      introduction: applyForm.introduction
    }
    
    await userApi.artistApply(requestData)
    
    alert('申请提交成功！请等待管理员审核')
    fetchApplicationStatus()
  } catch (error) {
    console.error('申请失败:', error)
    alert('申请失败: ' + (error.message || '请稍后再试'))
  } finally {
    applyLoading.value = false
  }
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchUserInfo()
  fetchApplicationStatus()
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

/* Layout */
.profile-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--spacing-xl);
}

/* Sidebar */
.profile-sidebar {
  position: sticky;
  top: 120px;
  height: fit-content;
}

.user-card {
  text-align: center;
  padding: var(--spacing-xl);
  background: var(--color-secondary-bg);
  margin-bottom: var(--spacing-lg);
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto var(--spacing-md);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.user-id {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-sm);
}

.role-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.role-tag.user { background: #e3f2fd; color: #1565c0; }
.role-tag.artist { background: #fff3e0; color: #e65100; }
.role-tag.admin { background: #f3e5f5; color: #7b1fa2; }

.profile-menu {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border);
}

.menu-item {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  font-size: var(--font-size-sm);
  letter-spacing: 1px;
  color: var(--color-text-light);
  border-bottom: 1px solid var(--color-border);
  transition: all var(--transition-fast);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover,
.menu-item.active {
  color: var(--color-primary);
  background: var(--color-secondary-bg);
}

.menu-item.active {
  border-left: 3px solid var(--color-accent);
}

/* Content */
.content-section {
  padding: var(--spacing-xl);
  border: 1px solid var(--color-border);
}

.section-title {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

/* Form */
.profile-form .form-group {
  max-width: 400px;
}

.form-group {
  margin-bottom: var(--spacing-md);
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
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-accent);
}

.form-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

textarea.form-input {
  resize: vertical;
  font-family: inherit;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Avatar Upload */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--color-border);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.upload-btn {
  cursor: pointer;
  display: inline-block;
}

.upload-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
}

/* Security */
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg) 0;
  border-bottom: 1px solid var(--color-border);
}

.security-item:last-child {
  border-bottom: none;
}

.security-info h4 {
  font-size: var(--font-size-base);
  margin-bottom: var(--spacing-xs);
}

.security-info p {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.btn-sm {
  padding: 8px 20px;
  font-size: var(--font-size-xs);
}

.btn-lg {
  padding: 14px 32px;
  font-size: var(--font-size-base);
}

/* Artist Apply */
.apply-section {
  max-width: 600px;
}

.apply-intro {
  text-align: center;
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  background: linear-gradient(135deg, #f9f7f5 0%, #f1ebe5 100%);
  border-radius: 8px;
}

.apply-intro h4 {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-sm);
}

.apply-intro p {
  color: var(--color-text-light);
  margin-bottom: var(--spacing-lg);
}

.apply-benefits {
  display: flex;
  justify-content: center;
  gap: var(--spacing-xl);
}

.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.benefit-icon {
  font-size: 24px;
}

.benefit-item span:last-child {
  font-size: 13px;
  color: var(--color-text-light);
}

.form-section-title {
  font-size: var(--font-size-base);
  letter-spacing: 1px;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

.specialty-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.specialty-option {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.specialty-option:hover {
  border-color: var(--color-accent);
}

.specialty-option.selected {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: #fff;
}

/* Status Cards */
.status-section {
  text-align: center;
  padding: var(--spacing-xl);
}

.status-card {
  padding: var(--spacing-xl);
  border-radius: 8px;
  margin-bottom: var(--spacing-lg);
}

.status-card.pending { 
  background: #fff8e1;
  border: 1px solid #ffcc02;
}

.status-card.approved { 
  background: #e8f5e9;
  border: 1px solid #4caf50;
}

.status-card.rejected { 
  background: #ffebee;
  border: 1px solid #f44336;
}

.status-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

.status-card h4 {
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-sm);
}

.status-card p {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-xs);
}

.status-time {
  font-size: 12px !important;
  color: #999 !important;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }
  
  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .apply-benefits {
    flex-direction: column;
    gap: var(--spacing-md);
  }
}
</style>
