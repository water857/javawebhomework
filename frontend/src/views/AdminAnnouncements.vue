<template>
  <div class="container">
    <header>
      <h1>公告管理</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="handlePublishAnnouncement">发布公告</button>
        <button class="btn" @click="handleBack">返回首页</button>
      </div>
    </header>

    <div class="announcements-section">
      <div class="filter-section">
        <input type="text" v-model="searchQuery" class="search-input" placeholder="搜索公告" />
        <select v-model="statusFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="1">已发布</option>
          <option value="0">草稿</option>
        </select>
        <button class="btn" @click="fetchAnnouncements">筛选</button>
      </div>

      <div class="announcements-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="announcements.length === 0" class="no-data">暂无公告</div>
        <div v-else class="announcement-card" v-for="announcement in announcements" :key="announcement.id">
          <div @click="handleAnnouncementDetail(announcement.id)">
            <h3>{{ announcement.title }}</h3>
            <p class="announcement-content">{{ truncateContent(announcement.content) }}</p>
            <p class="announcement-status" :class="getStatusClass(announcement.status)">{{ getStatusText(announcement.status) }}</p>
            <div class="announcement-info">
              <span>发布者: {{ announcement.authorName || '匿名' }}</span>
              <span>发布时间: {{ formatDateTime(announcement.publishedAt || announcement.createdAt) }}</span>
            </div>
          </div>
          <div class="announcement-actions">
            <button class="btn btn-edit" @click.stop="handleEditAnnouncement(announcement.id)">编辑</button>
            <button class="btn btn-delete" @click.stop="handleDeleteAnnouncement(announcement.id)">删除</button>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button class="btn" @click="previousPage" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}</span>
        <button class="btn" @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { announcementApi } from '../services/api';

export default {
  name: 'AdminAnnouncements',
  data() {
    return {
      announcements: [],
      loading: false,
      searchQuery: '',
      statusFilter: '',
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
    // 上一页
    previousPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.fetchAnnouncements();
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.fetchAnnouncements();
      }
    },
    
    async fetchAnnouncements() {
      this.loading = true;
      try {
        const statusFilter = this.statusFilter !== '' ? parseInt(this.statusFilter) : null;
        const response = await announcementApi.getAnnouncements(this.currentPage, this.pageSize, this.searchQuery, statusFilter);
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
      this.$router.push(`/admin/announcements/${announcementId}`);
    },
    handlePublishAnnouncement() {
      this.$router.push('/admin/announcements/publish');
    },
    handleBack() {
      this.$router.push('/admin');
    },
    handleEditAnnouncement(announcementId) {
      this.$router.push(`/admin/announcements/edit/${announcementId}`);
    },
    async handleDeleteAnnouncement(announcementId) {
      if (confirm('确定要删除这条公告吗？')) {
        try {
          const response = await announcementApi.deleteAnnouncement(announcementId);
          if (response.code === 'success') {
            alert('公告删除成功');
            this.fetchAnnouncements();
          } else {
            console.error('删除公告失败:', response.message);
            alert('删除公告失败');
          }
        } catch (error) {
          console.error('删除公告失败:', error);
          alert('删除公告失败，请稍后重试');
        }
      }
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
    getStatusText(status) {
      return status === 1 ? '已发布' : '草稿';
    },
    getStatusClass(status) {
      return status === 1 ? 'published' : 'draft';
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

.search-input, .filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.announcement-status {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin: 5px 0;
}

.announcement-status.published {
  background-color: #f6ffed;
  color: #52c41a;
}

.announcement-status.draft {
  background-color: #fffbe6;
  color: #faad14;
}

.announcement-info {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 14px;
  color: #888;
}

.announcement-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.btn-edit {
  background-color: #1890ff;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
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