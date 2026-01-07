<template>
  <div class="container">
    <header>
      <h1>活动回顾填写</h1>
    </header>

    <div class="card">
      <div class="form-row">
        <label>活动ID</label>
        <input v-model.number="activityId" />
      </div>
      <div class="form-row">
        <label>回顾内容</label>
        <textarea v-model="summary"></textarea>
      </div>
      <div class="form-row">
        <label>图片URL（逗号分隔）</label>
        <input v-model="images" />
      </div>
      <button class="btn" @click="submitReview">提交</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentActivityReview',
  data() {
    return {
      activityId: null,
      summary: '',
      images: ''
    }
  },
  methods: {
    async submitReview() {
      if (!this.activityId) return
      try {
        await axios.post('/activities/review', {
          activityId: this.activityId,
          summary: this.summary,
          images: this.images
        })
        this.summary = ''
        this.images = ''
      } catch (error) {
        console.error('提交失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.card {
  border: 1px solid #e0e0e0;
  padding: 16px;
  border-radius: 8px;
}
.form-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}
</style>
