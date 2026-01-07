<template>
  <div class="container">
    <header>
      <h1>技能交换</h1>
      <button class="btn" @click="loadSkills">刷新</button>
    </header>

    <div class="card">
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
    </div>

    <div class="grid">
      <div v-for="skill in skills" :key="skill.id" class="card">
        <h3>{{ skill.skillName }}</h3>
        <p>{{ skill.description }}</p>
        <p>联系方式：{{ skill.contact }}</p>
      </div>
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
