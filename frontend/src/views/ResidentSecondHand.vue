<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">二手市场</div>
        <div class="page-subtitle">发布闲置好物，帮助邻里循环利用。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="loadItems">刷新列表</button>
      </div>
    </div>

    <div v-if="notice" class="info-banner">
      <span>{{ notice }}</span>
      <button class="btn btn-secondary" @click="notice = ''">知道了</button>
    </div>

    <div class="card-grid">
      <section class="section-card">
        <div class="section-title">发布物品</div>
        <div class="form-row">
          <label>标题</label>
          <input v-model.trim="title" placeholder="例如：九成新电饭煲" />
        </div>
        <div class="form-row">
          <label>描述</label>
          <textarea v-model.trim="description" placeholder="简单描述物品状况"></textarea>
        </div>
        <div class="form-row">
          <label>价格</label>
          <input type="number" v-model.number="price" />
        </div>
        <button class="btn btn-primary" @click="publish">发布</button>
      </section>

      <section class="section-card">
        <div class="section-title">最新列表</div>
        <div class="search-bar">
          <input v-model.trim="keyword" placeholder="搜索标题或描述" />
          <select v-model="statusFilter">
            <option value="">全部状态</option>
            <option value="available">在售</option>
            <option value="sold">已售出</option>
          </select>
        </div>
        <div v-if="filteredItems.length === 0" class="empty-state">暂无发布，快发布第一件物品吧。</div>
        <div v-else class="card-grid">
          <div v-for="item in filteredItems" :key="item.id" class="card">
            <div class="card-header">
              <h3>{{ item.title }}</h3>
              <span class="status-pill" :class="item.status === 'sold' ? 'success' : 'info'">
                {{ item.status === 'sold' ? '已售出' : '在售' }}
              </span>
            </div>
            <p class="desc">{{ item.description || '暂无描述' }}</p>
            <p class="meta">价格：¥{{ item.price }}</p>
            <p class="meta">发布者：{{ item.publisherName || item.publisher_name || '社区居民' }}</p>
            <button class="btn btn-secondary" @click="markSold(item.id)" :disabled="item.status === 'sold'">标记售出</button>
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
      items: [],
      keyword: '',
      statusFilter: '',
      notice: ''
    }
  },
  computed: {
    filteredItems() {
      return this.items.filter(item => {
        const matchesKeyword = this.keyword
          ? `${item.title}${item.description || ''}`.includes(this.keyword)
          : true
        const matchesStatus = this.statusFilter ? item.status === this.statusFilter : true
        return matchesKeyword && matchesStatus
      })
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
        this.notice = '发布成功，已同步到社区二手列表。'
        this.loadItems()
      } catch (error) {
        console.error('发布失败:', error)
      }
    },
    async markSold(itemId) {
      try {
        await axios.post('/second-hand/mark-sold', { itemId })
        this.notice = '已标记售出，感谢分享。'
        this.loadItems()
      } catch (error) {
        console.error('更新状态失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.form-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}
.desc {
  color: #4b5563;
  margin-bottom: 0.5rem;
}
.meta {
  color: #6b7280;
  font-size: 0.9rem;
  margin-bottom: 0.35rem;
}
</style>
