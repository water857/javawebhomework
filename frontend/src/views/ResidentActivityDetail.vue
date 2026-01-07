<template>
  <div class="container">
    <header>
      <h1>活动详情</h1>
      <button class="btn" @click="handleBack">返回活动列表</button>
    </header>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!activity" class="no-data">活动不存在</div>
    <div v-else class="activity-detail">
      <div class="activity-header">
        <h2>{{ activity.title }}</h2>
        <span class="activity-status" :class="activity.status">{{ getStatusText(activity.status) }}</span>
      </div>

      <div class="activity-info">
        <div class="info-item">
          <label>发布者:</label>
          <span>{{ activity.publisherName || '匿名' }}</span>
        </div>
        <div class="info-item">
          <label>活动时间:</label>
          <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
        </div>
        <div class="info-item">
          <label>活动地点:</label>
          <span>{{ activity.location }}</span>
        </div>
        <div class="info-item">
          <label>参与人数:</label>
          <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants || '无限制' }}</span>
        </div>
        <div class="info-item">
          <label>发布时间:</label>
          <span>{{ formatDateTime(activity.createdAt) }}</span>
        </div>
      </div>

      <div class="activity-content">
        <h3>活动内容</h3>
        <p>{{ activity.content }}</p>
      </div>

      <!-- 活动发布者操作 -->
      <div class="activity-publisher-actions" v-if="isPublisher">
        <h3>活动管理</h3>
        <div class="status-update-section">
          <label>更新活动状态:</label>
          <select v-model="selectedStatus" class="status-select">
            <option value="pending">待开始</option>
            <option value="ongoing">进行中</option>
            <option value="completed">已完成</option>
            <option value="cancelled">已取消</option>
          </select>
          <button class="btn btn-primary" @click="handleUpdateStatus" :disabled="selectedStatus === activity.status">更新状态</button>
        </div>
        <div class="qr-code-section">
          <button v-if="activity.qrCodeUrl" class="btn btn-info" @click="showQrCode = true">查看签到二维码</button>
          <button v-else class="btn btn-primary" @click="handleGenerateQrCode">生成签到二维码</button>
        </div>
      </div>
      
      <!-- 活动参与者操作 -->
      <div class="activity-actions">
        <button v-if="canRegister" class="btn btn-primary" @click="handleRegister">立即报名</button>
        <button v-else-if="canCancelRegister" class="btn btn-danger" @click="handleCancelRegister">取消报名</button>
        <button v-else-if="canCheckin" class="btn btn-success" @click="handleCheckin">立即签到</button>
        <button v-else-if="canEvaluate" class="btn btn-warning" @click="showEvaluateModal = true">评价活动</button>
      </div>

      <!-- 签到二维码模态框 -->
      <div v-if="showQrCode" class="modal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>签到二维码</h3>
            <button class="close-btn" @click="showQrCode = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="qr-code-container">
              <img :src="activity.qrCodeUrl" alt="签到二维码" class="qr-code">
              <p>请扫码签到</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价模态框 -->
      <div v-if="showEvaluateModal" class="modal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>评价活动</h3>
            <button class="close-btn" @click="showEvaluateModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>评分:</label>
              <div class="rating">
                <span v-for="n in 5" :key="n" 
                      :class="['star', { 'active': rating >= n }]" 
                      @click="rating = n">
                  ★
                </span>
              </div>
            </div>
            <div class="form-group">
              <label>评价内容:</label>
              <textarea v-model="evaluation" placeholder="请输入您的评价..." rows="5"></textarea>
            </div>
            <div class="modal-actions">
              <button class="btn btn-primary" @click="handleSubmitEvaluation">提交评价</button>
              <button class="btn" @click="showEvaluateModal = false">取消</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动照片 -->
      <div v-if="activity.images && activity.images.length > 0" class="activity-images">
        <h3>活动照片</h3>
        <div class="images-grid">
          <img v-for="(image, index) in activity.images" :key="index" :src="image.imageUrl" alt="活动照片" class="activity-image">
        </div>
      </div>

      <!-- 活动评价 -->
      <div v-if="activity.evaluations && activity.evaluations.length > 0" class="activity-evaluations">
        <h3>活动评价</h3>
        <div class="evaluations-list">
          <div v-for="(evaluationItem, index) in activity.evaluations" :key="index" class="evaluation-item">
            <div class="evaluation-header">
              <span class="evaluator-name">{{ evaluationItem.userName }}</span>
              <div class="evaluation-rating">
                <span v-for="n in 5" :key="n" :class="['star', { 'active': evaluationItem.rating >= n }]">★</span>
              </div>
            </div>
            <div class="evaluation-content">{{ evaluationItem.evaluation }}</div>
            <div class="evaluation-time">{{ formatDateTime(evaluationItem.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { activityApi } from '../services/api';

export default {
  name: 'ResidentActivityDetail',
  data() {
    return {
      activity: null,
      loading: false,
      showQrCode: false,
      showEvaluateModal: false,
      rating: 5,
      evaluation: '',
      isRegistered: false,
      isPublisher: false,
      selectedStatus: ''
    }
  },
  computed: {
    canRegister() {
      if (!this.activity) return false
      const now = new Date()
      const startTime = new Date(this.activity.startTime)
      return !this.isRegistered && this.activity.status === 'pending' && now < startTime && 
             (!this.activity.maxParticipants || this.activity.currentParticipants < this.activity.maxParticipants)
    },
    canCancelRegister() {
      if (!this.activity) return false
      const now = new Date()
      const startTime = new Date(this.activity.startTime)
      return this.isRegistered && this.activity.status === 'pending' && now < startTime
    },
    canCheckin() {
      if (!this.activity) return false
      const now = new Date()
      const startTime = new Date(this.activity.startTime)
      const endTime = new Date(this.activity.endTime)
      return this.isRegistered && this.activity.status === 'ongoing' && now >= startTime && now <= endTime
    },
    canEvaluate() {
      if (!this.activity) return false
      const now = new Date()
      const endTime = new Date(this.activity.endTime)
      return this.activity.status === 'completed' && now > endTime
    }
  },
  mounted() {
    this.fetchActivityDetail()
  },
  methods: {
    async fetchActivityDetail() {
      const activityId = this.$route.params.id
      this.loading = true
      try {
        const response = await activityApi.getActivityDetail(activityId);
        if (response.code === 'success') {
          // 处理新的API响应格式
          if (response.data.activity) {
            this.activity = response.data.activity;
            this.isRegistered = response.data.isRegistered;
          } else {
            this.activity = response.data;
            this.isRegistered = false;
          }
          // 检查当前用户是否为活动发布者
          this.isPublisher = this.activity.userId === parseInt(localStorage.getItem('userId'));
          // 设置默认选中状态
          this.selectedStatus = this.activity.status;
        } else {
          console.error('获取活动详情失败:', response.message);
        }
      } catch (error) {
        console.error('获取活动详情失败:', error);
        alert('获取活动详情失败，请稍后重试');
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
    handleBack() {
      this.$router.push('/resident/activities')
    },
    async handleRegister() {
      try {
        const response = await activityApi.registerForActivity(this.activity.id);
        if (response.code === 'success') {
          alert('报名成功！');
          // 刷新活动详情
          this.fetchActivityDetail();
        } else {
          alert(`报名失败：${response.message}`);
        }
      } catch (error) {
        console.error('报名失败:', error);
        alert('报名失败，请稍后重试');
      }
    },
    async handleCheckin() {
      try {
        const response = await activityApi.checkin(this.activity.id);
        if (response.code === 'success') {
          alert('签到成功！');
          // 刷新活动详情
          this.fetchActivityDetail();
        } else {
          alert(`签到失败：${response.message}`);
        }
      } catch (error) {
        console.error('签到失败:', error);
        alert('签到失败，请稍后重试');
      }
    },
    async handleCancelRegister() {
      try {
        const response = await activityApi.cancelRegistration(this.activity.id);
        if (response.code === 'success') {
          alert('取消报名成功！');
          // 刷新活动详情
          this.fetchActivityDetail();
        } else {
          alert(`取消报名失败：${response.message}`);
        }
      } catch (error) {
        console.error('取消报名失败:', error);
        alert('取消报名失败，请稍后重试');
      }
    },
    async handleSubmitEvaluation() {
      if (this.rating < 1 || !this.evaluation.trim()) {
        alert('请填写完整的评价信息');
        return;
      }
      
      try {
        const evaluationData = {
          rating: this.rating,
          evaluation: this.evaluation.trim()
        };
        const response = await activityApi.evaluateActivity(this.activity.id, evaluationData);
        if (response.code === 'success') {
          alert('评价提交成功！');
          this.showEvaluateModal = false;
          // 刷新活动详情
          this.fetchActivityDetail();
          // 重置评价表单
          this.rating = 5;
          this.evaluation = '';
        } else {
          alert(`评价提交失败：${response.message}`);
        }
      } catch (error) {
        console.error('评价提交失败:', error);
        alert('评价提交失败，请稍后重试');
      }
    },
    async handleUpdateStatus() {
      try {
        const response = await activityApi.updateActivityStatus(this.activity.id, this.selectedStatus);
        if (response.code === 'success') {
          alert('活动状态更新成功！');
          // 刷新活动详情
          this.fetchActivityDetail();
        } else {
          alert(`活动状态更新失败：${response.message}`);
        }
      } catch (error) {
        console.error('活动状态更新失败:', error);
        alert('活动状态更新失败，请稍后重试');
      }
    },
    async handleGenerateQrCode() {
      try {
        const response = await activityApi.generateCheckinQrCode(this.activity.id);
        if (response.code === 'success') {
          alert('签到二维码生成成功！');
          // 刷新活动详情
          this.fetchActivityDetail();
        } else {
          alert(`二维码生成失败：${response.message}`);
        }
      } catch (error) {
        console.error('二维码生成失败:', error);
        alert('二维码生成失败，请稍后重试');
      }
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 800px;
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

.activity-detail {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.activity-header h2 {
  margin: 0;
  color: #333;
}

.activity-status {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
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
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  width: 100px;
  font-weight: bold;
  color: #666;
}

.info-item span {
  color: #333;
}

.activity-content {
  margin-bottom: 20px;
}

.activity-content h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.activity-content p {
  line-height: 1.6;
  color: #666;
  white-space: pre-wrap;
}

.activity-actions {
  margin-bottom: 30px;
}

.activity-actions .btn {
  margin-right: 10px;
  padding: 8px 20px;
}

/* 活动发布者操作样式 */
.activity-publisher-actions {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.activity-publisher-actions h3 {
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

.qr-code-section {
  margin-top: 15px;
}

.qr-code-section .btn {
  margin-right: 10px;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.qr-code-container {
  text-align: center;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: 10px;
}

/* 评价表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.rating {
  display: flex;
  gap: 5px;
}

.star {
  font-size: 24px;
  color: #ddd;
  cursor: pointer;
}

.star.active {
  color: #faad14;
}

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 活动照片样式 */
.activity-images {
  margin-bottom: 30px;
}

.activity-images h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.activity-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.activity-image:hover {
  transform: scale(1.1);
}

/* 活动评价样式 */
.activity-evaluations {
  margin-bottom: 20px;
}

.activity-evaluations h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.evaluations-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.evaluation-item {
  background-color: #fafafa;
  padding: 15px;
  border-radius: 8px;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.evaluator-name {
  font-weight: bold;
  color: #333;
}

.evaluation-rating .star {
  font-size: 16px;
}

.evaluation-content {
  margin-bottom: 10px;
  color: #666;
  line-height: 1.5;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style>