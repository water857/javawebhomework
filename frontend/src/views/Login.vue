<template>
  <div class="container">
    <h1>智能社区服务平台</h1>
    <div class="nav-buttons">
      <button class="btn btn-primary">登录</button>
      <router-link to="/register" class="btn btn-secondary">注册</router-link>
    </div>
    
    <!-- 登录表单 -->
    <div class="form-container">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="loginForm.username" placeholder="请输入用户名" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="loginForm.password" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%; margin-bottom: 1rem;">登录</button>
        <div v-if="message" :class="['message', messageType]">{{ message }}</div>
      </form>
    </div>
  </div>
</template>

<script>
import { apiRequest } from '../services/api.js'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      message: '',
      messageType: ''
    }
  },
  methods: {
    async handleLogin() {
      try {
        // 发送登录请求
        const data = await apiRequest('/login', 'POST', this.loginForm)
        
        // 保存token和userId到本地存储
        localStorage.setItem('token', data.data.token)
        localStorage.setItem('username', data.data.user.username)
        localStorage.setItem('role', data.data.user.role)
        localStorage.setItem('userId', data.data.user.id)
        
        this.message = '登录成功！欢迎回来，' + data.data.user.username + '！'
        this.messageType = 'success'
        
        // 获取完整用户资料
        const userData = await apiRequest('/user')
        // 保存完整用户资料到本地存储
        localStorage.setItem('userData', JSON.stringify(userData.data))
        
        // 根据角色跳转到不同页面
        setTimeout(() => {
          if (data.data.user.role === 'resident') {
            this.$router.push('/resident')
          } else if (data.data.user.role === 'property_admin') {
            this.$router.push('/admin')
          } else if (data.data.user.role === 'service_provider') {
            this.$router.push('/provider')
          }
        }, 1500)
      } catch (error) {
        this.message = '登录失败：' + error.message
        this.messageType = 'error'
      }
    }
  }
}
</script>