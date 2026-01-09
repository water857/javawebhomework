<template>
  <div class="container">
    <header>
      <h1>私信会话列表</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="fetchConversations">刷新</button>
      </div>
    </header>

    <div class="card">
      <h2>发起新私信</h2>
      <p class="muted">输入对方用户ID即可发起聊天。</p>
      <div class="start-row">
        <input v-model.trim="newUserId" placeholder="对方用户ID" />
        <button class="btn" @click="startChat">发起聊天</button>
      </div>
    </div>

    <div class="list card">
      <h2>最近会话</h2>
      <div v-if="conversations.length === 0" class="empty">
        暂无会话，您可以在上方输入用户ID发起第一条私信。
      </div>
      <div v-for="item in conversations" :key="item.otherUserId" class="list-item">
        <div>
          <div class="title">{{ item.otherRealName || item.otherUsername }}</div>
          <div class="subtitle">{{ item.lastMessage }}</div>
        </div>
        <div class="actions">
          <span class="badge" v-if="item.unreadCount">{{ item.unreadCount }}</span>
          <button class="btn" @click="goChat(item.otherUserId)">进入对话</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentMessages',
  data() {
    return {
      conversations: [],
      newUserId: ''
    }
  },
  mounted() {
    this.fetchConversations()
  },
  methods: {
    handleBack() {
      this.$router.push('/resident')
    },
    async fetchConversations() {
      try {
        const response = await axios.get('/message/list')
        this.conversations = response.data.data || []
      } catch (error) {
        console.error('获取会话失败:', error)
      }
    },
    startChat() {
      if (!this.newUserId) return
      this.$router.push(`/resident/messages/${this.newUserId}`)
    },
    goChat(userId) {
      this.$router.push(`/resident/messages/${userId}`)
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
  border-radius: 10px;
  margin-top: 16px;
  background: #fff;
}
.muted {
  color: #666;
  font-size: 14px;
  margin: 4px 0 12px;
}
.start-row {
  display: flex;
  gap: 10px;
}
.start-row input {
  flex: 1;
  padding: 8px 10px;
}
.list {
  margin-top: 20px;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
}
.subtitle {
  color: #666;
  font-size: 14px;
}
.badge {
  background-color: #ff4d4f;
  color: #fff;
  border-radius: 12px;
  padding: 2px 8px;
  margin-right: 10px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.empty {
  padding: 16px 8px;
  color: #888;
}
</style>
