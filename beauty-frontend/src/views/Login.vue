<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-form-wrapper">
        <div class="auth-brand">
          <h1 class="auth-logo">安颜美妆</h1>
          <p class="auth-logo-sub">ANYAN BEAUTY</p>
        </div>
        
        <h2 class="auth-title">登录</h2>
        
        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input 
              v-model="form.username"
              type="text" 
              class="form-input" 
              placeholder="请输入用户名"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">密码</label>
            <input 
              v-model="form.password"
              type="password" 
              class="form-input" 
              placeholder="请输入密码"
              required
            />
          </div>
          
          <p v-if="error" class="form-error">{{ error }}</p>
          
          <button type="submit" class="btn-solid auth-submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        
        <p class="auth-switch">
          还没有账号？ 
          <router-link to="/register">立即注册</router-link>
        </p>
      </div>
      
      <div class="auth-image">
        <div class="auth-image-overlay"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userApi } from '../api'

const router = useRouter()
const route = useRoute()
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
    
    // 存储到用户端专用键
    userStore.setUserByPortal('user', userData)
    // 同时更新响应式状态（向后兼容）
    userStore.setUser(userData)
    
    // 如果有重定向地址，优先使用
    if (route.query.redirect) {
      router.push(route.query.redirect)
      return
    }
    
    // 用户端登录统一跳转到首页
    router.push('/')
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
}

.auth-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

.auth-form-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--spacing-3xl);
  max-width: 480px;
  margin: 0 auto;
}

.auth-brand {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-logo {
  font-size: var(--font-size-2xl);
  font-weight: 300;
  letter-spacing: 6px;
}

.auth-logo-sub {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
  letter-spacing: 3px;
  margin-top: 4px;
}

.auth-title {
  font-size: var(--font-size-xl);
  letter-spacing: 4px;
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-form {
  margin-bottom: var(--spacing-lg);
}

.form-error {
  color: #e74c3c;
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-md);
  text-align: center;
}

.auth-submit {
  width: 100%;
  padding: 16px;
}

.auth-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-switch {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.auth-switch a {
  color: var(--color-accent);
  font-weight: 500;
}

.auth-image {
  background: url('https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=1200') center/cover;
  position: relative;
}

.auth-image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.3), rgba(0,0,0,0.5));
}

@media (max-width: 768px) {
  .auth-container {
    grid-template-columns: 1fr;
  }
  
  .auth-image {
    display: none;
  }
}
</style>
