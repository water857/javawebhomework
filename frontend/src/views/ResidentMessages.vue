<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">私信会话</div>
        <div class="page-subtitle">选择会话进入聊天，或输入用户 ID 发起新对话。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="fetchConversations">刷新会话</button>
      </div>
    </div>

    <div class="section-card" style="margin-bottom: 1.5rem;">
      <div class="section-title">快速发起私信</div>
      <p class="muted">提示：输入对方的用户 ID，即可创建会话并进入聊天。</p>
      <div class="start-row">
        <input v-model.trim="newUserId" placeholder="对方用户ID" />
        <button class="btn btn-primary" @click="startChat">发起聊天</button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">最近会话</div>
      <div v-if="conversations.length === 0" class="empty-state">
        暂无会话，您可以在上方输入用户ID发起第一条私信。
      </div>
      <div v-else class="list">
        <div v-for="item in conversations" :key="item.otherUserId" class="list-item">
          <div>
            <div class="title">{{ item.otherRealName || item.otherUsername }}</div>
            <div class="subtitle">{{ item.lastMessage }}</div>
          </div>
          <div class="actions">
            <span class="badge" v-if="item.unreadCount">{{ item.unreadCount }}</span>
            <button class="btn btn-secondary" @click="goChat(item.otherUserId)">进入对话</button>
          </div>
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
.muted {
  color: #6b7280;
  font-size: 0.9rem;
  margin: 0.25rem 0 0.75rem;
}
.start-row {
  display: flex;
  gap: 10px;
}
.start-row input {
  flex: 1;
}
.list {
  margin-top: 1rem;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
}
.subtitle {
  color: #6b7280;
  font-size: 0.9rem;
}
.actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.badge {
  background: #fee2e2;
  color: #b91c1c;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  font-size: 0.75rem;
}
</style>
