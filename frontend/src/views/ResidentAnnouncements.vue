<template>
  <div class="container">
    <header>
      <h1>社区公告</h1>
      <button class="btn" @click="handleBack">返回首页</button>
    </header>

    <div class="announcements-section">
      <div class="filter-section">
        <input type="text" v-model="searchQuery" placeholder="搜索公告" class="search-input">
        <button class="btn" @click="fetchAnnouncements">搜索</button>
      </div>

      <div class="announcements-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="announcements.length === 0" class="no-data">暂无公告</div>
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
        <button class="btn" @click="currentPage > 1 && (currentPage--, fetchAnnouncements())" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}</span>
        <button class="btn" @click="currentPage < totalPages && (currentPage++, fetchAnnouncements())" :disabled="currentPage === totalPages">下一页</button>
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
      // 居民查看公告详情，这里可以跳转到详情页面，或者直接在当前页面展开详情
      // 为了简化实现，我们直接在控制台打印公告ID，并弹出一个alert显示公告内容
      // 实际项目中应该跳转到详情页面
      const announcement = this.announcements.find(a => a.id === announcementId);
      if (announcement) {
        alert(`公告详情\n\n标题: ${announcement.title}\n\n内容: ${announcement.content}\n\n发布者: ${announcement.authorName}\n\n发布时间: ${this.formatDateTime(announcement.publishedAt || announcement.createdAt)}`);
      }
    },
    handleBack() {
      this.$router.push('/resident');
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      
      // 如果已经是字符串格式，直接返回（后端已格式化）
      if (typeof dateTime === 'string') {
        return dateTime;
      }
      
      // 否则，处理时间戳或日期对象
      const date = new Date(dateTime);
      if (isNaN(date.getTime())) {
        return '';
      }
      
      // 确保格式一致性
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
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.announcements-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 250px;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.announcement-card:hover {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.announcement-card h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.announcement-content {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 3em;
}

.announcement-info {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 14px;
  color: #888;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.loading, .no-data {
  text-align: center;
  padding: 50px 0;
  color: #666;
}
</style>