<template>
  <div class="container">
    <header>
      <h1>活动详情（回顾）</h1>
      <button class="btn" @click="loadDetail">刷新</button>
    </header>

    <div class="card" v-if="detail">
      <h2>{{ detail.activity.title }}</h2>
      <p>{{ detail.activity.description }}</p>
      <h3>活动回顾</h3>
      <p>{{ detail.review ? detail.review.summary : '暂无回顾' }}</p>
      <p>图片：{{ detail.review ? detail.review.images : '' }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentActivityReviewDetail',
  data() {
    return {
      detail: null
    }
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      try {
        const id = this.$route.params.id
        const response = await axios.get(`/activities/${id}`)
        this.detail = response.data.data
      } catch (error) {
        console.error('加载详情失败:', error)
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
</style>
