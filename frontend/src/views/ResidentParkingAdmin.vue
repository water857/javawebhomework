<template>
  <div class="container">
    <header>
      <h1>车位申请审批</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadApplications">刷新</button>
      </div>
    </header>

    <div class="list">
      <div v-for="app in applications" :key="app.id" class="list-item">
        <div>
          申请ID {{ app.id }} - 用户 {{ app.userId }} - 车位 {{ app.parkingId }} - {{ app.status }}
        </div>
        <div class="actions">
          <button class="btn" @click="review(app.id, 'approved')">通过</button>
          <button class="btn" @click="review(app.id, 'rejected')">拒绝</button>
        </div>
      </div>
    </div>

    <h2>释放车位</h2>
    <div class="input-row">
      <input v-model.number="releaseId" placeholder="车位ID" />
      <button class="btn" @click="release">释放</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentParkingAdmin',
  data() {
    return {
      applications: [],
      releaseId: null
    }
  },
  mounted() {
    this.loadApplications()
  },
  methods: {
    handleBack() {
      this.$router.push('/admin')
    },
    async loadApplications() {
      try {
        const response = await axios.get('/parking/application/list')
        this.applications = response.data.data || []
      } catch (error) {
        console.error('加载申请失败:', error)
      }
    },
    async review(applicationId, status) {
      try {
        await axios.post('/parking/application/review', { applicationId, status })
        this.loadApplications()
      } catch (error) {
        console.error('审核失败:', error)
      }
    },
    async release() {
      if (!this.releaseId) return
      try {
        await axios.post('/parking/space/release', { parkingId: this.releaseId })
      } catch (error) {
        console.error('释放失败:', error)
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
.list {
  margin-top: 20px;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 10px;
}
.actions {
  display: flex;
  gap: 8px;
}
.input-row {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.input-row input {
  flex: 1;
  padding: 8px;
}
</style>
