<template>
  <div class="container">
    <header>
      <h1>二手市场</h1>
      <button class="btn" @click="loadItems">刷新</button>
    </header>

    <div class="card">
      <div class="form-row">
        <label>标题</label>
        <input v-model="title" />
      </div>
      <div class="form-row">
        <label>描述</label>
        <textarea v-model="description"></textarea>
      </div>
      <div class="form-row">
        <label>价格</label>
        <input type="number" v-model.number="price" />
      </div>
      <button class="btn" @click="publish">发布</button>
    </div>

    <div class="grid">
      <div v-for="item in items" :key="item.id" class="card">
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
        <p>价格：¥{{ item.price }}</p>
        <p>状态：{{ item.status }}</p>
        <button class="btn" @click="markSold(item.id)">标记售出</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentSecondHand',
  data() {
    return {
      title: '',
      description: '',
      price: 0,
      items: []
    }
  },
  mounted() {
    this.loadItems()
  },
  methods: {
    async loadItems() {
      try {
        const response = await axios.get('/second-hand/list')
        this.items = response.data.data || []
      } catch (error) {
        console.error('加载列表失败:', error)
      }
    },
    async publish() {
      if (!this.title) return
      try {
        await axios.post('/second-hand/publish', {
          title: this.title,
          description: this.description,
          price: this.price
        })
        this.title = ''
        this.description = ''
        this.price = 0
        this.loadItems()
      } catch (error) {
        console.error('发布失败:', error)
      }
    },
    async markSold(itemId) {
      try {
        await axios.post('/second-hand/mark-sold', { itemId })
        this.loadItems()
      } catch (error) {
        console.error('更新状态失败:', error)
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
