<template>
  <div class="container">
    <header>
      <h1>居民车位申请</h1>
      <button class="btn" @click="loadSpaces">刷新车位</button>
    </header>

    <div class="grid">
      <div v-for="space in spaces" :key="space.id" class="card">
        <h3>{{ space.code }}</h3>
        <p>状态：{{ space.status }}</p>
        <button class="btn" :disabled="space.status !== 'available'" @click="apply(space.id)">申请</button>
      </div>
    </div>

    <h2>我的申请</h2>
    <button class="btn" @click="loadApplications">刷新申请</button>
    <div class="list">
      <div v-for="app in applications" :key="app.id" class="list-item">
        申请ID {{ app.id }} - 车位 {{ app.parkingId }} - {{ app.status }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentParking',
  data() {
    return {
      spaces: [],
      applications: []
    }
  },
  mounted() {
    this.loadSpaces()
    this.loadApplications()
  },
  methods: {
    async loadSpaces() {
      try {
        const response = await axios.get('/parking/space/list')
        this.spaces = response.data.data || []
      } catch (error) {
        console.error('加载车位失败:', error)
      }
    },
    async loadApplications() {
      try {
        const response = await axios.get('/parking/application/list')
        this.applications = response.data.data || []
      } catch (error) {
        console.error('加载申请失败:', error)
      }
    },
    async apply(parkingId) {
      try {
        await axios.post('/parking/application', { parkingId })
        this.loadApplications()
      } catch (error) {
        console.error('申请失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin: 20px 0;
}
.card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
}
.list {
  margin-top: 12px;
}
.list-item {
  padding: 8px;
  border-bottom: 1px solid #eee;
}
</style>
