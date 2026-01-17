<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-slides">
        <div 
          v-for="(slide, index) in slides" 
          :key="index"
          class="hero-slide"
          :class="{ active: currentSlide === index }"
          :style="{ backgroundImage: `url(${slide.image})` }"
        >
          <div class="hero-overlay"></div>
          <div class="hero-content container">
            <h1 class="hero-title">{{ slide.title }}</h1>
            <p class="hero-subtitle">{{ slide.subtitle }}</p>
            <router-link :to="slide.link" class="btn-ghost btn-ghost--white">
              {{ slide.buttonText }}
            </router-link>
          </div>
        </div>
      </div>
      
      <!-- Slider Indicators -->
      <div class="hero-indicators">
        <button 
          v-for="(slide, index) in slides" 
          :key="index"
          class="hero-indicator"
          :class="{ active: currentSlide === index }"
          @click="currentSlide = index"
        ></button>
      </div>
      
      <!-- Search Bar -->
      <div class="hero-search">
        <div class="container">
          <div class="search-wrapper">
            <p class="search-label">搜索您心仪的化妆师</p>
            <div class="search-bar">
              <input 
                v-model="searchKeyword"
                type="text" 
                class="search-input" 
                placeholder="输入化妆师名称或服务类型..."
                @keyup.enter="handleSearch"
              />
              <button class="search-button" @click="handleSearch">搜索</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Artists Showcase -->
    <section class="section showcase">
      <div class="container">
        <h2 class="section-title">推荐化妆师</h2>
        <p class="section-subtitle">发现专业、优秀的化妆师</p>
        
        <div class="artist-grid" v-if="!loading">
          <div 
            v-for="artist in recommendArtists" 
            :key="artist.id"
            class="artist-item"
            @click="goToArtist(artist.id)"
          >
            <div class="avatar-circle avatar-large">
              <img :src="artist.avatar || defaultAvatar" :alt="artist.realName" />
            </div>
            <h3 class="artist-name">{{ artist.realName }}</h3>
            <p class="artist-specialty">{{ getFirstSpecialty(artist) }}</p>
            <div class="artist-rating">
              <span class="rating-stars">★ {{ artist.rating || 5 }}</span>
            </div>
          </div>
        </div>
        
        <div v-else class="loading">
          <div class="loading-spinner"></div>
        </div>
        
        <div class="showcase-action">
          <router-link to="/artists" class="btn-ghost">
            查看全部化妆师
          </router-link>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="section features">
      <div class="container">
        <div class="feature-grid">
          <div class="feature-item">
            <div class="feature-icon">✨</div>
            <h3 class="feature-title">专业认证</h3>
            <p class="feature-desc">所有化妆师均经过严格审核，持证上岗</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <h3 class="feature-title">精准匹配</h3>
            <p class="feature-desc">根据您的需求和风格，推荐最适合的化妆师</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🛡️</div>
            <h3 class="feature-title">安心保障</h3>
            <p class="feature-desc">订单保障、隐私保护，让您安心预约</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">💬</div>
            <h3 class="feature-title">真实评价</h3>
            <p class="feature-desc">查看真实用户评价，做出明智选择</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { artistApi } from '../api'

const router = useRouter()

const defaultAvatar = 'https://via.placeholder.com/200?text=Avatar'
const loading = ref(true)
const searchKeyword = ref('')
const recommendArtists = ref([])
const currentSlide = ref(0)

const slides = [
  {
    image: 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=1920',
    title: '发现您的专属美丽',
    subtitle: '预约专业化妆师，开启美丽之旅',
    buttonText: '立即探索',
    link: '/artists'
  },
  {
    image: 'https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?w=1920',
    title: '专业团队',
    subtitle: '汇聚行业顶尖化妆师，为您打造完美妆容',
    buttonText: '查看化妆师',
    link: '/artists'
  },
  {
    image: 'https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=1920',
    title: '品质服务',
    subtitle: '从预约到完成，全程贴心服务',
    buttonText: '了解更多',
    link: '/artists'
  }
]

let slideInterval = null

const fetchRecommendArtists = async () => {
  try {
    loading.value = true
    // 调用后端API获取推荐化妆师
    const data = await artistApi.getRecommend(6)
    if (data && data.length > 0) {
      recommendArtists.value = data
    } else {
      // 如果后端没有数据，使用默认展示数据
      recommendArtists.value = [
        { id: 1, realName: '李梦琪', specialty: '新娘妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=200' },
        { id: 2, realName: '王艺涵', specialty: '日常妆', rating: 4.8, avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200' },
        { id: 3, realName: '张思雨', specialty: '晚宴妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200' },
        { id: 4, realName: '陈依婷', specialty: '古风妆', rating: 4.7, avatar: 'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200' },
        { id: 5, realName: '林雨桐', specialty: '时尚妆', rating: 4.8, avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200' },
        { id: 6, realName: '周晓婉', specialty: '影视妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200' }
      ]
    }
  } catch (error) {
    console.error('获取推荐化妆师失败:', error)
    // API调用失败时使用默认数据
    recommendArtists.value = [
      { id: 1, realName: '李梦琪', specialty: '新娘妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b647?w=200' },
      { id: 2, realName: '王艺涵', specialty: '日常妆', rating: 4.8, avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200' },
      { id: 3, realName: '张思雨', specialty: '晚宴妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200' },
      { id: 4, realName: '陈依婷', specialty: '古风妆', rating: 4.7, avatar: 'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200' },
      { id: 5, realName: '林雨桐', specialty: '时尚妆', rating: 4.8, avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200' },
      { id: 6, realName: '周晓婉', specialty: '影视妆', rating: 4.9, avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200' }
    ]
  } finally {
    loading.value = false
  }
}

const getFirstSpecialty = (artist) => {
  // 处理 specialties 数组
  if (artist.specialties && Array.isArray(artist.specialties) && artist.specialties.length > 0) {
    return artist.specialties[0]
  }
  // 处理 specialty 字符串
  if (artist.specialty) {
    return artist.specialty
  }
  // 尝试解析 JSON 字符串格式的 specialties
  if (typeof artist.specialties === 'string') {
    try {
      const parsed = JSON.parse(artist.specialties)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed[0]
      }
    } catch (e) {
      return artist.specialties
    }
  }
  return ''
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/artists', query: { keyword: searchKeyword.value } })
  }
}

const goToArtist = (id) => {
  router.push(`/artist/${id}`)
}

const startSlider = () => {
  slideInterval = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % slides.length
  }, 5000)
}

onMounted(() => {
  fetchRecommendArtists()
  startSlider()
})

onUnmounted(() => {
  if (slideInterval) {
    clearInterval(slideInterval)
  }
})
</script>

<style scoped>
/* Hero Section */
.hero {
  position: relative;
  height: 100vh;
  min-height: 700px;
}

.hero-slides {
  position: absolute;
  inset: 0;
}

.hero-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1s ease;
}

.hero-slide.active {
  opacity: 1;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: var(--gradient-overlay);
}

.hero-content {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  color: var(--color-background);
  padding-top: 100px;
}

.hero-title {
  font-size: var(--font-size-4xl);
  font-weight: 300;
  letter-spacing: 8px;
  margin-bottom: var(--spacing-md);
  color: var(--color-background);
}

.hero-subtitle {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xl);
  opacity: 0.9;
}

/* Hero Indicators */
.hero-indicators {
  position: absolute;
  bottom: 180px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--spacing-sm);
}

.hero-indicator {
  width: 40px;
  height: 3px;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all var(--transition-base);
}

.hero-indicator.active {
  background: var(--color-background);
  width: 60px;
}

/* Hero Search */
.hero-search {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  padding: var(--spacing-xl) 0;
}

.search-wrapper {
  text-align: center;
}

.search-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-sm);
  letter-spacing: 1px;
}

.search-bar {
  display: flex;
  max-width: 700px;
  margin: 0 auto;
}

.search-bar .search-input {
  flex: 1;
  padding: 16px 24px;
  border: 1px solid var(--color-border);
  border-right: none;
  font-size: var(--font-size-base);
}

.search-button {
  padding: 16px 48px;
  background: var(--color-accent);
  color: var(--color-background);
  font-size: var(--font-size-sm);
  letter-spacing: 2px;
  transition: background var(--transition-fast);
}

.search-button:hover {
  background: var(--color-accent-hover);
}

/* Showcase */
.showcase {
  text-align: center;
}

.section-subtitle {
  color: var(--color-text-light);
  margin-top: calc(-1 * var(--spacing-md));
  margin-bottom: var(--spacing-xl);
  letter-spacing: 1px;
}

.artist-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

.artist-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform var(--transition-base);
}

.artist-item:hover {
  transform: translateY(-8px);
}

.avatar-large {
  width: 140px;
  height: 140px;
  margin-bottom: var(--spacing-md);
}

.artist-name {
  font-size: var(--font-size-base);
  font-weight: 400;
  letter-spacing: 2px;
  margin-bottom: var(--spacing-xs);
}

.artist-specialty {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  margin-bottom: var(--spacing-xs);
}

.artist-rating {
  color: var(--color-accent);
  font-size: var(--font-size-sm);
}

.showcase-action {
  margin-top: var(--spacing-lg);
}

/* Features */
.features {
  background: var(--color-secondary-bg);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-xl);
}

.feature-item {
  text-align: center;
  padding: var(--spacing-xl);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

.feature-title {
  font-size: var(--font-size-lg);
  letter-spacing: 2px;
  margin-bottom: var(--spacing-sm);
}

.feature-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-light);
  line-height: 1.8;
}

/* Responsive */
@media (max-width: 1024px) {
  .artist-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: var(--font-size-2xl);
  }
  
  .artist-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .search-bar {
    flex-direction: column;
  }
  
  .search-bar .search-input {
    border-right: 1px solid var(--color-border);
    border-bottom: none;
  }
}
</style>
