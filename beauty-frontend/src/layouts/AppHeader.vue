<template>
  <header class="header" :class="{ 'header--scrolled': isScrolled }">
    <!-- 顶部功能栏 -->
    <div class="header-top">
      <div class="container header-top__inner">
        <div class="header-top__left">
          <span class="header-top__slogan">预约专业化妆师，尽享美丽时刻</span>
        </div>
        <div class="header-top__right">
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/login" class="header-top__link">登录</router-link>
            <span class="header-top__divider">|</span>
            <router-link to="/register" class="header-top__link">注册</router-link>
          </template>
          <template v-else>
            <router-link to="/profile" class="header-top__link">{{ userStore.user?.nickname || '个人中心' }}</router-link>
            <span class="header-top__divider">|</span>
            <a href="#" class="header-top__link" @click.prevent="handleLogout">退出</a>
          </template>
        </div>
      </div>
    </div>
    
    <!-- 主导航 -->
    <nav class="header-nav">
      <div class="container header-nav__inner">
        <router-link to="/" class="header-logo">
          <span class="header-logo__text">安颜美妆</span>
          <span class="header-logo__sub">ANYAN BEAUTY</span>
        </router-link>
        
        <ul class="header-menu">
          <li class="header-menu__item">
            <router-link to="/" class="header-menu__link">首页</router-link>
          </li>
          <li class="header-menu__item">
            <router-link to="/artists" class="header-menu__link">化妆师</router-link>
          </li>
          <li v-if="userStore.isLoggedIn" class="header-menu__item">
            <router-link to="/my/appointments" class="header-menu__link">我的预约</router-link>
          </li>
          <li v-if="userStore.isLoggedIn" class="header-menu__item">
            <router-link to="/my/favorites" class="header-menu__link">我的收藏</router-link>
          </li>
        </ul>
        
        <div class="header-actions">
          <button class="header-search" @click="toggleSearch">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
          </button>
        </div>
      </div>
    </nav>
    
    <!-- 搜索遮罩 -->
    <transition name="fade">
      <div v-if="showSearch" class="search-overlay" @click.self="toggleSearch">
        <div class="search-box">
          <input 
            v-model="searchKeyword"
            type="text" 
            class="search-input" 
            placeholder="搜索化妆师..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const scrolled = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')

// 判断是否在首页
const isHomePage = computed(() => route.path === '/')

// 非首页时强制使用滚动后的样式（深色文字）
const isScrolled = computed(() => {
  if (!isHomePage.value) {
    return true // 非首页始终显示深色样式
  }
  return scrolled.value
})

const handleScroll = () => {
  scrolled.value = window.scrollY > 50
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/artists', query: { keyword: searchKeyword.value } })
    showSearch.value = false
    searchKeyword.value = ''
  }
}

const handleLogout = async () => {
  userStore.logoutByPortal('user')
  userStore.logout() // 同时清除响应式状态
  router.push('/')
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: transparent;
  transition: all var(--transition-base);
}

.header--scrolled {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: var(--shadow-sm);
}

.header--scrolled .header-top {
  display: none;
}

.header--scrolled .header-menu__link,
.header--scrolled .header-logo__text {
  color: var(--color-primary);
}

/* 顶部功能栏 */
.header-top {
  background: rgba(0, 0, 0, 0.3);
  padding: 8px 0;
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.8);
}

.header-top__inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-top__right {
  display: flex;
  align-items: center;
}

.header-top__link {
  color: rgba(255, 255, 255, 0.9);
}

.header-top__link:hover {
  color: var(--color-accent);
}

.header-top__divider {
  margin: 0 12px;
  opacity: 0.5;
}

/* 主导航 */
.header-nav {
  padding: 20px 0;
}

.header-nav__inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Logo */
.header-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
}

.header-logo__text {
  font-size: var(--font-size-xl);
  font-weight: 300;
  letter-spacing: 6px;
  color: var(--color-background);
  transition: color var(--transition-base);
}

.header-logo__sub {
  font-size: 10px;
  letter-spacing: 3px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2px;
}

.header--scrolled .header-logo__sub {
  color: var(--color-text-light);
}

/* Menu */
.header-menu {
  display: flex;
  gap: var(--spacing-xl);
}

.header-menu__link {
  font-size: var(--font-size-sm);
  letter-spacing: 2px;
  color: var(--color-background);
  position: relative;
  transition: color var(--transition-base);
}

.header-menu__link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-accent);
  transition: width var(--transition-base);
}

.header-menu__link:hover::after,
.header-menu__link.router-link-active::after {
  width: 100%;
}

.header-menu__link--highlight {
  color: var(--color-accent) !important;
  font-weight: 500;
}

.header--scrolled .header-menu__link--highlight {
  color: var(--color-accent) !important;
}

/* Actions */
.header-actions {
  display: flex;
  align-items: center;
}

.header-search {
  color: var(--color-background);
  transition: color var(--transition-base);
}

.header--scrolled .header-search {
  color: var(--color-primary);
}

.header-search:hover {
  color: var(--color-accent);
}

/* Search Overlay */
.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.search-box {
  width: 100%;
  max-width: 600px;
  display: flex;
  gap: var(--spacing-sm);
  padding: 0 var(--spacing-lg);
}

.search-input {
  flex: 1;
  padding: 16px 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: transparent;
  color: var(--color-background);
  font-size: var(--font-size-lg);
  letter-spacing: 1px;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-btn {
  padding: 16px 32px;
  background: var(--color-accent);
  color: var(--color-background);
  font-size: var(--font-size-sm);
  letter-spacing: 2px;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .header-menu {
    display: none;
  }
  
  .header-logo__text {
    font-size: var(--font-size-lg);
  }
}
</style>
