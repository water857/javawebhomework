<template>
  <div class="container">
    <header>
      <h1>发布活动</h1>
      <div class="header-actions">
        <button class="btn" @click="handleHome">返回首页</button>
        <button class="btn" @click="handleBack">返回活动列表</button>
      </div>
    </header>

    <div class="publish-form">
      <form @submit.prevent="handlePublish">
        <div class="form-group">
          <label for="title">活动标题 <span class="required">*</span></label>
          <input type="text" id="title" v-model="activity.title" required placeholder="请输入活动标题">
        </div>

        <div class="form-group">
          <label for="content">活动内容 <span class="required">*</span></label>
          <textarea id="content" v-model="activity.content" required placeholder="请输入活动详细内容" rows="6"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startTime">开始时间 <span class="required">*</span></label>
            <input type="datetime-local" id="startTime" v-model="activity.startTime" required>
          </div>

          <div class="form-group">
            <label for="endTime">结束时间 <span class="required">*</span></label>
            <input type="datetime-local" id="endTime" v-model="activity.endTime" required>
          </div>
        </div>

        <div class="form-group">
          <label for="location">活动地点 <span class="required">*</span></label>
          <input type="text" id="location" v-model="activity.location" required placeholder="请输入活动地点">
        </div>

        <div class="form-group">
          <label for="maxParticipants">最大参与人数</label>
          <input type="number" id="maxParticipants" v-model.number="activity.maxParticipants" min="0" placeholder="0表示无限制">
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="submitting">发布活动</button>
          <button type="button" class="btn" @click="handleBack">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { activityApi } from '../services/api';

export default {
  name: 'ResidentPublishActivity',
  data() {
    return {
      activity: {
        title: '',
        content: '',
        startTime: '',
        endTime: '',
        location: '',
        maxParticipants: 0
      },
      submitting: false
    }
  },
  mounted() {
    // 设置默认开始时间为当前时间
    const now = new Date()
    const defaultStartTime = now.toISOString().slice(0, 16)
    this.activity.startTime = defaultStartTime
    
    // 设置默认结束时间为当前时间后2小时
    const defaultEndTime = new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString().slice(0, 16)
    this.activity.endTime = defaultEndTime
  },
  methods: {
    handleHome() {
      this.$router.push('/resident')
    },
    async handlePublish() {
      // 表单验证
      if (!this.activity.title || !this.activity.content || !this.activity.startTime || !this.activity.endTime || !this.activity.location) {
        alert('请填写所有必填字段')
        return
      }

      // 验证时间逻辑
      const startTime = new Date(this.activity.startTime)
      const endTime = new Date(this.activity.endTime)
      const now = new Date()

      if (startTime < now) {
        alert('开始时间不能早于当前时间')
        return
      }

      if (endTime <= startTime) {
        alert('结束时间必须晚于开始时间')
        return
      }

      this.submitting = true
      try {
        // 格式化日期时间为完整的ISO 8601格式（包含秒和毫秒）
        const activityData = {
          ...this.activity,
          startTime: new Date(this.activity.startTime).toISOString(),
          endTime: new Date(this.activity.endTime).toISOString()
        };
        
        const response = await activityApi.publishActivity(activityData);
        if (response.code === 'success') {
          alert('活动发布成功！')
          this.$router.push('/resident/activities')
        } else {
          alert(`活动发布失败：${response.message}`)
        }
      } catch (error) {
        console.error('活动发布失败:', error);
        alert('活动发布失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    },
    handleBack() {
      this.$router.push('/resident/activities')
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

.header-actions {
  display: flex;
  gap: 10px;
}

.publish-form {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.required {
  color: #ff4d4f;
}

input[type="text"],
input[type="number"],
input[type="datetime-local"],
textarea {
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

input:focus,
textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.form-actions .btn {
  padding: 8px 20px;
}

.form-actions .btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
