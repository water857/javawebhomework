<template>
  <div class="container">
    <header>
      <h1>社区活动</h1>
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
        <div class="checkbox-filter">
          <input type="checkbox" id="myActivities" v-model="isMyActivities">
          <label for="myActivities">只看我发布的活动</label>
        </div>
        <button class="btn" @click="fetchActivities">筛选</button>
      </div>

      <div class="activities-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="activities.length === 0" class="no-data">暂无活动</div>
        <div v-else class="activity-card" v-for="activity in activities" :key="activity.id">
          <div class="activity-content" @click="handleActivityDetail(activity.id)">
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
            <button class="btn btn-delete" @click.stop="handleDeleteActivity(activity.id)">删除</button>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button class="btn" @click="handlePrevPage" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}</span>
        <button class="btn" @click="handleNextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { activityApi } from '../services/api';

export default {
  name: 'ProviderActivities',
  data() {
    return {
      activities: [],
      loading: false,
      searchQuery: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      totalPages: 1,
      isMyActivities: false
    }
  },
  computed: {
    currentUserId() {
      if (typeof localStorage !== 'undefined') {
        return localStorage.getItem('userId')
      }
      return ''
    }
  },
  mounted() {
    this.fetchActivities()
  },
  methods: {
    async fetchActivities() {
      this.loading = true
      try {
        let response;
        if (this.isMyActivities) {
          const userId = this.currentUserId;
          if (!userId) {
            alert('无法获取用户信息，请重新登录');
            this.loading = false;
            return;
          }
          response = await activityApi.getUserActivities(userId, this.currentPage, this.pageSize, this.searchQuery, this.statusFilter);
        } else {
          response = await activityApi.getActivities(this.currentPage, this.pageSize, this.searchQuery, this.statusFilter);
        }
        if (response.code === 'success') {
          this.activities = response.data;
          // 假设API返回的totalPages字段表示总页数
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
    formatDateTime(dateTime) {
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    },
    getStatusText(status) {
      const statusMap = {
        pending: '待开始',
        ongoing: '进行中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    },
    handleActivityDetail(activityId) {
      this.$router.push(`/provider/activities/${activityId}`)
    },
    handlePublishActivity() {
      this.$router.push('/provider/activities/publish')
    },
    handleEditActivity(activityId) {
      this.$router.push(`/provider/activities/edit/${activityId}`)
    },
    handleDeleteActivity(activityId) {
      if (confirm('确定要删除这个活动吗？')) {
        activityApi.deleteActivity(activityId)
          .then(response => {
            if (response.code === 'success') {
              alert('活动删除成功')
              this.fetchActivities()
            } else {
              alert(`删除失败: ${response.message}`)
            }
          })
          .catch(error => {
            console.error('删除活动失败:', error)
            alert('删除活动失败，请稍后重试')
          })
      }
    },
    handleBack() {
      this.$router.push('/provider')
    },
    handlePrevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.fetchActivities()
      }
    },
    handleNextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.fetchActivities()
      }
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

.header-actions .btn {
  margin-left: 10px;
}

.filter-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 5px;
  align-items: center;
}

.checkbox-filter {
  display: flex;
  align-items: center;
  gap: 5px;
}

.search-input, .filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  width: 300px;
}

.activities-list {
  margin-bottom: 20px;
}

.loading, .no-data {
  text-align: center;
  padding: 20px;
  color: #666;
}

.activity-card {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.activity-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.activity-content {
  padding: 15px;
  cursor: pointer;
}

.activity-actions {
  display: flex;
  gap: 10px;
  padding: 10px 15px;
  border-top: 1px solid #eee;
  background-color: #fafafa;
}

.btn-edit {
  background-color: #1890ff;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
}

.activity-card h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.activity-date, .activity-location {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.activity-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
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
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.pagination button {
  padding: 5px 10px;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>