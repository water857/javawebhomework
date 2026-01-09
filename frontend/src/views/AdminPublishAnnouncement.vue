<template>
  <div class="container">
    <header>
      <h1>发布公告</h1>
      <div class="header-actions">
        <button class="btn" @click="handleHome">返回首页</button>
        <button class="btn" @click="handleBack">返回公告列表</button>
      </div>
    </header>

    <div class="publish-form">
      <form @submit.prevent="handlePublish">
        <div class="form-group">
          <label for="title">公告标题 <span class="required">*</span></label>
          <input type="text" id="title" v-model="announcement.title" required placeholder="请输入公告标题">
        </div>

        <div class="form-group">
          <label for="content">公告内容 <span class="required">*</span></label>
          <textarea id="content" v-model="announcement.content" required placeholder="请输入公告详细内容" rows="10"></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="submitting">发布公告</button>
          <button type="button" class="btn btn-secondary" @click="handleSaveDraft" :disabled="submitting">保存草稿</button>
          <button type="button" class="btn" @click="handleBack">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { announcementApi } from '../services/api';

export default {
  name: 'AdminPublishAnnouncement',
  data() {
    return {
      announcement: {
        title: '',
        content: ''
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
        // 验证表单数据
        if (!this.announcement.title.trim() || !this.announcement.content.trim()) {
          alert('标题和内容不能为空');
          return;
        }

        const response = await announcementApi.publishAnnouncement(this.announcement);
        if (response.code === 'success') {
          alert('公告发布成功！');
          this.$router.push('/admin/announcements');
        } else {
          alert(`公告发布失败：${response.message}`);
        }
      } catch (error) {
        console.error('发布公告失败:', error);
        alert('发布公告失败，请稍后重试');
      } finally {
        this.submitting = false;
      }
    },
    async handleSaveDraft() {
      this.submitting = true;
      try {
        // 验证表单数据
        if (!this.announcement.title.trim() || !this.announcement.content.trim()) {
          alert('标题和内容不能为空');
          return;
        }

        const response = await announcementApi.saveDraft(this.announcement);
        if (response.code === 'success') {
          alert('草稿保存成功！');
          this.$router.push('/admin/announcements');
        } else {
          alert(`草稿保存失败：${response.message}`);
        }
      } catch (error) {
        console.error('保存草稿失败:', error);
        alert('保存草稿失败，请稍后重试');
      } finally {
        this.submitting = false;
      }
    },
    handleBack() {
      this.$router.push('/admin/announcements');
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
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
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
textarea,
input[type="number"],
input[type="datetime-local"] {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

textarea {
  resize: vertical;
  min-height: 150px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-secondary {
  background-color: #faad14;
  color: white;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
