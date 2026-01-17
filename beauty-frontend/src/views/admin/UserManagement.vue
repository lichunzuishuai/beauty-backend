<template>
  <div class="admin-page">
    <!-- 搜索和筛选工具栏 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索用户名或手机号" 
            @keyup.enter="handleSearch" 
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <select v-model="filterRole" class="filter-select" @change="handleSearch">
          <option value="">全部角色</option>
          <option value="0">普通用户</option>
          <option value="1">化妆师</option>
          <option value="2">管理员</option>
        </select>
        <select v-model="filterStatus" class="filter-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
      </div>
      <button class="add-btn" @click="showAddModal = true">+ 新增用户</button>
    </div>

    <!-- 用户列表表格 -->
    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span class="role-badge" :class="getRoleClass(user.role)">
                {{ getRoleText(user.role) }}
              </span>
            </td>
            <td>
              <span class="status-badge" :class="user.status === 1 ? 'active' : 'inactive'">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button class="action-btn" @click="handleEdit(user)">编辑</button>
              <button 
                class="action-btn" 
                :class="user.status === 1 ? 'warning' : 'success'" 
                @click="handleToggleStatus(user)"
              >
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="action-btn danger" @click="handleDelete(user)">删除</button>
            </td>
          </tr>
          <tr v-if="users.length === 0">
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

    <!-- 新增/编辑用户弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑用户' : '新增用户' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input 
              v-model="formData.username" 
              type="text" 
              placeholder="请输入用户名"
              :disabled="showEditModal"
            />
          </div>
          <div class="form-group" v-if="!showEditModal">
            <label>密码 <span class="required">*</span></label>
            <input v-model="formData.password" type="password" placeholder="请输入密码" />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="formData.nickname" type="text" placeholder="请输入昵称" />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input v-model="formData.phone" type="text" placeholder="请输入手机号" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="formData.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="formData.role">
              <option value="0">普通用户</option>
              <option value="1">化妆师</option>
              <option value="2">管理员</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="submit-btn" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索和筛选
const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref('')

// 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingUser = ref(null)

// 表单数据
const formData = ref({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: '0'
})

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 角色相关
const getRoleText = (role) => {
  const map = { 0: '普通用户', 1: '化妆师', 2: '管理员' }
  return map[role] || '未知'
}

const getRoleClass = (role) => {
  const map = { 0: 'user', 1: 'artist', 2: 'admin' }
  return map[role] || 'user'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchKeyword.value) {
      // 根据搜索内容判断是用户名还是手机号
      if (/^\d+$/.test(searchKeyword.value)) {
        params.phone = searchKeyword.value
      } else {
        params.username = searchKeyword.value
      }
    }
    
    if (filterRole.value !== '') {
      params.role = filterRole.value
    }
    
    if (filterStatus.value !== '') {
      params.status = parseInt(filterStatus.value)
    }
    
    const data = await adminApi.getUserList(params)
    if (data && data.records) {
      users.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    alert('获取用户列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 分页
const changePage = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 编辑用户
const handleEdit = (user) => {
  editingUser.value = user
  formData.value = {
    username: user.username,
    password: '',
    nickname: user.nickname || '',
    phone: user.phone || '',
    email: user.email || '',
    role: String(user.role)
  }
  showEditModal.value = true
}

// 切换用户状态
const handleToggleStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await adminApi.updateUserStatus(user.id, newStatus)
    user.status = newStatus
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 删除用户
const handleDelete = async (user) => {
  try {
    await adminApi.deleteUser(user.id)
    fetchUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  editingUser.value = null
  formData.value = {
    username: '',
    password: '',
    nickname: '',
    phone: '',
    email: '',
    role: '0'
  }
}

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!formData.value.username) {
    alert('请输入用户名')
    return
  }
  
  if (!showEditModal.value && !formData.value.password) {
    alert('请输入密码')
    return
  }

  try {
    submitting.value = true
    
    if (showEditModal.value) {
      // 编辑用户
      const updateData = {
        nickname: formData.value.nickname,
        phone: formData.value.phone,
        email: formData.value.email,
        role: parseInt(formData.value.role)
      }
      await adminApi.updateUser(editingUser.value.id, updateData)
      alert('编辑成功')
    } else {
      // 新增用户
      const createData = {
        username: formData.value.username,
        password: formData.value.password,
        nickname: formData.value.nickname,
        phone: formData.value.phone,
        email: formData.value.email,
        role: parseInt(formData.value.role)
      }
      await adminApi.createUser(createData)
      alert('新增成功')
    }
    
    closeModal()
    fetchUsers()
  } catch (error) {
    console.error('提交失败:', error)
    alert('操作失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchUsers()
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

.add-btn {
  padding: 10px 24px;
  background: #C1B0A1;
  color: #fff;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.add-btn:hover {
  background: #a89888;
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

/* 徽章样式 */
.role-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.role-badge.admin { background: #f3e5f5; color: #7b1fa2; }
.role-badge.user { background: #e3f2fd; color: #1565c0; }
.role-badge.artist { background: #fff3e0; color: #e65100; }

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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #333;
}

.required {
  color: #e74c3c;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #C1B0A1;
}

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
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
