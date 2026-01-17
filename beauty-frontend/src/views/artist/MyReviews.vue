<template>
  <div class="page">
    <div class="card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      <div v-else class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="rating">
              <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">★</span>
            </div>
            <span class="review-time">{{ formatDate(review.createTime) }}</span>
          </div>
          <p class="review-content">{{ review.content }}</p>
          
          <div v-if="review.reply" class="reply-box">
            <span class="reply-label">您的回复：</span>
            <p class="reply-content">{{ review.reply }}</p>
          </div>
          
          <div v-else class="reply-action">
            <button v-if="replyingId !== review.id" class="btn-reply" @click="startReply(review)">回复</button>
            <div v-else class="reply-form">
              <textarea v-model="replyContent" placeholder="请输入回复内容"></textarea>
              <div class="reply-buttons">
                <button class="btn-cancel" @click="cancelReply">取消</button>
                <button class="btn-submit" @click="submitReply(review)" :disabled="submitting">回复</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="reviews.length === 0" class="empty">暂无客户评价</div>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { artistPortalApi } from '../../api'

const loading = ref(false)
const reviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const replyingId = ref(null)
const replyContent = ref('')
const submitting = ref(false)

const fetchReviews = async () => {
  try {
    loading.value = true
    const data = await artistPortalApi.getReviews({ current: currentPage.value, pageSize: pageSize.value })
    if (data && data.records) {
      reviews.value = data.records
      total.value = data.total
    }
  } catch (error) {
    console.error('获取评价失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const changePage = (page) => {
  currentPage.value = page
  fetchReviews()
}

const startReply = (review) => {
  replyingId.value = review.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingId.value = null
  replyContent.value = ''
}

const submitReply = async (review) => {
  if (!replyContent.value.trim()) {
    alert('请输入回复内容')
    return
  }
  
  try {
    submitting.value = true
    await artistPortalApi.replyReview(review.id, replyContent.value)
    alert('回复成功')
    replyingId.value = null
    replyContent.value = ''
    fetchReviews()
  } catch (error) {
    alert('回复失败: ' + (error.message || '请稍后再试'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.loading-container { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 24px; height: 24px; border: 2px solid #ddd; border-top-color: #C1B0A1; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.reviews-list { padding: 0; }

.review-item {
  padding: 24px;
  border-bottom: 1px solid #eee;
}

.review-item:last-child { border-bottom: none; }

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rating { display: flex; gap: 2px; }
.star { color: #ddd; font-size: 16px; }
.star.filled { color: #f5a623; }
.review-time { font-size: 12px; color: #999; }

.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.reply-box {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 6px;
  margin-top: 12px;
}

.reply-label {
  font-size: 12px;
  color: #999;
}

.reply-content {
  font-size: 13px;
  color: #666;
  margin: 8px 0 0 0;
}

.reply-action { margin-top: 12px; }

.btn-reply {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #C1B0A1;
  color: #C1B0A1;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-reply:hover { background: #faf8f6; }

.reply-form { display: flex; flex-direction: column; gap: 12px; }
.reply-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  box-sizing: border-box;
}

.reply-buttons { display: flex; justify-content: flex-end; gap: 12px; }
.btn-cancel { padding: 8px 16px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.btn-submit { padding: 8px 16px; background: #2c3e50; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.empty { text-align: center; padding: 60px; color: #999; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; padding: 16px; border-top: 1px solid #eee; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
