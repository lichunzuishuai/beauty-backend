<template>
  <div class="admin-page">
    <!-- 搜索和筛选工具栏 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <div class="search-box">
          <input 
            v-model="searchName" 
            type="text" 
            placeholder="搜索套餐名称" 
            @keyup.enter="handleSearch" 
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <select v-model="filterStatus" class="filter-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="1">已上架</option>
          <option value="0">已下架</option>
        </select>
      </div>
      <button class="add-btn" @click="openAddModal">+ 新增套餐</button>
    </div>

    <!-- 服务套餐列表表格 -->
    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>封面</th>
            <th>套餐名称</th>
            <th>化妆师</th>
            <th>价格</th>
            <th>时长</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in services" :key="service.id">
            <td>{{ service.id }}</td>
            <td>
              <img v-if="service.coverImage" :src="service.coverImage" class="cover-image" alt="封面"/>
              <span v-else class="no-image">-</span>
            </td>
            <td>{{ service.name }}</td>
            <td>{{ getArtistName(service.artistId) }}</td>
            <td class="price">¥{{ service.price }}</td>
            <td>{{ service.duration ? service.duration + '分钟' : '-' }}</td>
            <td>
              <span class="status-badge" :class="service.status === 1 ? 'active' : 'inactive'">
                {{ service.status === 1 ? '已上架' : '已下架' }}
              </span>
            </td>
            <td>{{ formatDate(service.createTime) }}</td>
            <td>
              <button class="action-btn" @click="handleEdit(service)">编辑</button>
              <button 
                class="action-btn" 
                :class="service.status === 1 ? 'warning' : 'success'" 
                @click="handleToggleStatus(service)"
              >
                {{ service.status === 1 ? '下架' : '上架' }}
              </button>
              <button class="action-btn danger" @click="handleDelete(service)">删除</button>
            </td>
          </tr>
          <tr v-if="services.length === 0">
            <td colspan="9" class="empty-row">暂无数据</td>
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

    <!-- 新增/编辑弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑套餐' : '新增套餐' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group" v-if="!showEditModal">
            <label>选择化妆师 <span class="required">*</span></label>
            <select v-model="formData.artistId" class="form-select">
              <option value="">请选择化妆师</option>
              <option v-for="artist in artists" :key="artist.id" :value="artist.id">
                {{ artist.realName || artist.id }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>套餐名称 <span class="required">*</span></label>
            <input v-model="formData.name" type="text" placeholder="请输入套餐名称" />
          </div>
          <div class="form-group">
            <label>价格 <span class="required">*</span></label>
            <input v-model="formData.price" type="number" step="0.01" placeholder="请输入价格" />
          </div>
          <div class="form-group">
            <label>服务时长（分钟）</label>
            <input v-model="formData.duration" type="number" placeholder="请输入服务时长" />
          </div>
          <div class="form-group">
            <label>套餐描述</label>
            <textarea v-model="formData.description" placeholder="请输入套餐描述" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>封面图片</label>
            <div class="image-upload-area">
              <div v-if="formData.coverImage" class="image-preview">
                <img :src="formData.coverImage" alt="封面预览" />
                <button class="remove-image-btn" @click="formData.coverImage = ''">×</button>
              </div>
              <div v-else class="upload-placeholder" @click="triggerUpload">
                <input 
                  ref="fileInput" 
                  type="file" 
                  accept="image/*" 
                  @change="handleImageUpload" 
                  style="display: none;"
                />
                <span class="upload-icon">📷</span>
                <span class="upload-text">点击上传封面图片</span>
              </div>
            </div>
            <div v-if="uploading" class="upload-progress">上传中...</div>
          </div>
          <div class="form-group">
            <label>排序权重</label>
            <input v-model="formData.sortOrder" type="number" placeholder="数字越小越靠前" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="submit-btn" @click="handleSubmit" :disabled="submitting || uploading">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi, uploadApi } from '../../api'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const services = ref([])
const artists = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const fileInput = ref(null)

// 搜索和筛选
const searchName = ref('')
const filterStatus = ref('')

// 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingService = ref(null)

// 表单数据
const formData = ref({
  artistId: '',
  name: '',
  price: '',
  duration: '',
  description: '',
  coverImage: '',
  sortOrder: 0
})

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取化妆师姓名
const getArtistName = (artistId) => {
  const artist = artists.value.find(a => a.id === artistId)
  return artist ? (artist.realName || `ID: ${artistId}`) : artistId
}

// 获取化妆师列表
const fetchArtists = async () => {
  try {
    const data = await adminApi.getArtistList({ current: 1, pageSize: 100 })
    if (data && data.records) {
      artists.value = data.records
    }
  } catch (error) {
    console.error('获取化妆师列表失败:', error)
  }
}

// 获取服务套餐列表
const fetchServices = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchName.value) {
      params.name = searchName.value
    }
    if (filterStatus.value !== '') {
      params.status = parseInt(filterStatus.value)
    }
    
    const data = await adminApi.getServiceList(params)
    if (data && data.records) {
      services.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取服务套餐列表失败:', error)
    alert('获取服务套餐列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 分页
const changePage = (page) => {
  currentPage.value = page
  fetchServices()
}

// 打开新增弹窗
const openAddModal = () => {
  if (artists.value.length === 0) {
    fetchArtists()
  }
  showAddModal.value = true
}

// 编辑套餐
const handleEdit = (service) => {
  if (artists.value.length === 0) {
    fetchArtists()
  }
  editingService.value = service
  formData.value = {
    artistId: service.artistId,
    name: service.name || '',
    price: service.price || '',
    duration: service.duration || '',
    description: service.description || '',
    coverImage: service.coverImage || '',
    sortOrder: service.sortOrder || 0
  }
  showEditModal.value = true
}

// 切换状态
const handleToggleStatus = async (service) => {
  try {
    const newStatus = service.status === 1 ? 0 : 1
    await adminApi.updateServiceStatus(service.id, newStatus)
    service.status = newStatus
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 删除套餐
const handleDelete = async (service) => {
  try {
    await adminApi.deleteService(service.id)
    fetchServices()
  } catch (error) {
    console.error('删除套餐失败:', error)
  }
}

// 触发文件上传
const triggerUpload = () => {
  fileInput.value?.click()
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    uploading.value = true
    const result = await uploadApi.uploadImage(file, 'service')
    if (result) {
      formData.value.coverImage = result
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('图片上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  editingService.value = null
  formData.value = {
    artistId: '',
    name: '',
    price: '',
    duration: '',
    description: '',
    coverImage: '',
    sortOrder: 0
  }
}

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!showEditModal.value && !formData.value.artistId) {
    alert('请选择化妆师')
    return
  }
  if (!formData.value.name) {
    alert('请输入套餐名称')
    return
  }
  if (!formData.value.price || parseFloat(formData.value.price) <= 0) {
    alert('请输入有效的价格')
    return
  }

  try {
    submitting.value = true
    
    const submitData = {
      name: formData.value.name,
      price: parseFloat(formData.value.price),
      duration: formData.value.duration ? parseInt(formData.value.duration) : null,
      description: formData.value.description,
      coverImage: formData.value.coverImage,
      sortOrder: formData.value.sortOrder ? parseInt(formData.value.sortOrder) : 0
    }
    
    if (showEditModal.value) {
      await adminApi.updateService(editingService.value.id, submitData)
      alert('编辑成功')
    } else {
      submitData.artistId = parseInt(formData.value.artistId)
      await adminApi.createService(submitData)
      alert('新增成功')
    }
    
    closeModal()
    fetchServices()
  } catch (error) {
    console.error('提交失败:', error)
    alert('操作失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchServices()
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
  width: 200px;
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
}

.search-btn:hover { background: #2a2a4e; }

.filter-select, .form-select {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  background: #fff;
}

.form-select {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
}

.add-btn {
  padding: 10px 24px;
  background: #C1B0A1;
  color: #fff;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover { background: #a89888; }

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

@keyframes spin { to { transform: rotate(360deg); } }

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th, .admin-table td {
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

.cover-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image { color: #999; }
.price { color: #e74c3c; font-weight: 500; }
.empty-row { text-align: center !important; color: #999; padding: 40px 16px !important; }

.status-badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.status-badge.active { background: #e8f5e9; color: #2e7d32; }
.status-badge.inactive { background: #ffebee; color: #c62828; }

.action-btn {
  padding: 4px 12px;
  font-size: 12px;
  margin-right: 8px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.action-btn:hover { background: #f5f5f5; }
.action-btn.warning { color: #f57c00; border-color: #f57c00; }
.action-btn.warning:hover { background: #fff3e0; }
.action-btn.success { color: #2e7d32; border-color: #2e7d32; }
.action-btn.success:hover { background: #e8f5e9; }
.action-btn.danger { color: #e74c3c; border-color: #e74c3c; }
.action-btn.danger:hover { background: #ffebee; }

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #eee;
}

.pagination-info { font-size: 13px; color: #666; }
.pagination-controls { display: flex; align-items: center; gap: 12px; }

.page-btn {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) { background: #f5f5f5; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-current { font-size: 13px; color: #666; }

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
  width: 520px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 { margin: 0; font-size: 16px; font-weight: 500; }

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.close-btn:hover { color: #333; }

.modal-body { padding: 24px; }

.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; color: #333; }
.required { color: #e74c3c; }

.form-group input, .form-group select, .form-group textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea { resize: vertical; }
.form-group input:focus, .form-group textarea:focus, .form-group select:focus { 
  outline: none; 
  border-color: #C1B0A1; 
}

/* 图片上传区域 */
.image-upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-placeholder:hover {
  background: #fafafa;
  border-color: #C1B0A1;
}

.upload-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 13px;
  color: #666;
}

.image-preview {
  position: relative;
  padding: 10px;
}

.image-preview img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
}

.remove-image-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-image-btn:hover {
  background: rgba(0,0,0,0.7);
}

.upload-progress {
  text-align: center;
  padding: 8px;
  color: #C1B0A1;
  font-size: 13px;
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
}

.cancel-btn:hover { background: #f5f5f5; }

.submit-btn {
  padding: 10px 24px;
  border: none;
  background: #C1B0A1;
  color: #fff;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:hover:not(:disabled) { background: #a89888; }
.submit-btn:disabled { opacity: 0.7; cursor: not-allowed; }
</style>
