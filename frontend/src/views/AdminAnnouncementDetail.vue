<template>
  <div class="container">
    <header>
      <h1>公告详情</h1>
      <button class="btn" @click="handleBack">返回公告列表</button>
    </header>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!announcement" class="no-data">公告不存在</div>
    <div v-else class="announcement-detail">
      <div class="announcement-header">
        <h2>{{ announcement.title }}</h2>
        <span class="announcement-status" :class="getStatusClass(announcement.status)">{{ getStatusText(announcement.status) }}</span>
      </div>

      <div class="announcement-info">
        <div class="info-item">
          <label>发布者:</label>
          <span>{{ announcement.authorName || '匿名' }}</span>
        </div>
        <div class="info-item">
          <label>发布时间:</label>
          <span>{{ formatDateTime(announcement.publishedAt || announcement.createdAt) }}</span>
        </div>
        <div class="info-item">
          <label>创建时间:</label>
          <span>{{ formatDateTime(announcement.createdAt) }}</span>
        </div>
        <div class="info-item" v-if="announcement.updatedAt">
          <label>更新时间:</label>
          <span>{{ formatDateTime(announcement.updatedAt) }}</span>
        </div>
      </div>

      <div class="announcement-content">
        <h3>公告内容</h3>
        <p>{{ announcement.content }}</p>
      </div>

      <!-- 公告管理操作 -->
      <div class="announcement-actions">
        <h3>公告管理</h3>
        <div class="status-update-section">
          <label>更新公告状态:</label>
          <select v-model.number="selectedStatus" class="status-select">
            <option :value="0">草稿</option>
            <option :value="1">已发布</option>
          </select>
          <button class="btn btn-primary" @click="handleUpdateStatus" :disabled="selectedStatus == announcement.status">更新状态</button>
        </div>
        <div class="action-buttons">
          <button class="btn btn-edit" @click="handleEditAnnouncement">编辑公告</button>
          <button class="btn btn-delete" @click="handleDeleteAnnouncement">删除公告</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { announcementApi } from '../services/api';

export default {
  name: 'AdminAnnouncementDetail',
  data() {
    return {
      announcement: null,
      loading: false,
      selectedStatus: ''
    }
  },
  mounted() {
    this.fetchAnnouncementDetail();
  },
  methods: {
    async fetchAnnouncementDetail() {
      this.loading = true;
      try {
        const announcementId = this.$route.params.id;
        const response = await announcementApi.getAnnouncementDetail(announcementId);
        if (response.code === 'success') {
          this.announcement = response.data;
          this.selectedStatus = this.announcement.status;
        } else {
          console.error('获取公告详情失败:', response.message);
          alert('获取公告详情失败');
        }
      } catch (error) {
        console.error('获取公告详情失败:', error);
        alert('获取公告详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    async handleUpdateStatus() {
      try {
        const response = await announcementApi.updateAnnouncementStatus(this.announcement.id, this.selectedStatus);
        if (response.code === 'success') {
          alert('公告状态更新成功！');
          // 刷新公告详情
          this.fetchAnnouncementDetail();
        } else {
          alert(`公告状态更新失败：${response.message}`);
        }
      } catch (error) {
        console.error('公告状态更新失败:', error);
        alert('公告状态更新失败，请稍后重试');
      }
    },
    async handleDeleteAnnouncement() {
      if (confirm('确定要删除这条公告吗？')) {
        try {
          const response = await announcementApi.deleteAnnouncement(this.announcement.id);
          if (response.code === 'success') {
            alert('公告删除成功');
            this.handleBack();
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
    handleEditAnnouncement() {
      this.$router.push(`/admin/announcements/edit/${this.announcement.id}`);
    },
    handleBack() {
      this.$router.push('/admin/announcements');
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN');
    },
    getStatusText(status) {
      return status === 1 ? '已发布' : '草稿';
    },
    getStatusClass(status) {
      return status === 1 ? 'published' : 'draft';
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

.loading, .no-data {
  text-align: center;
  padding: 50px 0;
  color: #666;
}

.announcement-detail {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.announcement-header h2 {
  margin: 0;
  color: #333;
}

/* 公告状态样式 */
.announcement-status {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
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
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: bold;
  color: #333;
  width: 100px;
  margin-right: 10px;
}

.info-item span {
  color: #666;
}

.announcement-content {
  margin-bottom: 30px;
}

.announcement-content h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.announcement-content p {
  color: #555;
  line-height: 1.8;
  font-size: 16px;
  white-space: pre-wrap;
}

/* 公告操作样式 */
.announcement-actions {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.announcement-actions h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.status-update-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.status-update-section label {
  font-weight: bold;
  color: #333;
}

.status-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.btn-edit {
  background-color: #1890ff;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
}
</style>