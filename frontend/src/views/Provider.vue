<template>
  <div class="container">
    <header>
      <h1>服务商中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="role-features">
      <h2>服务商功能</h2>
      <div class="features-grid">
        <div class="feature-card">
          <h3>任务管理</h3>
          <p>查看和处理居民提交的服务订单</p>
          <button class="btn" @click="handleServiceManagement('tasks')">查看任务</button>
        </div>
        <div class="feature-card">
          <h3>服务记录</h3>
          <p>查看历史服务记录和详情</p>
          <button class="btn" @click="handleServiceManagement('records')">查看记录</button>
        </div>
        <div class="feature-card">
          <h3>服务评价</h3>
          <p>查看居民对服务的评价和反馈</p>
          <button class="btn" @click="handleServiceManagement('evaluations')">查看评价</button>
        </div>
        <div class="feature-card">
          <h3>收益统计</h3>
          <p>查看服务收益和业务数据</p>
          <button class="btn">查看统计</button>
        </div>
        <div class="feature-card">
          <h3>社区活动</h3>
          <p>查看和参与社区组织的各种活动</p>
          <button class="btn" @click="handleActivities">查看活动</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Provider',
  data() {
    return {
      username: ''
    }
  },
  mounted() {
    // 从本地存储获取用户名
    this.username = localStorage.getItem('username')
  },
  methods: {
    handleBack() {
      this.$router.push('/provider')
    },
    handleProfile() {
      // 跳转到个人中心页面
      this.$router.push('/provider/profile')
    },
    handleServiceManagement(tab) {
      // 跳转到服务管理界面，并根据参数显示相应标签页
      this.$router.push({ path: '/provider/service-management', query: { tab } })
    },
    handleActivities() {
      // 跳转到活动列表页面
      this.$router.push('/provider/activities')
    },
    handleLogout() {
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('realName')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
      localStorage.removeItem('address')
      localStorage.removeItem('idCard')
      
      // 跳转到登录页
      this.$router.push('/login')
    }
  }
}
</script>
