<template>
  <div class="artist-login">
    <div class="login-box">
      <div class="login-header">
        <h1>安颜美妆</h1>
        <p>化妆师工作台登录</p>
      </div>
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入化妆师账号" required />
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
      <p class="back-link">
        <router-link to="/">返回首页</router-link>
      </p>
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
    
    // 检查是否为化妆师或管理员 (role: 0用户 1化妆师 2管理员)
    if (userData.role !== 1 && userData.role !== 2) {
      error.value = '此账号不是化妆师，无权访问工作台'
      return
    }
    
    // 存储到化妆师端专用键
    userStore.setUserByPortal('artist', userData)
    router.push('/artist')
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.artist-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);
}

.login-box {
  width: 400px;
  padding: 48px;
  background: #fff;
  border-radius: 8px;
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
  border-radius: 4px;
  font-size: 14px;
  transition: border 0.2s;
  box-sizing: border-box;
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
  background: #2c3e50;
  color: #fff;
  font-size: 14px;
  letter-spacing: 2px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:hover {
  background: #1a252f;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-link {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
}

.back-link a {
  color: #666;
  text-decoration: none;
}

.back-link a:hover {
  color: #C1B0A1;
}
</style>
