<template>
  <div class="container">
    <header>
      <h1>访客登记</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadRecords">刷新</button>
      </div>
    </header>

    <div class="card">
      <div class="form-row">
        <label>访客姓名</label>
        <input v-model="visitorName" />
      </div>
      <div class="form-row">
        <label>电话</label>
        <input v-model="phone" />
      </div>
      <button class="btn" @click="registerVisitor">登记</button>
    </div>

    <h2>我的访客记录</h2>
    <div class="list">
      <div v-for="record in records" :key="record.id" class="list-item">
        {{ record.visitorName }} - {{ record.phone }} - {{ record.visitTime }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentVisitor',
  data() {
    return {
      visitorName: '',
      phone: '',
      records: []
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    handleBack() {
      this.$router.push('/resident')
    },
    async registerVisitor() {
      if (!this.visitorName) return
      try {
        await axios.post('/visitor/register', {
          visitorName: this.visitorName,
          phone: this.phone
        })
        this.visitorName = ''
        this.phone = ''
        this.loadRecords()
      } catch (error) {
        console.error('登记失败:', error)
      }
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
.card {
  border: 1px solid #e0e0e0;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.form-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}
.list-item {
  padding: 8px;
  border-bottom: 1px solid #eee;
}
</style>
