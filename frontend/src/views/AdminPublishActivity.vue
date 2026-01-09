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
  name: 'AdminPublishActivity',
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
  methods: {
    handleHome() {
      this.$router.push('/admin');
    },
    async handlePublish() {
      this.submitting = true;
      try {
        // 验证开始时间和结束时间
        const startTime = new Date(this.activity.startTime);
        const endTime = new Date(this.activity.endTime);
        if (endTime <= startTime) {
          alert('结束时间必须晚于开始时间');
          return;
        }

        // 格式化日期时间为完整的ISO 8601格式（包含秒和毫秒）
        const activityData = {
          ...this.activity,
          startTime: new Date(this.activity.startTime).toISOString(),
          endTime: new Date(this.activity.endTime).toISOString()
        };
        
        const response = await activityApi.publishActivity(activityData);
        if (response.code === 'success') {
          alert('活动发布成功！');
          this.$router.push('/admin/activities');
        } else {
          alert(`活动发布失败：${response.message}`);
        }
      } catch (error) {
        console.error('发布活动失败:', error);
        alert('发布活动失败，请稍后重试');
      } finally {
        this.submitting = false;
      }
    },
    handleBack() {
      this.$router.push('/admin/activities');
    }
  }
}
</script>

<style scoped>
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-actions {
  display: flex;
  gap: 10px;
}
</style>
