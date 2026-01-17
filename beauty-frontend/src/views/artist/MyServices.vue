<template>
  <div class="page">
    <div class="toolbar">
      <button class="btn-add" @click="openAddModal">+ 添加服务套餐</button>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>封面</th>
            <th>名称</th>
            <th>价格</th>
            <th>时长</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in services" :key="service.id">
            <td><img :src="service.coverImage || defaultCover" class="cover-img" /></td>
            <td>{{ service.name }}</td>
            <td class="price">¥{{ service.price }}</td>
            <td>{{ service.duration }}分钟</td>
            <td>
              <span class="status-badge" :class="service.status === 1 ? 'on' : 'off'">
                {{ service.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td>
              <button class="action-btn" @click="openEditModal(service)">编辑</button>
              <button class="action-btn" @click="toggleStatus(service)">{{ service.status === 1 ? '下架' : '上架' }}</button>
              <button class="action-btn danger" @click="deleteService(service)">删除</button>
            </td>
          </tr>
          <tr v-if="services.length === 0">
            <td colspan="6" class="empty">暂无服务套餐，点击上方按钮添加</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑服务套餐' : '添加服务套餐' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>套餐名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入套餐名称" />
          </div>
          <div class="form-group">
            <label>价格 *</label>
            <input v-model.number="form.price" type="number" placeholder="请输入价格" />
          </div>
          <div class="form-group">
            <label>时长(分钟)</label>
            <input v-model.number="form.duration" type="number" placeholder="请输入服务时长" />
          </div>
          <div class="form-group">
            <label>封面图片</label>
            <div class="image-upload-area">
              <div v-if="form.coverImage" class="image-preview">
                <img :src="form.coverImage" alt="封面预览" />
                <button type="button" class="remove-image-btn" @click="form.coverImage = ''">×</button>
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
            <label>描述</label>
            <textarea v-model="form.description" placeholder="请输入服务描述"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-submit" @click="submitForm" :disabled="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { artistPortalApi, uploadApi } from '../../api'

const loading = ref(false)
const services = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const editId = ref(null)
const fileInput = ref(null)
const defaultCover = 'https://via.placeholder.com/80x60?text=No+Image'

const form = ref({
  name: '',
  price: null,
  duration: 60,
  coverImage: '',
  description: ''
})

const fetchServices = async () => {
  try {
    loading.value = true
    const data = await artistPortalApi.getServices()
    if (data) {
      services.value = data
    }
  } catch (error) {
    console.error('获取服务失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddModal = () => {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', price: null, duration: 60, coverImage: '', description: '' }
  showModal.value = true
}

const openEditModal = (service) => {
  isEdit.value = true
  editId.value = service.id
  form.value = {
    name: service.name,
    price: service.price,
    duration: service.duration,
    coverImage: service.coverImage,
    description: service.description
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const submitForm = async () => {
  if (!form.value.name) {
    alert('请输入套餐名称')
    return
  }
  if (!form.value.price) {
    alert('请输入价格')
    return
  }

  try {
    submitting.value = true
    if (isEdit.value) {
      await artistPortalApi.updateService(editId.value, form.value)
      alert('更新成功')
    } else {
      await artistPortalApi.createService(form.value)
      alert('添加成功')
    }
    closeModal()
    fetchServices()
  } catch (error) {
    alert('操作失败: ' + (error.message || '请稍后再试'))
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (service) => {
  const newStatus = service.status === 1 ? 0 : 1
  try {
    await artistPortalApi.updateServiceStatus(service.id, newStatus)
    alert(newStatus === 1 ? '已上架' : '已下架')
    fetchServices()
  } catch (error) {
    alert('操作失败: ' + (error.message || '请稍后再试'))
  }
}

const deleteService = async (service) => {
  if (!confirm(`确定要删除"${service.name}"吗？`)) return
  try {
    await artistPortalApi.deleteService(service.id)
    alert('已删除')
    fetchServices()
  } catch (error) {
    alert('删除失败: ' + (error.message || '请稍后再试'))
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
      form.value.coverImage = result
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('图片上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

onMounted(() => {
  fetchServices()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; justify-content: flex-end; }
.btn-add { padding: 10px 24px; background: #2c3e50; color: #fff; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-add:hover { background: #1a252f; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }
.loading-container { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 24px; height: 24px; border: 2px solid #ddd; border-top-color: #C1B0A1; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #fafafa; color: #666; font-weight: 500; }
.cover-img { width: 60px; height: 45px; object-fit: cover; border-radius: 4px; }
.price { color: #e74c3c; font-weight: 500; }
.empty { text-align: center; color: #999; padding: 40px !important; }
.status-badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.status-badge.on { background: #e8f5e9; color: #2e7d32; }
.status-badge.off { background: #fafafa; color: #999; }
.action-btn { padding: 4px 12px; font-size: 12px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; margin-right: 8px; }
.action-btn:hover { background: #f5f5f5; }
.action-btn.danger { color: #e74c3c; border-color: #e74c3c; }
.action-btn.danger:hover { background: #ffebee; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: #fff; border-radius: 8px; width: 500px; max-width: 90vw; max-height: 90vh; overflow: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #eee; }
.modal-header h3 { margin: 0; font-size: 16px; font-weight: 500; }
.close-btn { width: 32px; height: 32px; border: none; background: transparent; font-size: 24px; color: #999; cursor: pointer; }
.modal-body { padding: 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.form-group input, .form-group textarea { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
.form-group textarea { height: 80px; resize: vertical; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid #eee; }
.btn-cancel { padding: 10px 24px; border: 1px solid #ddd; background: #fff; border-radius: 6px; cursor: pointer; }
.btn-submit { padding: 10px 24px; background: #2c3e50; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

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
</style>
