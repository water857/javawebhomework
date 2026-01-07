<template>
  <div class="container">
    <header>
      <h1>私信会话列表</h1>
      <button class="btn" @click="fetchConversations">刷新</button>
    </header>

    <div class="list">
      <div v-if="conversations.length === 0" class="empty">暂无会话</div>
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
      conversations: []
    }
  },
  mounted() {
    this.fetchConversations()
  },
  methods: {
    async fetchConversations() {
      try {
        const response = await axios.get('/message/list')
        this.conversations = response.data.data || []
      } catch (error) {
        console.error('获取会话失败:', error)
      }
    },
    goChat(userId) {
      this.$router.push(`/resident/messages/${userId}`)
    }
  }
}
</script>

<style scoped>
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
  padding: 20px;
  color: #888;
}
</style>
