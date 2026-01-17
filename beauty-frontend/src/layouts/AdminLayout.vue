<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-logo">安颜美妆</h1>
        <p class="sidebar-sub">管理后台</p>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item" exact-active-class="active">
          <span class="nav-icon">📊</span>
          仪表盘
        </router-link>
        <router-link to="/admin/users" class="nav-item" active-class="active">
          <span class="nav-icon">👥</span>
          用户管理
        </router-link>
        <router-link to="/admin/artists" class="nav-item" active-class="active">
          <span class="nav-icon">💄</span>
          化妆师管理
        </router-link>
        <router-link to="/admin/applications" class="nav-item" active-class="active">
          <span class="nav-icon">📝</span>
          入驻审核
        </router-link>
        <router-link to="/admin/appointments" class="nav-item" active-class="active">
          <span class="nav-icon">📅</span>
          预约管理
        </router-link>
        <router-link to="/admin/reviews" class="nav-item" active-class="active">
          <span class="nav-icon">⭐</span>
          评价管理
        </router-link>
        <router-link to="/admin/services" class="nav-item" active-class="active">
          <span class="nav-icon">📦</span>
          服务套餐管理
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-user">
          <span>管理员：{{ adminUser?.username || 'Admin' }}</span>
        </div>
      </header>
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 从管理端存储键读取用户信息
const adminUser = computed(() => userStore.getUserByPortal('admin'))

const titleMap = {
  '/admin': '仪表盘',
  '/admin/users': '用户管理',
  '/admin/artists': '化妆师管理',
  '/admin/applications': '入驻审核',
  '/admin/appointments': '预约管理',
  '/admin/reviews': '评价管理',
  '/admin/services': '服务套餐管理'
}

const currentTitle = computed(() => titleMap[route.path] || '管理后台')

const handleLogout = () => {
  userStore.logoutByPortal('admin')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* Sidebar */
.admin-sidebar {
  width: 240px;
  background: #1a1a2e;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.sidebar-logo {
  font-size: 20px;
  font-weight: 300;
  letter-spacing: 4px;
}

.sidebar-sub {
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  margin-top: 4px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  color: rgba(255,255,255,0.7);
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}

.nav-item.active {
  background: rgba(193, 176, 161, 0.2);
  color: #C1B0A1;
  border-left: 3px solid #C1B0A1;
}

.nav-icon {
  font-size: 18px;
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

/* Main */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.header-title {
  font-size: 18px;
  font-weight: 500;
}

.header-user {
  font-size: 14px;
  color: #666;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
