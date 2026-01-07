<template>
  <div class="container">
    <header>
      <h1>活动详情</h1>
      <button class="btn" @click="handleBack">返回活动列表</button>
    </header>

    <div class="activity-detail" v-if="activity">
      <div class="activity-header">
        <h2>{{ activity.title }}</h2>
        <p class="activity-status" :class="activity.status">{{ getStatusText(activity.status) }}</p>
      </div>

      <div class="activity-meta">
        <div class="meta-item">
          <span class="meta-label">发布者：</span>
          <span>{{ activity.publisherName || '匿名' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">时间：</span>
          <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">地点：</span>
          <span>{{ activity.location }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">参与人数：</span>
          <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants || '无限制' }}</span>
        </div>
      </div>

      <div class="activity-content">
        <h3>活动内容</h3>
        <div class="content-text">
          {{ activity.content }}
        </div>
      </div>

      <div class="activity-actions">
        <button class="btn btn-primary" @click="handleRegister" v-if="canRegister">报名活动</button>
        <button class="btn btn-primary" @click="handleCancelRegistration" v-if="isRegistered">取消报名</button>
        <button class="btn btn-primary" @click="handleCheckin" v-if="canCheckin">签到</button>
        <!-- 只对活动发布者显示编辑和删除按钮 -->
        <button class="btn btn-edit" @click="handleEdit" v-if="activity.publisherId === localStorage.getItem('userId')">编辑活动</button>
        <button class="btn btn-delete" @click="handleDelete" v-if="activity.publisherId === localStorage.getItem('userId')">删除活动</button>
      </div>

      <div class="activity-evaluation" v-if="activity.status === 'completed'">
        <h3>活动评价</h3>
        <form @submit.prevent="handleEvaluate">
          <div class="form-group">
            <label for="evaluation">评价内容</label>
            <textarea id="evaluation" v-model="evaluation.content" placeholder="请输入您对活动的评价" rows="4"></textarea>
          </div>
          <div class="form-group">
            <label for="rating">评分</label>
            <select id="rating" v-model.number="evaluation.rating">
              <option value="1">1星</option>
              <option value="2">2星</option>
              <option value="3">3星</option>
              <option value="4">4星</option>
              <option value="5">5星</option>
            </select>
          </div>
          <button type="submit" class="btn btn-primary" :disabled="submitting">提交评价</button>
        </form>
      </div>
    </div>

    <div class="loading" v-else-if="loading">加载中...</div>
    <div class="error" v-else>无法获取活动详情</div>
  </div>
</template>

<script>
import { activityApi } from '../services/api';

export default {
  name: 'ProviderActivityDetail',
  data() {
    return {
      activity: null,
      loading: true,
      evaluation: {
        content: '',
        rating: 5
      },
      submitting: false,
      isRegistered: false,
      canRegister: false,
      canCheckin: false
    }
  },
  mounted() {
    this.loadActivityDetail()
  },
  methods: {
    async loadActivityDetail() {
      const activityId = this.$route.params.id
      try {
        const response = await activityApi.getActivityDetail(activityId)
        if (response.code === 'success') {
          this.activity = response.data
          this.checkRegistrationStatus()
        } else {
          alert(`获取活动详情失败：${response.message}`)
          this.$router.push('/provider/activities')
        }
      } catch (error) {
        console.error('获取活动详情失败:', error)
        alert('获取活动详情失败，请稍后重试')
        this.$router.push('/provider/activities')
      } finally {
        this.loading = false
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
    checkRegistrationStatus() {
      // 这里需要根据实际API返回的用户注册状态来判断
      // 暂时假设用户未注册
      this.isRegistered = false
      this.canRegister = this.activity.status === 'pending'
      this.canCheckin = this.activity.status === 'ongoing'
    },
    async handleRegister() {
      try {
        const response = await activityApi.registerForActivity(this.activity.id)
        if (response.code === 'success') {
          alert('报名成功')
          this.isRegistered = true
          this.canRegister = false
          this.activity.currentParticipants++
        } else {
          alert(`报名失败：${response.message}`)
        }
      } catch (error) {
        console.error('报名失败:', error)
        alert('报名失败，请稍后重试')
      }
    },
    async handleCancelRegistration() {
      try {
        const response = await activityApi.cancelRegistration(this.activity.id)
        if (response.code === 'success') {
          alert('取消报名成功')
          this.isRegistered = false
          this.canRegister = this.activity.status === 'pending'
          this.activity.currentParticipants--
        } else {
          alert(`取消报名失败：${response.message}`)
        }
      } catch (error) {
        console.error('取消报名失败:', error)
        alert('取消报名失败，请稍后重试')
      }
    },
    async handleCheckin() {
      try {
        const response = await activityApi.checkin(this.activity.id)
        if (response.code === 'success') {
          alert('签到成功')
          this.canCheckin = false
        } else {
          alert(`签到失败：${response.message}`)
        }
      } catch (error) {
        console.error('签到失败:', error)
        alert('签到失败，请稍后重试')
      }
    },
    async handleEvaluate() {
      this.submitting = true
      try {
        const response = await activityApi.evaluateActivity(this.activity.id, this.evaluation)
        if (response.code === 'success') {
          alert('评价成功')
          this.evaluation = { content: '', rating: 5 }
        } else {
          alert(`评价失败：${response.message}`)
        }
      } catch (error) {
        console.error('评价失败:', error)
        alert('评价失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    },
    handleEdit() {
      this.$router.push(`/provider/activities/edit/${this.activity.id}`)
    },
    async handleDelete() {
      if (confirm('确定要删除这个活动吗？')) {
        try {
          const response = await activityApi.deleteActivity(this.activity.id)
          if (response.code === 'success') {
            alert('活动删除成功')
            this.$router.push('/provider/activities')
          } else {
            alert(`删除失败：${response.message}`)
          }
        } catch (error) {
          console.error('删除活动失败:', error)
          alert('删除活动失败，请稍后重试')
        }
      }
    },
    handleBack() {
      this.$router.push('/provider/activities')
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.activity-detail {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
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
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
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

.activity-meta {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  margin-bottom: 8px;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-label {
  font-weight: bold;
  margin-right: 10px;
  color: #666;
}

.activity-content {
  margin-bottom: 20px;
}

.activity-content h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.content-text {
  line-height: 1.6;
  color: #333;
  white-space: pre-line;
}

.activity-actions {
  margin-bottom: 30px;
}

.activity-actions .btn {
  margin-right: 10px;
}

.btn-edit {
  background-color: #1890ff;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
}

.activity-evaluation {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.activity-evaluation h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

textarea, select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.loading, .error {
  text-align: center;
  padding: 50px 0;
  color: #666;
}
</style>