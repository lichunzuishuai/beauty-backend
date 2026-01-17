<template>
  <div class="artist-layout">
    <aside class="sidebar">
      <div class="logo">
        <h2>化妆师工作台</h2>
      </div>
      <nav class="nav-menu">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path" class="nav-item" active-class="active">
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.name }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="status-toggle">
          <span class="status-label">工作状态</span>
          <button class="status-btn" :class="{ working: isWorking }" @click="toggleStatus">
            {{ isWorking ? '🟢 工作中' : '🔴 休息中' }}
          </button>
        </div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </aside>
    <main class="main-content">
      <header class="header">
        <h1 class="page-title">{{ currentTitle }}</h1>
        <div class="user-info">
          <span class="user-name">{{ artistName }}</span>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { artistPortalApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isWorking = ref(true)
const artistName = ref('')

const menuItems = [
  { path: '/artist', name: '工作台', icon: '📊' },
  { path: '/artist/orders', name: '预约管理', icon: '📋' },
  { path: '/artist/services', name: '服务套餐', icon: '💄' },
  { path: '/artist/reviews', name: '客户评价', icon: '⭐' }
]

const titleMap = {
  '/artist': '工作台',
  '/artist/orders': '预约管理',
  '/artist/services': '服务套餐',
  '/artist/reviews': '客户评价'
}

const currentTitle = computed(() => titleMap[route.path] || '化妆师工作台')

const fetchProfile = async () => {
  try {
    const data = await artistPortalApi.getProfile()
    if (data) {
      artistName.value = data.realName || '化妆师'
      isWorking.value = data.status === 1
    }
  } catch (error) {
    console.error('获取信息失败:', error)
  }
}

const toggleStatus = async () => {
  try {
    const newStatus = isWorking.value ? 0 : 1
    await artistPortalApi.setWorkStatus(newStatus)
    isWorking.value = !isWorking.value
    alert(isWorking.value ? '已切换为工作中' : '已切换为休息中')
  } catch (error) {
    console.error('切换状态失败:', error)
    alert('切换失败: ' + (error.message || '请稍后再试'))
  }
}

const handleLogout = () => {
  userStore.logoutByPortal('artist')
  router.push('/artist/login')
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.artist-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #2c3e50 0%, #1a252f 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo h2 {
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 2px;
  margin: 0;
}

.nav-menu {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.nav-item.active {
  background: rgba(193, 176, 161, 0.3);
  color: #fff;
  border-left: 3px solid #C1B0A1;
}

.nav-icon { font-size: 18px; }
.nav-text { font-size: 14px; }

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.status-toggle {
  margin-bottom: 12px;
}

.status-label {
  display: block;
  font-size: 12px;
  color: rgba(255,255,255,0.6);
  margin-bottom: 8px;
}

.status-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn.working {
  background: rgba(46, 125, 50, 0.3);
  border-color: #2e7d32;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: rgba(255,255,255,0.7);
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.user-name {
  font-size: 14px;
  color: #666;
}

.content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}
</style>
