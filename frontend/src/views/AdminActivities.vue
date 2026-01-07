<template>
  <div class="container">
    <header>
      <h1>活动管理</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="handlePublishActivity">发布活动</button>
        <button class="btn" @click="handleBack">返回首页</button>
      </div>
    </header>

    <div class="activities-section">
      <div class="filter-section">
        <input type="text" v-model="searchQuery" placeholder="搜索活动" class="search-input">
        <select v-model="statusFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="pending">待开始</option>
          <option value="ongoing">进行中</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
        <button class="btn" @click="fetchActivities">筛选</button>
      </div>

      <div class="activities-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="activities.length === 0" class="no-data">暂无活动</div>
        <div v-else class="activity-card" v-for="activity in activities" :key="activity.id">
          <div @click="handleActivityDetail(activity.id)">
            <h3>{{ activity.title }}</h3>
            <p class="activity-date">{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</p>
            <p class="activity-location">{{ activity.location }}</p>
            <p class="activity-status" :class="activity.status">{{ getStatusText(activity.status) }}</p>
            <div class="activity-info">
              <span>发布者: {{ activity.publisherName || '匿名' }}</span>
              <span>参与人数: {{ activity.currentParticipants }}/{{ activity.maxParticipants || '无限制' }}</span>
            </div>
          </div>
          <div class="activity-actions" v-if="activity.userId == currentUserId">
            <button class="btn btn-edit" @click.stop="handleEditActivity(activity.id)">编辑</button>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button class="btn" @click="currentPage > 1 && (currentPage--, fetchActivities())" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}</span>
        <button class="btn" @click="currentPage < totalPages && (currentPage++, fetchActivities())" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { activityApi } from '../services/api';

export default {
  name: 'AdminActivities',
  data() {
    return {
      activities: [],
      loading: false,
      searchQuery: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      totalPages: 1,
      currentUserId: localStorage.getItem('userId'),
      userRole: localStorage.getItem('role')
    }
  },
  mounted() {
    this.fetchActivities();
  },
  methods: {
    async fetchActivities() {
      this.loading = true;
      try {
        const response = await activityApi.getActivities(this.currentPage, this.pageSize, this.searchQuery, this.statusFilter);
        if (response.code === 'success') {
          this.activities = response.data;
          this.totalPages = response.totalPages || 1;
        } else {
          console.error('获取活动列表失败:', response.message);
        }
      } catch (error) {
        console.error('获取活动列表失败:', error);
        alert('获取活动列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handleActivityDetail(activityId) {
      this.$router.push(`/admin/activities/${activityId}`);
    },
    handlePublishActivity() {
      this.$router.push('/admin/activities/publish');
    },
    handleBack() {
      this.$router.push('/admin');
    },
    handleEditActivity(activityId) {
      this.$router.push(`/admin/activities/edit/${activityId}`);
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN');
    },
    getStatusText(status) {
      const statusMap = {
        'pending': '待开始',
        'ongoing': '进行中',
        'completed': '已完成',
        'cancelled': '已取消'
      };
      return statusMap[status] || status;
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

.activities-section {
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

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-card {
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

.activity-card:hover {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.activity-card h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.activity-date, .activity-location {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.activity-status {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin: 5px 0;
}

.activity-status.pending {
  background-color: #e6f7ff;
  color: #1890ff;
}

.activity-status.ongoing {
  background-color: #f6ffed;
  color: #52c41a;
}

.activity-status.completed {
  background-color: #fff7e6;
  color: #fa8c16;
}

.activity-status.cancelled {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.activity-info {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 14px;
  color: #888;
}

.activity-actions {
  display: flex;
  gap: 10px;
}

.btn-edit {
  background-color: #1890ff;
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