<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">技能交换</div>
        <div class="page-subtitle">分享技能，互助学习，社区一起成长。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="loadSkills">刷新列表</button>
      </div>
    </div>

    <div v-if="notice" class="info-banner">
      <span>{{ notice }}</span>
      <button class="btn btn-secondary" @click="notice = ''">知道了</button>
    </div>

    <div class="card-grid">
      <section class="section-card">
        <div class="section-title">发布技能</div>
        <div class="form-row">
          <label>技能名称</label>
          <input v-model.trim="skillName" placeholder="例如：钢琴陪练" />
        </div>
        <div class="form-row">
          <label>描述</label>
          <textarea v-model.trim="description" placeholder="描述可提供的帮助" ></textarea>
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
          <input v-model.trim="keyword" placeholder="搜索技能或描述" />
        </div>
        <div v-if="filteredSkills.length === 0" class="empty-state">暂无技能信息，欢迎发布第一条。</div>
        <div v-else class="card-grid">
          <div v-for="skill in filteredSkills" :key="skill.id" class="card">
            <div class="card-header">
              <h3>{{ skill.skillName }}</h3>
              <span class="tag">技能互助</span>
            </div>
            <p class="desc">{{ skill.description || '暂无描述' }}</p>
            <p class="meta">联系方式：{{ skill.contact || '暂无' }}</p>
            <p class="meta">发布者：{{ skill.publisherName || skill.publisher_name || '社区居民' }}</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentSkillShare',
  data() {
    return {
      skillName: '',
      description: '',
      contact: '',
      skills: [],
      keyword: '',
      notice: ''
    }
  },
  computed: {
    filteredSkills() {
      return this.skills.filter(skill => {
        return this.keyword
          ? `${skill.skillName}${skill.description || ''}`.includes(this.keyword)
          : true
      })
    }
  },
  mounted() {
    this.loadSkills()
  },
  methods: {
    async loadSkills() {
      try {
        const response = await axios.get('/skills/list')
        this.skills = response.data.data || []
      } catch (error) {
        console.error('加载列表失败:', error)
      }
    },
    async publish() {
      if (!this.skillName) return
      try {
        await axios.post('/skills/publish', {
          skillName: this.skillName,
          description: this.description,
          contact: this.contact
        })
        this.skillName = ''
        this.description = ''
        this.contact = ''
        this.notice = '发布成功，已同步到技能交换列表。'
        this.loadSkills()
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
