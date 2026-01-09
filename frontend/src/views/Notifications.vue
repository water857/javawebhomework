<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">智能通知</div>
        <div class="page-subtitle">汇总系统提醒、互动通知与社区动态，点击可标记已读。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="loadNotifications">刷新</button>
        <button class="btn btn-primary" @click="markAllRead" :disabled="notifications.length === 0">全部已读</button>
      </div>
    </div>

    <div class="info-banner">
      <div>
        <strong>未读通知：</strong>{{ unreadCount }} 条
      </div>
      <span>最近更新 {{ lastUpdated }}</span>
    </div>

    <div class="section-card">
      <div class="search-bar">
        <input v-model.trim="keyword" placeholder="搜索通知内容" />
        <select v-model="typeFilter">
          <option value="">全部类型</option>
          <option value="system">系统</option>
          <option value="like">点赞</option>
          <option value="comment">评论</option>
        </select>
      </div>

      <div v-if="filteredNotifications.length === 0" class="empty-state">
        暂无通知，参与邻里互动后会在这里收到提醒。
      </div>
      <div v-else class="card-grid">
        <div v-for="item in filteredNotifications" :key="item.id" class="card">
          <div class="card-header">
            <div class="section-title">{{ typeLabel(item.type) }}</div>
            <span class="status-pill" :class="item.isRead ? 'success' : 'warning'">
              {{ item.isRead ? '已读' : '未读' }}
            </span>
          </div>
          <p>{{ item.content }}</p>
          <div class="meta">{{ formatDate(item.createdAt || item.created_at) }}</div>
          <div class="page-actions" style="margin-top: 0.75rem;">
            <button class="btn btn-secondary" v-if="!item.isRead" @click="markRead(item)">标记已读</button>
            <button class="btn" @click="goCommunity">去邻里圈</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Notifications',
  data() {
    return {
      notifications: [],
      keyword: '',
      typeFilter: '',
      lastUpdated: ''
    }
  },
  computed: {
    unreadCount() {
      return this.notifications.filter(item => !item.isRead).length
    },
    filteredNotifications() {
      return this.notifications.filter(item => {
        const matchesType = this.typeFilter ? item.type === this.typeFilter : true
        const matchesKeyword = this.keyword
          ? (item.content || '').includes(this.keyword)
          : true
        return matchesType && matchesKeyword
      })
    }
  },
  mounted() {
    this.loadNotifications()
  },
  methods: {
    async loadNotifications() {
      try {
        const response = await axios.get('/community/notifications')
        this.notifications = response.data.data || []
      } catch (error) {
        console.error('加载通知失败:', error)
        this.notifications = [
          {
            id: 1,
            type: 'system',
            content: '欢迎体验智能社区服务平台，您可以在此查看互动提醒。',
            isRead: false,
            createdAt: new Date().toISOString()
          }
        ]
      } finally {
        this.lastUpdated = new Date().toLocaleString('zh-CN')
      }
    },
    async markRead(item) {
      try {
        await axios.post(`/community/notification/read/${item.id}`)
        item.isRead = true
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },
    markAllRead() {
      this.notifications = this.notifications.map(item => ({
        ...item,
        isRead: true
      }))
    },
    typeLabel(type) {
      if (type === 'like') return '点赞通知'
      if (type === 'comment') return '评论通知'
      return '系统通知'
    },
    formatDate(dateValue) {
      if (!dateValue) return ''
      const date = new Date(dateValue)
      if (Number.isNaN(date.getTime())) return ''
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    goCommunity() {
      this.$router.push('/resident/community')
    }
  }
}
</script>

<style scoped>
.meta {
  color: #6b7280;
  margin-top: 0.5rem;
  font-size: 0.85rem;
}
</style>
