<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.userCount }}</p>
          <p class="stat-label">注册用户</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💄</div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.artistCount }}</p>
          <p class="stat-label">化妆师</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.appointmentCount }}</p>
          <p class="stat-label">预约订单</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.pendingApplications }}</p>
          <p class="stat-label">待审核申请</p>
        </div>
      </div>
    </div>

    <div class="dashboard-row">
      <div class="card recent-orders">
        <div class="card-header">
          <h3>最近预约</h3>
        </div>
        <div class="card-body">
          <table class="admin-table">
            <thead>
              <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>化妆师</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td>{{ order.orderNo }}</td>
                <td>{{ order.username }}</td>
                <td>{{ order.artistName }}</td>
                <td><span class="status-badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card pending-reviews">
        <div class="card-header">
          <h3>待审核入驻申请</h3>
        </div>
        <div class="card-body">
          <div v-for="app in pendingApplications" :key="app.id" class="application-item">
            <div class="app-info">
              <p class="app-name">{{ app.realName }}</p>
              <p class="app-time">{{ app.createTime }}</p>
            </div>
            <router-link to="/admin/applications" class="btn-link">去审核</router-link>
          </div>
          <div v-if="pendingApplications.length === 0" class="empty-msg">
            暂无待审核申请
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const stats = ref({
  userCount: 128,
  artistCount: 15,
  appointmentCount: 356,
  pendingApplications: 3
})

const recentOrders = ref([
  { id: 1, orderNo: 'AP202601150001', username: '王小姐', artistName: '李梦琪', status: 2 },
  { id: 2, orderNo: 'AP202601140002', username: '张女士', artistName: '王艺涵', status: 4 },
  { id: 3, orderNo: 'AP202601130003', username: '刘先生', artistName: '张思雨', status: 1 }
])

const pendingApplications = ref([
  { id: 1, realName: '陈美美', createTime: '2026-01-15 10:30' },
  { id: 2, realName: '林小艺', createTime: '2026-01-14 15:20' }
])

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待确认', 2: '已确认', 3: '已拒绝', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'pending', 2: 'confirmed', 3: 'rejected', 4: 'completed', 5: 'cancelled' }
  return map[status] || ''
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-icon {
  font-size: 32px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.dashboard-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  font-size: 15px;
  font-weight: 500;
}

.card-body {
  padding: 16px 20px;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 13px;
}

.admin-table th {
  color: #666;
  font-weight: 500;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.pending { background: #fff3e0; color: #e65100; }
.status-badge.confirmed { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.cancelled { background: #fafafa; color: #757575; }

.application-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.application-item:last-child {
  border-bottom: none;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
}

.app-time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.btn-link {
  font-size: 13px;
  color: #C1B0A1;
}

.empty-msg {
  text-align: center;
  color: #999;
  padding: 24px;
  font-size: 13px;
}
</style>
