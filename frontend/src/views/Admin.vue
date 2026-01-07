<template>
  <div class="container">
    <header>
      <h1>物业管理员中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="role-features">
        <h2>管理员功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <h3>用户管理</h3>
            <p>管理社区居民和服务商账户</p>
            <div style="display: flex; gap: 10px; margin-top: 10px;">
              <button class="btn" @click="handleUserManagement">管理居民</button>
              <button class="btn" @click="handleProviderManagement">管理服务商</button>
            </div>
          </div>
        <div class="feature-card">
          <h3>报修处理</h3>
          <p>处理居民提交的维修申请</p>
          <button class="btn" @click="handleRepairManagement">查看报修</button>
        </div>
        <div class="feature-card">
          <h3>活动管理</h3>
          <p>发布和管理社区活动</p>
          <div style="display: flex; gap: 10px; margin-top: 10px;">
            <button class="btn" @click="handleActivities">查看活动</button>
            <button class="btn" @click="handlePublishActivity">发布活动</button>
          </div>
        </div>
        <div class="feature-card">
          <h3>公告发布</h3>
          <p>发布社区通知和活动信息</p>
          <div style="display: flex; gap: 10px; margin-top: 10px;">
            <button class="btn" @click="handleAnnouncements">管理公告</button>
            <button class="btn" @click="handlePublishAnnouncement">发布公告</button>
          </div>
        </div>
        <div class="feature-card">
          <h3>费用管理</h3>
          <p>管理居民物业费和水电费账单</p>
          <div style="display: flex; gap: 10px; margin-top: 10px;">
            <button class="btn" @click="handlePropertyFeeManagement">账单管理</button>
            <button class="btn" @click="handlePropertyFeeStatistics">统计分析</button>
            <button class="btn" @click="handlePropertyFeeReconciliation">对账管理</button>
          </div>
        </div>
        <div class="feature-card">
          <h3>车位审批</h3>
          <p>审核居民车位申请并释放车位</p>
          <button class="btn" @click="handleParkingAdmin">进入审批</button>
        </div>
        <div class="feature-card">
          <h3>访客记录</h3>
          <p>查看全部访客登记信息</p>
          <button class="btn" @click="handleVisitorAdmin">查看记录</button>
        </div>
        <div class="feature-card">
          <h3>活动回顾</h3>
          <p>填写活动回顾并展示</p>
          <button class="btn" @click="handleActivityReview">填写回顾</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Admin',
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
    handleProfile() {
      // 跳转到个人中心页面
      this.$router.push('/admin/profile')
    },
    handleUserManagement() {
      // 跳转到居民管理页面
      this.$router.push('/admin/resident-management')
    },
    handleProviderManagement() {
      // 跳转到服务商管理页面
      this.$router.push('/admin/provider-management')
    },
    handleRepairManagement() {
      // 跳转到报修管理页面
      this.$router.push('/admin/repair-management')
    },
    handleActivities() {
      // 跳转到活动管理页面
      this.$router.push('/admin/activities')
    },
    handlePublishActivity() {
      // 跳转到发布活动页面
      this.$router.push('/admin/activities/publish')
    },
    handleAnnouncements() {
      // 跳转到公告管理页面
      this.$router.push('/admin/announcements')
    },
    handlePublishAnnouncement() {
      // 跳转到发布公告页面
      this.$router.push('/admin/announcements/publish')
    },
    handlePropertyFeeManagement() {
      // 跳转到物业费账单管理页面
      this.$router.push('/admin/property-fee-management')
    },
    handlePropertyFeeStatistics() {
      // 跳转到物业费统计分析页面
      this.$router.push('/admin/property-fee-statistics')
    },
    handlePropertyFeeReconciliation() {
      // 跳转到物业费对账管理页面
      this.$router.push('/admin/property-fee-reconciliation')
    },
    handleParkingAdmin() {
      this.$router.push('/resident/parking-admin')
    },
    handleVisitorAdmin() {
      this.$router.push('/resident/visitor-admin')
    },
    handleActivityReview() {
      this.$router.push('/resident/activity-review')
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
