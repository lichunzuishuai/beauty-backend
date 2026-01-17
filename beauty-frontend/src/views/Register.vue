<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-form-wrapper">
        <div class="auth-brand">
          <h1 class="auth-logo">安颜美妆</h1>
          <p class="auth-logo-sub">ANYAN BEAUTY</p>
        </div>
        
        <h2 class="auth-title">注册</h2>
        
        <form class="auth-form" @submit.prevent="handleRegister">
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
            <label class="form-label">手机号</label>
            <input 
              v-model="form.phone"
              type="tel" 
              class="form-input" 
              placeholder="请输入手机号（可选）"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">密码</label>
            <input 
              v-model="form.password"
              type="password" 
              class="form-input" 
              placeholder="请输入密码（至少6位）"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input 
              v-model="form.confirmPassword"
              type="password" 
              class="form-input" 
              placeholder="请再次输入密码"
              required
            />
          </div>
          
          <p v-if="error" class="form-error">{{ error }}</p>
          
          <button type="submit" class="btn-solid auth-submit" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>
        
        <p class="auth-switch">
          已有账号？ 
          <router-link to="/login">立即登录</router-link>
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
import { useRouter } from 'vue-router'
import { userApi } from '../api'

const router = useRouter()

const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  
  if (form.password.length < 6) {
    error.value = '密码长度至少6位'
    return
  }
  
  try {
    loading.value = true
    error.value = ''
    
    await userApi.register(form)
    router.push('/login')
  } catch (e) {
    error.value = e.message || '注册失败，请稍后重试'
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
  background: url('https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=1200') center/cover;
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
