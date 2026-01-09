<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">社区公告</div>
        <div class="page-subtitle">查看最新公告与通知内容。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="fetchAnnouncements">刷新</button>
      </div>
    </div>

    <div class="section-card">
      <div class="search-bar">
        <input type="text" v-model="searchQuery" placeholder="搜索公告" class="search-input">
        <button class="btn btn-primary" @click="fetchAnnouncements">搜索</button>
      </div>

      <div class="announcements-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="announcements.length === 0" class="empty-state">暂无公告</div>
        <div v-else class="announcement-card" v-for="announcement in announcements" :key="announcement.id">
          <div @click="handleAnnouncementDetail(announcement.id)">
            <h3>{{ announcement.title }}</h3>
            <p class="announcement-content">{{ truncateContent(announcement.content) }}</p>
            <div class="announcement-info">
              <span>发布者: {{ announcement.authorName || '匿名' }}</span>
              <span>发布时间: {{ formatDateTime(announcement.publishedAt || announcement.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button class="btn btn-secondary" @click="currentPage > 1 && (currentPage--, fetchAnnouncements())" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}</span>
        <button class="btn btn-secondary" @click="currentPage < totalPages && (currentPage++, fetchAnnouncements())" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { announcementApi } from '../services/api';

export default {
  name: 'ResidentAnnouncements',
  data() {
    return {
      announcements: [],
      loading: false,
      searchQuery: '',
      currentPage: 1,
      pageSize: 10,
      totalPages: 1
    }
  },
  mounted() {
    this.fetchAnnouncements();
  },
  activated() {
    // 组件激活时自动刷新数据
    this.fetchAnnouncements();
  },
  methods: {
    async fetchAnnouncements() {
      this.loading = true;
      try {
        // 居民只能查看已发布的公告，所以statusFilter固定为1
        const response = await announcementApi.getAnnouncements(this.currentPage, this.pageSize, this.searchQuery, 1);
        if (response.code === 'success') {
          this.announcements = response.data;
          this.totalPages = response.totalPages || 1;
        } else {
          console.error('获取公告列表失败:', response.message);
        }
      } catch (error) {
        console.error('获取公告列表失败:', error);
        alert('获取公告列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handleAnnouncementDetail(announcementId) {
      const announcement = this.announcements.find(a => a.id === announcementId);
      if (announcement) {
        alert(`公告详情\n\n标题: ${announcement.title}\n\n内容: ${announcement.content}\n\n发布者: ${announcement.authorName}\n\n发布时间: ${this.formatDateTime(announcement.publishedAt || announcement.createdAt)}`);
      }
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      if (typeof dateTime === 'string') {
        return dateTime;
      }
      const date = new Date(dateTime);
      if (isNaN(date.getTime())) {
        return '';
      }
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      });
    },
    truncateContent(content) {
      if (!content) return '';
      return content.length > 100 ? content.substring(0, 100) + '...' : content;
    }
  }
}
</script>

<style scoped>
.announcement-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s ease;
}

.announcement-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.announcement-content {
  color: #4b5563;
  margin: 0.75rem 0;
}

.announcement-info {
  display: flex;
  justify-content: space-between;
  color: #6b7280;
  font-size: 0.85rem;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 1rem;
  align-items: center;
  margin-top: 1rem;
}
</style>
