<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">私信对话</div>
        <div class="page-subtitle">与对方交流详情，发送内容后将即时显示在聊天区。</div>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" @click="handleBackList">返回会话列表</button>
        <button class="btn btn-secondary" @click="fetchMessages">刷新</button>
      </div>
    </div>

    <div class="section-card">
      <div class="chat-box">
        <div v-if="messages.length === 0" class="empty-state">暂无消息，先发送一句问候吧。</div>
        <div v-for="msg in messages" :key="msg.id" :class="['bubble', msg.fromUserId === currentUserId ? 'bubble-right' : 'bubble-left']">
          {{ msg.content }}
        </div>
      </div>

      <div class="input-row">
        <input v-model="content" placeholder="输入消息" />
        <button class="btn btn-primary" @click="sendMessage">发送</button>
        <button class="btn btn-secondary" @click="markRead">标记已读</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ResidentMessageChat',
  data() {
    return {
      messages: [],
      content: '',
      currentUserId: Number(localStorage.getItem('userId')),
      otherUserId: Number(this.$route.params.userId)
    }
  },
  mounted() {
    this.fetchMessages()
  },
  methods: {
    handleBackList() {
      this.$router.push('/resident/messages')
    },
    async fetchMessages() {
      try {
        const response = await axios.get(`/message/chat/${this.otherUserId}`)
        this.messages = response.data.data || []
      } catch (error) {
        console.error('获取消息失败:', error)
      }
    },
    async sendMessage() {
      if (!this.content) return
      try {
        await axios.post('/message/send', {
          toUserId: this.otherUserId,
          content: this.content
        })
        this.content = ''
        this.fetchMessages()
      } catch (error) {
        console.error('发送失败:', error)
      }
    },
    async markRead() {
      try {
        await axios.post(`/message/read/${this.otherUserId}`)
      } catch (error) {
        console.error('标记失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.chat-box {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  min-height: 300px;
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.bubble {
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  max-width: 70%;
}
.bubble-right {
  align-self: flex-end;
  background: #dbeafe;
}
.bubble-left {
  align-self: flex-start;
  background: #e5e7eb;
}
.input-row {
  display: flex;
  gap: 0.75rem;
}
.input-row input {
  flex: 1;
}
</style>
