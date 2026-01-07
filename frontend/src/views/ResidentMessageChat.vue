<template>
  <div class="container">
    <header>
      <h1>私信对话</h1>
      <button class="btn" @click="fetchMessages">刷新</button>
    </header>

    <div class="chat-box">
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
.chat-box {
  min-height: 300px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fff;
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
}
.input-row input {
  flex: 1;
  padding: 8px;
}
</style>
