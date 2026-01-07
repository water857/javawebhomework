<template>
  <div class="container">
    <header>
      <h1>失物招领</h1>
      <button class="btn" @click="loadRecords">刷新</button>
    </header>

    <div class="card">
      <div class="form-row">
        <label>类型</label>
        <select v-model="type">
          <option value="lost">丢失</option>
          <option value="found">拾到</option>
        </select>
      </div>
      <div class="form-row">
        <label>标题</label>
        <input v-model="title" />
      </div>
      <div class="form-row">
        <label>描述</label>
        <textarea v-model="description"></textarea>
      </div>
      <div class="form-row">
        <label>联系方式</label>
        <input v-model="contact" />
      </div>
      <button class="btn" @click="publish">发布</button>
    </div>

    <div class="grid">
      <div v-for="record in records" :key="record.id" class="card">
        <h3>{{ record.title }}</h3>
        <p>类型：{{ record.type }}</p>
        <p>{{ record.description }}</p>
        <p>联系方式：{{ record.contact }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentLostFound',
  data() {
    return {
      type: 'lost',
      title: '',
      description: '',
      contact: '',
      records: []
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const response = await axios.get('/lost-found/list')
        this.records = response.data.data || []
      } catch (error) {
        console.error('加载列表失败:', error)
      }
    },
    async publish() {
      if (!this.title) return
      try {
        await axios.post('/lost-found/publish', {
          type: this.type,
          title: this.title,
          description: this.description,
          contact: this.contact
        })
        this.title = ''
        this.description = ''
        this.contact = ''
        this.loadRecords()
      } catch (error) {
        console.error('发布失败:', error)
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
  margin-bottom: 20px;
}
.form-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>
