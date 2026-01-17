<template>
  <div class="artists-page">
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">化妆师</h1>
        <p class="page-subtitle">探索专业化妆师，找到最适合您的美丽伙伴</p>
      </div>
    </div>
    
    <div class="container">
      <div class="artists-content">
        <!-- Filters -->
        <aside class="artists-filter">
          <h3 class="filter-title">筛选</h3>
          
          <div class="filter-group">
            <h4 class="filter-label">擅长领域</h4>
            <div class="filter-options">
              <label class="filter-option" v-for="cat in categories" :key="cat">
                <input type="checkbox" v-model="selectedCategories" :value="cat" />
                <span>{{ cat }}</span>
              </label>
            </div>
          </div>
          
          <div class="filter-group">
            <h4 class="filter-label">评分</h4>
            <div class="filter-options">
              <label class="filter-option">
                <input type="radio" v-model="minRating" value="4.5" />
                <span>4.5分以上</span>
              </label>
              <label class="filter-option">
                <input type="radio" v-model="minRating" value="4" />
                <span>4分以上</span>
              </label>
              <label class="filter-option">
                <input type="radio" v-model="minRating" value="0" />
                <span>全部</span>
              </label>
            </div>
          </div>
        </aside>
        
        <!-- Artists List -->
        <main class="artists-list">
          <div class="list-header">
            <span class="result-count">共 {{ artists.length }} 位化妆师</span>
          </div>
          
          <div v-if="!loading" class="artist-grid">
            <div 
              v-for="artist in artists" 
              :key="artist.id"
              class="artist-card"
              @click="goToArtist(artist.id)"
            >
              <div class="artist-card__image">
                <img :src="artist.avatar || defaultAvatar" :alt="artist.realName" />
              </div>
              <div class="artist-card__info">
                <h3 class="artist-card__name">{{ artist.realName }}</h3>
                <div class="artist-card__specialty">
                  <span 
                    v-for="(s, i) in parseSpecialties(artist)" 
                    :key="i" 
                    class="specialty-tag"
                  >{{ s }}</span>
                  <span v-if="parseSpecialties(artist).length === 0">-</span>
                </div>
                <div class="artist-card__meta">
                  <span class="artist-card__rating">★ {{ parseFloat(artist.rating || 5).toFixed(1) }}</span>
                  <span class="artist-card__exp">{{ artist.experienceYears || 0 }}年经验</span>
                </div>
                <p class="artist-card__intro">{{ artist.description || artist.introduction || '暂无简介' }}</p>
              </div>
            </div>
          </div>
          
          <div v-else class="loading">
            <div class="loading-spinner"></div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { artistApi } from '../api'

const router = useRouter()
const route = useRoute()

const defaultAvatar = 'https://via.placeholder.com/300?text=Avatar'
const loading = ref(true)
const artists = ref([])
const selectedCategories = ref([])
const minRating = ref('0')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categories = ['新娘妆', '日常妆', '晚宴妆', '古风妆', '时尚妆', '影视妆']

// 默认展示数据
const defaultArtists = [
  { id: 1, realName: '李梦琪', specialty: '新娘妆', rating: 4.9, experienceYears: 8, avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=400', introduction: '专注新娘妆容设计，打造您人生中最美的一天' },
  { id: 2, realName: '王艺涵', specialty: '日常妆', rating: 4.8, experienceYears: 5, avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400', introduction: '清新自然风格，让美丽融入日常' },
  { id: 3, realName: '张思雨', specialty: '晚宴妆', rating: 4.9, experienceYears: 10, avatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=400', introduction: '优雅精致的晚宴妆容，让您成为全场焦点' },
  { id: 4, realName: '陈依婷', specialty: '古风妆', rating: 4.7, experienceYears: 6, avatar: 'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400', introduction: '传统与现代的完美融合，展现东方美' },
  { id: 5, realName: '林雨桐', specialty: '时尚妆', rating: 4.8, experienceYears: 7, avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400', introduction: '紧跟潮流趋势，打造个性时尚造型' },
  { id: 6, realName: '周晓婉', specialty: '影视妆', rating: 4.9, experienceYears: 12, avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400', introduction: '资深影视化妆师，为您呈现专业级妆效' }
]

const fetchArtists = async () => {
  try {
    loading.value = true
    const keyword = route.query.keyword || ''
    
    if (keyword) {
      // 搜索模式
      const data = await artistApi.search(keyword, currentPage.value, pageSize.value)
      if (data && data.records && data.records.length > 0) {
        artists.value = data.records
        total.value = data.total
      } else {
        artists.value = defaultArtists
        total.value = defaultArtists.length
      }
    } else {
      // 列表模式
      const data = await artistApi.getList({ current: currentPage.value, pageSize: pageSize.value })
      if (data && data.records && data.records.length > 0) {
        artists.value = data.records
        total.value = data.total
      } else {
        artists.value = defaultArtists
        total.value = defaultArtists.length
      }
    }
  } catch (error) {
    console.error('获取化妆师列表失败:', error)
    artists.value = defaultArtists
    total.value = defaultArtists.length
  } finally {
    loading.value = false
  }
}

const goToArtist = (id) => {
  router.push(`/artist/${id}`)
}

// 解析擅长领域
const parseSpecialties = (artist) => {
  try {
    if (!artist) return []
    const val = artist.specialties || artist.specialty
    if (!val) return []
    // 如果是数组直接返回
    if (Array.isArray(val)) {
      return val.filter(s => s && String(s).trim())
    }
    // 转为字符串处理
    const strVal = String(val).trim()
    if (!strVal || strVal === '[]' || strVal === '[""]' || strVal === 'null') return []
    // 如果是JSON数组格式的字符串
    if (strVal.startsWith('[')) {
      try {
        const parsed = JSON.parse(strVal)
        return Array.isArray(parsed) ? parsed.filter(s => s && String(s).trim()) : []
      } catch {
        return []
      }
    }
    // 逗号分隔的字符串
    return strVal.split(',').map(s => s.trim()).filter(s => s)
  } catch (e) {
    console.error('解析擅长领域失败:', e)
    return []
  }
}

// 监听路由参数变化
watch(() => route.query.keyword, () => {
  fetchArtists()
})

onMounted(() => {
  fetchArtists()
})
</script>

<style scoped>
.artists-page {
  padding-top: 120px;
}

.page-header {
  background: var(--color-secondary-bg);
  padding: var(--spacing-3xl) 0;
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: var(--font-size-3xl);
  letter-spacing: 6px;
  margin-bottom: var(--spacing-sm);
}

.page-subtitle {
  color: var(--color-text-light);
  letter-spacing: 1px;
}

.artists-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: var(--spacing-xl);
  padding-bottom: var(--spacing-3xl);
}

/* Filter */
.artists-filter {
  position: sticky;
  top: 140px;
  height: fit-content;
}

.filter-title {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

.filter-group {
  margin-bottom: var(--spacing-lg);
}

.filter-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-sm);
  letter-spacing: 1px;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.filter-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  cursor: pointer;
  font-size: var(--font-size-sm);
}

/* Artists List */
.list-header {
  margin-bottom: var(--spacing-lg);
}

.result-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.artist-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.artist-card {
  display: flex;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all var(--transition-base);
}

.artist-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-md);
}

.artist-card__image {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.artist-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.artist-card__info {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
}

.artist-card__name {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.artist-card__specialty {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: var(--spacing-sm);
}

.specialty-tag {
  background: linear-gradient(135deg, #f8f5f2 0%, #ebe5df 100%);
  color: #6b5b4f;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  border: 1px solid #d9cfc3;
}

.artist-card__meta {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
}

.artist-card__rating {
  color: var(--color-accent);
}

.artist-card__intro {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  line-height: 1.6;
  flex: 1;
}

@media (max-width: 1024px) {
  .artist-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .artists-content {
    grid-template-columns: 1fr;
  }
  
  .artists-filter {
    position: static;
  }
}
</style>
