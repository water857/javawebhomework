<template>
  <div class="container">
    <header>
      <h1>居民车位申请</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="refreshAll">刷新</button>
      </div>
    </header>

    <div class="section-grid">
      <div class="panel">
        <div class="panel-header">
          <h2>车位列表</h2>
          <p class="muted">选择空闲车位后提交申请。</p>
        </div>
        <div class="grid">
          <div v-for="space in spaces" :key="space.id" class="card">
            <div class="card-header">
              <h3>{{ space.code }}</h3>
              <span class="badge" :class="statusClass(space.status)">{{ statusText(space.status) }}</span>
            </div>
            <div class="card-actions">
              <button class="btn" :disabled="space.status !== 'available'" @click="apply(space.id)">
                {{ space.status === 'available' ? '申请' : '不可申请' }}
              </button>
            </div>
          </div>
          <div v-if="spaces.length === 0" class="empty">暂无车位数据。</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>我的申请</h2>
          <p class="muted">查看当前申请进度与状态。</p>
        </div>
        <div v-if="applications.length === 0" class="empty">暂无申请记录。</div>
        <div v-else class="list">
          <div v-for="app in applications" :key="app.id" class="list-item">
            <div>
              <div class="title">申请编号：{{ app.id }}</div>
              <div class="subtitle">车位：{{ app.parkingId }}</div>
            </div>
            <span class="badge" :class="applicationStatusClass(app.status)">
              {{ applicationStatusText(app.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentParking',
  data() {
    return {
      spaces: [],
      applications: []
    }
  },
  mounted() {
    this.loadSpaces()
    this.loadApplications()
  },
  methods: {
    handleBack() {
      this.$router.push('/resident')
    },
    refreshAll() {
      this.loadSpaces()
      this.loadApplications()
    },
    statusText(status) {
      if (status === 'available') return '空闲'
      if (status === 'occupied') return '已占'
      if (status === 'pending') return '申请中'
      return status || '未知'
    },
    statusClass(status) {
      if (status === 'available') return 'badge-success'
      if (status === 'pending') return 'badge-warning'
      return 'badge-muted'
    },
    applicationStatusText(status) {
      if (status === 'pending') return '审核中'
      if (status === 'approved') return '已通过'
      if (status === 'rejected') return '已拒绝'
      return status || '未知'
    },
    applicationStatusClass(status) {
      if (status === 'approved') return 'badge-success'
      if (status === 'rejected') return 'badge-danger'
      return 'badge-warning'
    },
    async loadSpaces() {
      try {
        const response = await axios.get('/parking/space/list')
        this.spaces = response.data.data || []
      } catch (error) {
        console.error('加载车位失败:', error)
      }
    },
    async loadApplications() {
      try {
        const response = await axios.get('/parking/application/list')
        this.applications = response.data.data || []
      } catch (error) {
        console.error('加载申请失败:', error)
      }
    },
    async apply(parkingId) {
      try {
        await axios.post('/parking/application', { parkingId })
        this.loadApplications()
      } catch (error) {
        console.error('申请失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
}
.section-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.panel {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}
.panel-header {
  margin-bottom: 16px;
}
.muted {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.card {
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 12px 14px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-actions {
  display: flex;
  justify-content: flex-end;
}
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid transparent;
}
.badge-success {
  background: #e6f7ff;
  color: #1677ff;
  border-color: #91d5ff;
}
.badge-warning {
  background: #fff7e6;
  color: #d46b08;
  border-color: #ffd591;
}
.badge-danger {
  background: #fff1f0;
  color: #cf1322;
  border-color: #ffa39e;
}
.badge-muted {
  background: #f5f5f5;
  color: #666;
  border-color: #d9d9d9;
}
.list {
  margin-top: 12px;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #eee;
}
.title {
  font-weight: 600;
}
.subtitle {
  color: #666;
  font-size: 14px;
}
.empty {
  color: #888;
  padding: 12px;
}
</style>
