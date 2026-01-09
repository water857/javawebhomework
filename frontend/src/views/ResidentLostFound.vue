<template>
  <div class="container">
    <header>
      <h1>失物招领</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadRecords">刷新</button>
      </div>
    </header>

    <div class="content-layout">
      <section class="panel">
        <h2>发布信息</h2>
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
      </section>

      <section class="panel">
        <h2>最新列表</h2>
        <div v-if="records.length === 0" class="empty">暂无记录，发布第一条信息吧。</div>
        <div v-else class="grid">
          <div v-for="record in records" :key="record.id" class="card">
            <div class="card-header">
              <h3>{{ record.title }}</h3>
              <span class="tag">{{ record.type === 'lost' ? '丢失' : '拾到' }}</span>
            </div>
            <p class="desc">{{ record.description }}</p>
            <p class="meta">联系方式：{{ record.contact }}</p>
          </div>
        </div>
      </section>
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
    handleBack() {
      this.$router.push('/resident')
    },
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
.header-actions {
  display: flex;
  gap: 10px;
}
.content-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.panel {
  border: 1px solid #e0e0e0;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
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
.card {
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 12px;
  background: #fafafa;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #e6f7ff;
  color: #1677ff;
}
.desc {
  margin: 8px 0;
  color: #444;
}
.meta {
  color: #666;
  font-size: 13px;
}
.empty {
  color: #888;
  padding: 12px;
}
</style>
