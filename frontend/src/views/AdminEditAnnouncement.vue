<template>
  <div class="container">
    <header>
      <h1>编辑公告</h1>
      <div class="header-actions">
        <button class="btn" @click="handleHome">返回首页</button>
        <button class="btn" @click="handleBack">返回公告列表</button>
      </div>
    </header>

    <div class="edit-form">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!announcement.id" class="error">公告不存在或已被删除</div>
      <form v-else @submit.prevent="handleUpdate">
        <div class="form-group">
          <label for="title">公告标题 <span class="required">*</span></label>
          <input type="text" id="title" v-model="announcement.title" required placeholder="请输入公告标题">
        </div>

        <div class="form-group">
          <label for="content">公告内容 <span class="required">*</span></label>
          <textarea id="content" v-model="announcement.content" required placeholder="请输入公告详细内容" rows="10"></textarea>
        </div>

        <div class="form-group">
          <label for="status">公告状态 <span class="required">*</span></label>
          <select id="status" v-model.number="announcement.status" required>
            <option value="1">已发布</option>
            <option value="0">草稿</option>
          </select>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="submitting">更新公告</button>
          <button type="button" class="btn" @click="handleBack">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { announcementApi } from '../services/api';

export default {
  name: 'AdminEditAnnouncement',
  data() {
    return {
      announcement: {
        id: null,
        title: '',
        content: '',
        status: 1
      },
      loading: true,
      submitting: false
    }
  },
  mounted() {
    this.fetchAnnouncementDetail();
  },
  methods: {
    handleHome() {
      this.$router.push('/admin');
    },
    async fetchAnnouncementDetail() {
      this.loading = true;
      try {
        const announcementId = this.$route.params.id;
        const response = await announcementApi.getAnnouncementDetail(announcementId);
        if (response.code === 'success') {
          this.announcement = response.data;
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
    async handleUpdate() {
      this.submitting = true;
      try {
        // 验证表单数据
        if (!this.announcement.title.trim() || !this.announcement.content.trim()) {
          alert('标题和内容不能为空');
          return;
        }

        const response = await announcementApi.updateAnnouncement(this.announcement.id, this.announcement);
        if (response.code === 'success') {
          alert('公告更新成功！');
          this.$router.push('/admin/announcements');
        } else {
          console.error('更新公告失败:', response.message);
          alert(`更新公告失败：${response.message}`);
        }
      } catch (error) {
        console.error('更新公告失败:', error);
        alert('更新公告失败，请稍后重试');
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

.edit-form {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

.required {
  color: #ff4d4f;
}

input[type="text"],
textarea,
select {
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading,
.error {
  text-align: center;
  padding: 50px 0;
  font-size: 16px;
}

.loading {
  color: #1890ff;
}

.error {
  color: #ff4d4f;
}
</style>
