<template>
  <div class="admin-login">
    <div class="login-box">
      <div class="login-header">
        <h1>安颜美妆</h1>
        <p>管理后台登录</p>
      </div>
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入管理员账号" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    loading.value = true
    error.value = ''
    
    const userData = await userApi.login(form)
    
    // 检查是否为管理员 (role: 0用户 1化妆师 2管理员)
    if (userData.role !== 2) {
      error.value = '您不是管理员，无权访问'
      return
    }
    
    // 存储到管理端专用键
    userStore.setUserByPortal('admin', userData)
    router.push('/admin')
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.login-box {
  width: 400px;
  padding: 48px;
  background: #fff;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 300;
  letter-spacing: 6px;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #666;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  font-size: 14px;
  transition: border 0.2s;
}

.form-group input:focus {
  border-color: #C1B0A1;
  outline: none;
}

.error-msg {
  color: #e74c3c;
  font-size: 13px;
  text-align: center;
  margin-bottom: 16px;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: #1a1a2e;
  color: #fff;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:hover {
  background: #16213e;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
