<template>
  <div class="admin-page">
    <!-- 搜索和筛选工具栏 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索化妆师姓名" 
            @keyup.enter="handleSearch" 
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <select v-model="filterStatus" class="filter-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
        <select v-model="filterRecommend" class="filter-select" @change="handleSearch">
          <option value="">全部</option>
          <option value="1">推荐</option>
          <option value="0">普通</option>
        </select>
      </div>
    </div>

    <!-- 化妆师列表表格 -->
    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>擅长</th>
            <th>从业年限</th>
            <th>评分</th>
            <th>推荐</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="artist in artists" :key="artist.id">
            <td>{{ artist.id }}</td>
            <td>
              <div class="artist-info">
                <img :src="artist.avatar || defaultAvatar" class="artist-avatar" />
                <span>{{ artist.realName }}</span>
              </div>
            </td>
            <td>{{ artist.specialties || '-' }}</td>
            <td>{{ artist.experienceYears }}年</td>
            <td><span class="rating">⭐ {{ parseFloat(artist.rating || 0).toFixed(1) }}</span></td>
            <td>
              <span 
                class="recommend-badge" 
                :class="artist.isRecommend === 1 ? 'recommended' : 'normal'"
              >
                {{ artist.isRecommend === 1 ? '推荐' : '普通' }}
              </span>
            </td>
            <td>
              <span 
                class="status-badge" 
                :class="artist.status === 1 ? 'active' : 'inactive'"
              >
                {{ artist.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="action-btn" @click="handleViewDetail(artist)">查看</button>
              <button class="action-btn" @click="handleEdit(artist)">编辑</button>
              <button 
                class="action-btn" 
                :class="artist.isRecommend === 1 ? 'warning' : 'primary'" 
                @click="handleToggleRecommend(artist)"
              >
                {{ artist.isRecommend === 1 ? '取消推荐' : '设为推荐' }}
              </button>
              <button 
                class="action-btn" 
                :class="artist.status === 1 ? 'danger' : 'success'" 
                @click="handleToggleStatus(artist)"
              >
                {{ artist.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="action-btn danger" @click="handleDelete(artist)">删除</button>
            </td>
          </tr>
          <tr v-if="artists.length === 0">
            <td colspan="8" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>

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

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-lg">
        <div class="modal-header">
          <h3>化妆师详情</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body" v-if="currentArtist">
          <div class="detail-section">
            <div class="detail-header">
              <img :src="currentArtist.avatar || defaultAvatar" class="detail-avatar" />
              <div class="detail-info">
                <h4>{{ currentArtist.realName }}</h4>
                <p class="detail-specialty">{{ currentArtist.specialty }}</p>
                <div class="detail-meta">
                  <span class="rating">⭐ {{ currentArtist.rating?.toFixed(1) || '0.0' }}</span>
                  <span>{{ currentArtist.experienceYears }}年经验</span>
                  <span v-if="currentArtist.isRecommend === 1" class="recommend-tag">推荐化妆师</span>
                </div>
              </div>
            </div>
            <div class="detail-content">
              <div class="detail-item">
                <label>简介</label>
                <p>{{ currentArtist.introduction || '暂无简介' }}</p>
              </div>
              <div class="detail-item">
                <label>作品集</label>
                <p>{{ currentArtist.portfolio || '暂无作品集链接' }}</p>
              </div>
              <div class="detail-item">
                <label>入驻时间</label>
                <p>{{ formatDate(currentArtist.createTime) }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>编辑化妆师</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <!-- 头像上传 -->
          <div class="form-group">
            <label>头像照片</label>
            <div class="avatar-upload-section">
              <div class="avatar-preview-large">
                <img :src="formData.avatar || defaultAvatar" alt="头像预览" />
              </div>
              <div class="avatar-upload-actions">
                <input 
                  ref="avatarInput"
                  type="file" 
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  @change="handleAvatarUpload"
                  style="display: none"
                />
                <button type="button" class="upload-btn" @click="$refs.avatarInput.click()">
                  {{ uploading ? '上传中...' : '选择图片' }}
                </button>
                <p class="upload-tip">支持 jpg、png、gif、webp，最大 5MB</p>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>真实姓名</label>
            <input v-model="formData.realName" type="text" placeholder="请输入真实姓名" />
          </div>
          <div class="form-group">
            <label>擅长领域</label>
            <input v-model="formData.specialty" type="text" placeholder="请输入擅长领域，多个用逗号分隔" />
          </div>
          <div class="form-group">
            <label>从业年限</label>
            <input v-model.number="formData.experienceYears" type="number" min="0" placeholder="请输入从业年限" />
          </div>
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="formData.introduction" rows="4" placeholder="请输入个人简介"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="submit-btn" @click="handleSubmitEdit" :disabled="submitting">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

// 常量
const defaultAvatar = 'https://via.placeholder.com/40?text=Artist'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const artists = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索和筛选
const searchKeyword = ref('')
const filterStatus = ref('')
const filterRecommend = ref('')

// 弹窗状态
const showDetailModal = ref(false)
const showEditModal = ref(false)
const currentArtist = ref(null)

// 编辑表单
const formData = ref({
  realName: '',
  specialty: '',
  experienceYears: 0,
  introduction: '',
  avatar: ''
})

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取化妆师列表
const fetchArtists = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.realName = searchKeyword.value
    }
    
    if (filterStatus.value !== '') {
      params.status = parseInt(filterStatus.value)
    }
    
    if (filterRecommend.value !== '') {
      params.isRecommend = parseInt(filterRecommend.value)
    }
    
    const data = await adminApi.getArtistList(params)
    if (data && data.records) {
      artists.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取化妆师列表失败:', error)
    alert('获取化妆师列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchArtists()
}

// 分页
const changePage = (page) => {
  currentPage.value = page
  fetchArtists()
}

// 查看详情
const handleViewDetail = async (artist) => {
  try {
    const detail = await adminApi.getArtistDetail(artist.id)
    currentArtist.value = detail || artist
    showDetailModal.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    currentArtist.value = artist
    showDetailModal.value = true
  }
}

// 编辑化妆师
const handleEdit = (artist) => {
  currentArtist.value = artist
  formData.value = {
    realName: artist.realName || '',
    specialty: artist.specialties || '',
    experienceYears: artist.experienceYears || 0,
    introduction: artist.description || '',
    avatar: artist.avatar || ''
  }
  showEditModal.value = true
}

// 头像上传
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  try {
    uploading.value = true
    const formDataObj = new FormData()
    formDataObj.append('file', file)
    
    // 调用上传API (使用后端完整URL)
    const response = await fetch('http://localhost:8124/api/upload/avatar', {
      method: 'POST',
      body: formDataObj,
      credentials: 'include'
    })
    const result = await response.json()
    
    if (result.code === 0) {
      formData.value.avatar = result.data
      alert('头像上传成功！')
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!formData.value.realName) {
    alert('请输入真实姓名')
    return
  }
  
  try {
    submitting.value = true
    await adminApi.updateArtist(currentArtist.value.id, formData.value)
    alert('编辑成功')
    closeModal()
    fetchArtists()
  } catch (error) {
    console.error('编辑失败:', error)
    alert('编辑失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 删除化妆师
const handleDelete = async (artist) => {
  // 直接执行删除（管理员操作）
  try {
    await adminApi.deleteArtist(artist.id)
    fetchArtists()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 切换推荐状态
const handleToggleRecommend = async (artist) => {
  try {
    const newStatus = artist.isRecommend === 1 ? 0 : 1
    await adminApi.setArtistRecommend(artist.id, newStatus)
    artist.isRecommend = newStatus
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 切换状态
const handleToggleStatus = async (artist) => {
  try {
    const newStatus = artist.status === 1 ? 0 : 1
    await adminApi.updateArtistStatus(artist.id, newStatus)
    artist.status = newStatus
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 关闭弹窗
const closeModal = () => {
  showDetailModal.value = false
  showEditModal.value = false
  currentArtist.value = null
  formData.value = {
    realName: '',
    specialty: '',
    experienceYears: 0,
    introduction: ''
  }
}

// 初始化
onMounted(() => {
  fetchArtists()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box input {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 240px;
  font-size: 13px;
}

.search-btn {
  padding: 8px 20px;
  background: #1a1a2e;
  color: #fff;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #2a2a4e;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
}

/* 卡片和表格 */
.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
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

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 13px;
}

.admin-table th {
  background: #fafafa;
  color: #666;
  font-weight: 500;
}

.empty-row {
  text-align: center !important;
  color: #999;
  padding: 40px 16px !important;
}

/* 化妆师信息 */
.artist-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.artist-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.rating {
  color: #f5a623;
}

/* 徽章样式 */
.recommend-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.recommend-badge.recommended {
  background: #fff8e1;
  color: #f5a623;
}

.recommend-badge.normal {
  background: #f5f5f5;
  color: #999;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.active { background: #e8f5e9; color: #2e7d32; }
.status-badge.inactive { background: #ffebee; color: #c62828; }

/* 操作按钮 */
.action-btn {
  padding: 4px 12px;
  font-size: 12px;
  margin-right: 8px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f5f5f5;
}

.action-btn.primary {
  color: #C1B0A1;
  border-color: #C1B0A1;
}

.action-btn.primary:hover {
  background: #f9f7f5;
}

.action-btn.warning {
  color: #f57c00;
  border-color: #f57c00;
}

.action-btn.warning:hover {
  background: #fff3e0;
}

.action-btn.success {
  color: #2e7d32;
  border-color: #2e7d32;
}

.action-btn.success:hover {
  background: #e8f5e9;
}

.action-btn.danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.action-btn.danger:hover {
  background: #ffebee;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #eee;
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
  transition: all 0.2s;
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
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal.modal-lg {
  width: 600px;
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

/* 详情样式 */
.detail-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.detail-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.detail-info h4 {
  margin: 0 0 8px;
  font-size: 18px;
}

.detail-specialty {
  color: #C1B0A1;
  margin: 0 0 8px;
}

.detail-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #666;
}

.recommend-tag {
  background: #fff8e1;
  color: #f5a623;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.detail-item p {
  margin: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #C1B0A1;
}

/* 头像上传 */
.avatar-upload-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #eee;
  flex-shrink: 0;
}

.avatar-preview-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-btn {
  display: inline-block;
  padding: 8px 20px;
  background: #fff;
  border: 1px solid #C1B0A1;
  color: #C1B0A1;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  background: #f9f7f5;
}

.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

.cancel-btn {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.submit-btn {
  padding: 10px 24px;
  border: none;
  background: #C1B0A1;
  color: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #a89888;
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
