<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-main">
        <div class="footer-brand">
          <h3 class="footer-brand__name">安颜美妆</h3>
          <p class="footer-brand__sub">ANYAN BEAUTY</p>
          <p class="footer-brand__slogan">预约专业化妆师，尽享美丽时刻</p>
        </div>
        
        <div class="footer-links">
          <div class="footer-column">
            <h4 class="footer-column__title">探索</h4>
            <ul class="footer-column__list">
              <li><router-link to="/">首页</router-link></li>
              <li><router-link to="/artists">化妆师</router-link></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4 class="footer-column__title">个人</h4>
            <ul class="footer-column__list">
              <li><router-link to="/my/appointments">我的预约</router-link></li>
              <li><router-link to="/my/favorites">我的收藏</router-link></li>
              <li><router-link to="/profile">个人中心</router-link></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4 class="footer-column__title">支持</h4>
            <ul class="footer-column__list">
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">服务条款</a></li>
            </ul>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p class="footer-copyright">
          © {{ currentYear }} 安颜美妆 ANYAN BEAUTY. All rights reserved.
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--color-primary);
  color: var(--color-background);
  padding: var(--spacing-3xl) 0 var(--spacing-lg);
}

.footer-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--spacing-3xl);
  padding-bottom: var(--spacing-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* Brand */
.footer-brand__name {
  font-size: var(--font-size-xl);
  font-weight: 300;
  letter-spacing: 6px;
  margin-bottom: var(--spacing-xs);
}

.footer-brand__sub {
  font-size: var(--font-size-xs);
  letter-spacing: 3px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: var(--spacing-md);
}

.footer-brand__slogan {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.7);
  max-width: 300px;
  line-height: 1.8;
}

/* Links */
.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-xl);
}

.footer-column__title {
  font-size: var(--font-size-sm);
  font-weight: 400;
  letter-spacing: 2px;
  margin-bottom: var(--spacing-md);
  color: var(--color-accent);
}

.footer-column__list li {
  margin-bottom: var(--spacing-sm);
}

.footer-column__list a {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.7);
  transition: color var(--transition-fast);
}

.footer-column__list a:hover {
  color: var(--color-background);
}

/* Bottom */
.footer-bottom {
  padding-top: var(--spacing-lg);
  text-align: center;
}

.footer-copyright {
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 1px;
}

/* Responsive */
@media (max-width: 768px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: var(--spacing-xl);
  }
  
  .footer-links {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
