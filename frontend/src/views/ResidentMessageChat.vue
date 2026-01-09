<template>
  <div class="container">
    <header>
      <h1>私信对话</h1>
      <div class="header-actions">
        <button class="btn" @click="handleBackList">返回会话列表</button>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="fetchMessages">刷新</button>
      </div>
    </header>

    <div class="chat-layout">
      <div class="chat-box">
        <div v-if="messages.length === 0" class="empty">暂无消息，先发送一句问候吧。</div>
        <div v-for="msg in messages" :key="msg.id" :class="['bubble', msg.fromUserId === currentUserId ? 'bubble-right' : 'bubble-left']">
          {{ msg.content }}
        </div>
      </div>

      <div class="input-row">
        <input v-model="content" placeholder="输入消息" />
        <button class="btn" @click="sendMessage">发送</button>
        <button class="btn" @click="markRead">标记已读</button>
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
    handleBack() {
      this.$router.push('/resident')
    },
    handleBackList() {
      this.$router.push('/resident/messages')
    },
    async fetchMessages() {
      try {
        const response = await axios.get('/message/list', {
          params: { withUserId: this.otherUserId }
        })
        this.messages = response.data.data || []
      } catch (error) {
        console.error('获取对话失败:', error)
      }
    },
    async sendMessage() {
      if (!this.content) {
        return
      }
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
        await axios.post('/message/read', {
          withUserId: this.otherUserId
        })
        this.fetchMessages()
      } catch (error) {
        console.error('标记已读失败:', error)
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
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 70vh;
  margin-top: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: #fff;
}
.chat-box {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}
.bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 16px;
}
.bubble-left {
  align-self: flex-start;
  background: #f2f2f2;
}
.bubble-right {
  align-self: flex-end;
  background: #1677ff;
  color: #fff;
}
.input-row {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid #eee;
  position: sticky;
  bottom: 0;
  background: #fff;
}
.input-row input {
  flex: 1;
  padding: 8px;
}
.empty {
  color: #888;
  text-align: center;
  margin-top: 20px;
}
</style>
