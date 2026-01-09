<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">服务商首页</div>
        <div class="page-subtitle">查看公告与天气，快速进入服务任务。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="refreshAll">刷新首页</button>
        <button class="btn btn-primary" @click="goService">进入服务预约</button>
      </div>
    </div>

    <div class="card-grid">
      <section class="section-card">
        <div class="section-title">天气与温馨提示</div>
        <div v-if="weatherTips" class="weather-block">
          <pre>{{ weatherTips }}</pre>
        </div>
        <div v-else class="empty-state">正在获取天气信息...</div>
      </section>

      <section class="section-card">
        <div class="section-title">社区公告</div>
        <div v-if="announcements.length === 0" class="empty-state">暂无公告</div>
        <div v-else class="announcement-list">
          <div
            v-for="item in announcements"
            :key="item.id"
            class="announcement-item"
            @click="showAnnouncement(item)"
          >
            <div>
              <div class="announcement-title">{{ item.title }}</div>
              <div class="announcement-meta">{{ formatDate(item.publishedAt || item.createdAt) }}</div>
            </div>
            <span class="tag">详情</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { weatherApi, announcementApi } from '../services/api'

export default {
  name: 'Provider',
  data() {
    return {
      weatherTips: '',
      announcements: []
    }
  },
  mounted() {
    this.refreshAll()
  },
  methods: {
    async refreshAll() {
      await Promise.all([this.getWeatherInfo(), this.getAnnouncements()])
    },
    async getWeatherInfo() {
      try {
        const response = await weatherApi.getWeatherTips()
        if (response && response.success) {
          this.weatherTips = response.data
        } else if (response && response.data) {
          this.weatherTips = response.data
        } else {
          this.weatherTips = ''
        }
      } catch (error) {
        console.error('获取天气信息失败:', error)
        this.weatherTips = '今日天气：晴，温度：22℃\n风向：东南，风力：2级，湿度：45%\n温馨提示：今天天气宜人，适合户外活动。'
      }
    },
    async getAnnouncements() {
      try {
        const response = await announcementApi.getLatestAnnouncements(5)
        if (response.code === 'success') {
          this.announcements = response.data || []
        }
      } catch (error) {
        console.error('获取公告失败:', error)
        this.announcements = []
      }
    },
    showAnnouncement(item) {
      alert(`公告详情\n\n标题: ${item.title}\n\n内容: ${item.content}\n\n发布者: ${item.authorName || '管理员'}\n\n发布时间: ${this.formatDate(item.publishedAt || item.createdAt)}`)
    },
    goService() {
      this.$router.push('/provider/service-management')
    },
    formatDate(dateTime) {
      if (!dateTime) return ''
      if (typeof dateTime === 'string') return dateTime
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.weather-block pre {
  white-space: pre-wrap;
  font-family: inherit;
  color: #1f2937;
  background: #f8fafc;
  padding: 1rem;
  border-radius: 12px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.announcement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  border-radius: 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  cursor: pointer;
}

.announcement-title {
  font-weight: 600;
  color: #1f2937;
}

.announcement-meta {
  color: #6b7280;
  font-size: 0.85rem;
}
</style>
