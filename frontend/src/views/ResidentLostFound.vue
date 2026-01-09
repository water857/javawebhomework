<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">失物招领</div>
        <div class="page-subtitle">让丢失的物品尽快回到主人身边。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="loadRecords">刷新列表</button>
      </div>
    </div>

    <div v-if="notice" class="info-banner">
      <span>{{ notice }}</span>
      <button class="btn btn-secondary" @click="notice = ''">知道了</button>
    </div>

    <div class="card-grid">
      <section class="section-card">
        <div class="section-title">发布信息</div>
        <div class="form-row">
          <label>类型</label>
          <select v-model="type">
            <option value="lost">丢失</option>
            <option value="found">拾到</option>
          </select>
        </div>
        <div class="form-row">
          <label>标题</label>
          <input v-model.trim="title" placeholder="例如：丢失蓝色雨伞" />
        </div>
        <div class="form-row">
          <label>描述</label>
          <textarea v-model.trim="description" placeholder="描述丢失或拾到物品的细节"></textarea>
        </div>
        <div class="form-row">
          <label>联系方式</label>
          <input v-model.trim="contact" placeholder="电话/微信" />
        </div>
        <button class="btn btn-primary" @click="publish">发布</button>
      </section>

      <section class="section-card">
        <div class="section-title">最新列表</div>
        <div class="search-bar">
          <input v-model.trim="keyword" placeholder="搜索标题或描述" />
          <select v-model="typeFilter">
            <option value="">全部类型</option>
            <option value="lost">丢失</option>
            <option value="found">拾到</option>
          </select>
        </div>
        <div v-if="filteredRecords.length === 0" class="empty-state">暂无记录，发布第一条信息吧。</div>
        <div v-else class="card-grid">
          <div v-for="record in filteredRecords" :key="record.id" class="card">
            <div class="card-header">
              <h3>{{ record.title }}</h3>
              <span class="tag">{{ record.type === 'lost' ? '丢失' : '拾到' }}</span>
            </div>
            <p class="desc">{{ record.description || '暂无描述' }}</p>
            <p class="meta">联系方式：{{ record.contact || '暂无' }}</p>
            <p class="meta">发布者：{{ record.publisherName || record.publisher_name || '社区居民' }}</p>
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
      records: [],
      keyword: '',
      typeFilter: '',
      notice: ''
    }
  },
  computed: {
    filteredRecords() {
      return this.records.filter(record => {
        const matchesKeyword = this.keyword
          ? `${record.title}${record.description || ''}`.includes(this.keyword)
          : true
        const matchesType = this.typeFilter ? record.type === this.typeFilter : true
        return matchesKeyword && matchesType
      })
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
        this.notice = '发布成功，已同步到失物招领列表。'
        this.loadRecords()
      } catch (error) {
        console.error('发布失败:', error)
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
