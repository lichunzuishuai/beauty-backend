<template>
  <div id="app">
    <!-- 非管理员页面显示用户端Header和Footer -->
    <AppHeader v-if="!isAdminRoute" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter v-if="!isAdminRoute" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './layouts/AppHeader.vue'
import AppFooter from './layouts/AppFooter.vue'
import { useUserStore } from './stores/user'

const route = useRoute()
const userStore = useUserStore()

// 判断是否为管理员或化妆师工作台页面（这些页面有自己的布局，不需要用户端Header/Footer）
// 注意：/artists 是用户端化妆师列表，/artist/:id 是化妆师详情，这些需要显示Header
// 化妆师工作台路径：/artist, /artist/orders, /artist/services, /artist/reviews, /artist/login
const isArtistPortal = computed(() => {
  const path = route.path
  // 精确匹配化妆师工作台路径
  return path === '/artist' || 
         path === '/artist/login' ||
         path === '/artist/orders' || 
         path === '/artist/services' || 
         path === '/artist/reviews'
})

const isAdminRoute = computed(() => route.path.startsWith('/admin') || isArtistPortal.value)

onMounted(() => {
  userStore.init()
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>
