<template>
  <div class="page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">我的收藏</h1>
        <p class="page-subtitle">您收藏的化妆师和服务套餐</p>
      </div>
    </div>

    <div class="container">
      <!-- Tabs -->
      <div class="tabs">
        <button 
          class="tab-btn"
          :class="{ active: currentTab === 1 }"
          @click="currentTab = 1"
        >
          化妆师
        </button>
        <button 
          class="tab-btn"
          :class="{ active: currentTab === 2 }"
          @click="currentTab = 2"
        >
          服务套餐
        </button>
      </div>

      <!-- Favorites List -->
      <div v-if="!loading" class="favorites-content">
        <!-- Artists -->
        <div v-if="currentTab === 1" class="favorites-grid">
          <div v-if="favorites.length > 0">
            <div v-for="item in favorites" :key="item.id" class="favorite-card" @click="goToArtist(item.targetId)">
              <div class="favorite-image">
                <img :src="item.targetImage || defaultAvatar" :alt="item.targetName" />
              </div>
              <div class="favorite-info">
                <h3 class="favorite-name">{{ item.targetName }}</h3>
                <span class="favorite-time">收藏于 {{ formatDate(item.createTime) }}</span>
              </div>
              <button class="remove-btn" @click.stop="removeFavorite(item)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>
          <div v-else class="empty-state">
            <p>暂无收藏的化妆师</p>
            <router-link to="/artists" class="btn-ghost">去发现</router-link>
          </div>
        </div>

        <!-- Services -->
        <div v-else class="favorites-grid">
          <div v-if="favorites.length > 0">
            <div v-for="item in favorites" :key="item.id" class="favorite-card service-card">
              <div class="favorite-image">
                <img :src="item.targetImage || 'https://via.placeholder.com/200'" :alt="item.targetName" />
              </div>
              <div class="favorite-info">
                <h3 class="favorite-name">{{ item.targetName }}</h3>
                <span class="favorite-time">收藏于 {{ formatDate(item.createTime) }}</span>
              </div>
              <button class="remove-btn" @click.stop="removeFavorite(item)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>
          <div v-else class="empty-state">
            <p>暂无收藏的服务套餐</p>
            <router-link to="/artists" class="btn-ghost">去发现</router-link>
          </div>
        </div>
      </div>

      <div v-else class="loading">
        <div class="loading-spinner"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi } from '../api'

const router = useRouter()
const defaultAvatar = 'https://via.placeholder.com/100?text=Avatar'

const loading = ref(true)
const currentTab = ref(1)
const favorites = ref([])

// Mock data
const mockArtistFavorites = [
  { id: 1, targetType: 1, targetId: 1, targetName: '李梦琪', targetImage: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=200', createTime: '2026-01-10' },
  { id: 2, targetType: 1, targetId: 2, targetName: '王艺涵', targetImage: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200', createTime: '2026-01-08' }
]

const mockServiceFavorites = [
  { id: 3, targetType: 2, targetId: 1, targetName: '新娘跟妆', targetImage: 'https://images.unsplash.com/photo-1596704017254-9b121068fb31?w=200', createTime: '2026-01-12' }
]

const fetchFavorites = async () => {
  try {
    loading.value = true
    const data = await favoriteApi.getMyList({ targetType: currentTab.value, current: 1, pageSize: 20 })
    if (data && data.records && data.records.length > 0) {
      favorites.value = data.records
    } else {
      favorites.value = currentTab.value === 1 ? mockArtistFavorites : mockServiceFavorites
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    favorites.value = currentTab.value === 1 ? mockArtistFavorites : mockServiceFavorites
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (item) => {
  if (!confirm('确定要取消收藏吗？')) return
  try {
    if (item.targetType === 1) {
      await favoriteApi.removeArtist(item.targetId)
    } else {
      await favoriteApi.removeService(item.targetId)
    }
    favorites.value = favorites.value.filter(f => f.id !== item.id)
  } catch (error) {
    console.error('取消收藏失败:', error)
  }
}

const goToArtist = (id) => {
  router.push(`/artist/${id}`)
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

watch(currentTab, () => {
  fetchFavorites()
})

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.page {
  padding-top: 100px;
  padding-bottom: var(--spacing-3xl);
  min-height: 100vh;
}

.page-header {
  background: var(--color-secondary-bg);
  padding: var(--spacing-xl) 0;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: var(--font-size-2xl);
  letter-spacing: 4px;
  margin-bottom: var(--spacing-xs);
}

.page-subtitle {
  color: var(--color-text-light);
  letter-spacing: 1px;
}

/* Tabs */
.tabs {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--spacing-sm);
}

.tab-btn {
  padding: var(--spacing-sm) var(--spacing-lg);
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  letter-spacing: 2px;
  transition: all var(--transition-fast);
}

.tab-btn:hover,
.tab-btn.active {
  color: var(--color-primary);
}

.tab-btn.active {
  border-bottom: 2px solid var(--color-accent);
}

/* Favorites Grid */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: var(--spacing-md);
}

.favorite-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-md) var(--spacing-lg);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all var(--transition-base);
}

.favorite-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-sm);
}

.favorite-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.service-card .favorite-image {
  border-radius: 8px;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-info {
  flex: 1;
}

.favorite-name {
  font-size: var(--font-size-base);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.favorite-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-light);
}

.remove-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-light);
  transition: color var(--transition-fast);
}

.remove-btn:hover {
  color: #e74c3c;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--color-text-light);
}

.empty-state .btn-ghost {
  margin-top: var(--spacing-md);
}
</style>
