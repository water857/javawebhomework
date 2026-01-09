<template>
  <div class="container">
    <header>
      <h1>访客记录管理</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadRecords">刷新</button>
      </div>
    </header>

    <div class="list">
      <div v-for="record in records" :key="record.id" class="list-item">
        {{ record.id }} - {{ record.visitorName }} - {{ record.phone }} - {{ record.visitTime }} - 住户 {{ record.hostUserId }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentVisitorAdmin',
  data() {
    return {
      records: []
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    handleBack() {
      this.$router.push('/admin')
    },
    async loadRecords() {
      try {
        const response = await axios.get('/visitor/list')
        this.records = response.data.data || []
      } catch (error) {
        console.error('加载记录失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
}
.list-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}
</style>
