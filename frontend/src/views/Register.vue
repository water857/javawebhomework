<template>
  <div class="container">
    <h1>智能社区服务平台</h1>
    <div class="nav-buttons">
      <router-link to="/login" class="btn btn-secondary">登录</router-link>
      <button class="btn btn-primary">注册</button>
    </div>
    
    <!-- 注册表单 -->
    <div class="form-container">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="registerForm.username" placeholder="请输入用户名" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="registerForm.password" placeholder="请输入密码" required>
        </div>
        <div class="form-group">
          <label for="realName">真实姓名</label>
          <input type="text" id="realName" v-model="registerForm.realName" placeholder="请输入真实姓名">
        </div>
        <div class="form-group">
          <label for="phone">手机号码</label>
          <input type="tel" id="phone" v-model="registerForm.phone" placeholder="请输入手机号码">
        </div>
        <div class="form-group">
          <label for="email">邮箱</label>
          <input type="email" id="email" v-model="registerForm.email" placeholder="请输入邮箱">
        </div>
        <div class="form-group">
          <label for="role">用户角色</label>
          <select id="role" v-model="registerForm.role" required>
            <option value="">请选择角色</option>
            <option value="resident">居民</option>
            <option value="property_admin">物业管理员</option>
            <option value="service_provider">服务商</option>
          </select>
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%; margin-bottom: 1rem;">注册</button>
        <div v-if="message" :class="['message', messageType]">{{ message }}</div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        role: ''
      },
      message: '',
      messageType: ''
    }
  },
  methods: {
    handleRegister() {
      // 验证角色选择
      if (!this.registerForm.role) {
        this.message = '请选择用户角色！'
        this.messageType = 'error'
        return
      }
      
      // 发送注册请求
      fetch('/api/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.registerForm)
      })
      .then(response => {
        if (response.ok) {
          return response.json()
        } else {
          return response.text().then(text => {
            throw new Error(text || '注册失败')
          })
        }
      })
      .then(data => {
        this.message = '注册成功！您的用户ID是：' + data.userId
        this.messageType = 'success'
        
        // 清空表单
        this.registerForm = {
          username: '',
          password: '',
          realName: '',
          phone: '',
          email: '',
          role: ''
        }
        
        // 自动切换到登录页面
        setTimeout(() => {
          this.$router.push('/login')
        }, 1500)
      })
      .catch(error => {
        this.message = '注册失败：' + error.message
        this.messageType = 'error'
      })
    }
  }
}
</script>