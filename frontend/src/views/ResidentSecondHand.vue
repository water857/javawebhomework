<template>
  <div class="container">
    <header>
      <h1>二手市场</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadItems">刷新</button>
      </div>
    </header>

    <div class="content-layout">
      <section class="panel">
        <h2>发布物品</h2>
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
      </section>

      <section class="panel">
        <h2>最新列表</h2>
        <div v-if="items.length === 0" class="empty">暂无发布，快发布第一件物品吧。</div>
        <div v-else class="grid">
          <div v-for="item in items" :key="item.id" class="card">
            <div class="card-header">
              <h3>{{ item.title }}</h3>
              <span class="tag">{{ item.status || '在售' }}</span>
            </div>
            <p class="desc">{{ item.description }}</p>
            <p class="meta">价格：¥{{ item.price }}</p>
            <button class="btn" @click="markSold(item.id)">标记售出</button>
          </div>
        </div>
      </section>
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
    handleBack() {
      this.$router.push('/resident')
    },
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
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  background: #f5f5f5;
  color: #666;
}
.desc {
  margin: 4px 0;
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
