<template>
  <div class="admin-page">
    <div class="card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>化妆师</th>
            <th>评分</th>
            <th>评价内容</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="review in reviews" :key="review.id">
            <td>{{ review.id }}</td>
            <td>{{ review.username }}</td>
            <td>{{ review.artistName }}</td>
            <td>⭐ {{ review.rating }}</td>
            <td class="content-cell">{{ review.content }}</td>
            <td>{{ review.createTime }}</td>
            <td>
              <button class="action-btn danger" @click="handleDelete(review)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const reviews = ref([
  { id: 1, username: '王小姐', artistName: '李梦琪', rating: 5, content: '化妆师非常专业，妆容持久一整天都没有脱妆，非常满意！', createTime: '2026-01-15' },
  { id: 2, username: '张女士', artistName: '王艺涵', rating: 5, content: '手法很温柔，妆容很自然，完全符合我想要的效果。', createTime: '2026-01-14' },
  { id: 3, username: '刘先生', artistName: '张思雨', rating: 4, content: '整体不错，就是等待时间有点长。', createTime: '2026-01-13' }
])

const handleDelete = (review) => {
  reviews.value = reviews.value.filter(r => r.id !== review.id)
}
</script>

<style scoped>
.admin-page { display: flex; flex-direction: column; gap: 20px; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th, .admin-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.admin-table th { background: #fafafa; color: #666; font-weight: 500; }
.content-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action-btn { padding: 4px 12px; font-size: 12px; border: 1px solid #ddd; background: #fff; cursor: pointer; }
.action-btn.danger { color: #e74c3c; border-color: #e74c3c; }
</style>
