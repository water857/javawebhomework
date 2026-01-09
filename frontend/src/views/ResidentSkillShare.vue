<template>
  <div class="container">
    <header>
      <h1>技能交换</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="loadSkills">刷新</button>
      </div>
    </header>

    <div class="content-layout">
      <section class="panel">
        <h2>发布技能</h2>
        <div class="form-row">
          <label>技能名称</label>
          <input v-model="skillName" />
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
        <div v-if="skills.length === 0" class="empty">暂无技能信息，欢迎发布第一条。</div>
        <div v-else class="grid">
          <div v-for="skill in skills" :key="skill.id" class="card">
            <div class="card-header">
              <h3>{{ skill.skillName }}</h3>
              <span class="tag">技能互助</span>
            </div>
            <p class="desc">{{ skill.description }}</p>
            <p class="meta">联系方式：{{ skill.contact }}</p>
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
      skills: []
    }
  },
  mounted() {
    this.loadSkills()
  },
  methods: {
    handleBack() {
      this.$router.push('/resident')
    },
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
        this.loadSkills()
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
