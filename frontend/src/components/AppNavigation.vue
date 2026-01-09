<template>
  <nav class="app-nav">
    <div class="nav-brand" @click="goHome">
      <span class="logo">🏘️</span>
      <span class="brand-text">智能社区服务平台</span>
    </div>
    <div class="nav-links">
      <router-link class="nav-link" :to="homeRoute">首页</router-link>
      <div class="nav-group" v-for="group in menuGroups" :key="group.label">
        <button class="nav-link nav-link-button" type="button">
          {{ group.label }}
          <span class="caret">▾</span>
        </button>
        <div class="nav-dropdown">
          <div v-if="group.items.length === 0" class="nav-empty">暂无可访问功能</div>
          <router-link
            v-for="item in group.items"
            :key="item.label"
            class="nav-dropdown-item"
            :to="item.path"
          >
            {{ item.label }}
          </router-link>
        </div>
      </div>
      <div class="nav-group">
        <button class="nav-link nav-link-button" type="button">
          智能通知
          <span class="caret">▾</span>
        </button>
        <div class="nav-dropdown">
          <router-link
            v-for="item in notificationItems"
            :key="item.label"
            class="nav-dropdown-item"
            :to="item.path"
          >
            {{ item.label }}
          </router-link>
        </div>
      </div>
    </div>
    <div class="nav-user">
      <div class="user-meta">
        <span class="user-name">{{ displayName }}</span>
        <span class="user-role">{{ roleLabel }}</span>
      </div>
      <div class="nav-group">
        <button class="nav-link nav-link-button" type="button">
          用户信息
          <span class="caret">▾</span>
        </button>
        <div class="nav-dropdown nav-dropdown-right">
          <button class="nav-dropdown-item" type="button" @click="goProfile">个人中心</button>
          <button class="nav-dropdown-item" type="button" @click="logout">退出登录</button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'AppNavigation',
  data() {
    return {
      role: '',
      username: '',
      realName: ''
    }
  },
  computed: {
    homeRoute() {
      if (this.role === 'property_admin') return '/admin'
      if (this.role === 'service_provider') return '/provider'
      return '/resident'
    },
    displayName() {
      return this.realName || this.username || '用户'
    },
    roleLabel() {
      if (this.role === 'property_admin') return '管理员'
      if (this.role === 'service_provider') return '服务商'
      return '居民'
    },
    menuGroups() {
      const serviceItems = []
      const neighborItems = []
      const activityItems = []

      if (this.role === 'property_admin') {
        serviceItems.push(
          { label: '报修管理', path: '/admin/repair-management' },
          { label: '物业费管理', path: '/admin/property-fee-management' },
          { label: '车位审批', path: '/resident/parking-admin' },
          { label: '访客记录', path: '/resident/visitor-admin' }
        )
        activityItems.push({ label: '活动管理', path: '/admin/activities' })
      } else if (this.role === 'service_provider') {
        serviceItems.push(
          { label: '服务预约', path: '/provider/service-management' }
        )
        activityItems.push({ label: '活动列表', path: '/provider/activities' })
      } else {
        serviceItems.push(
          { label: '报修', path: '/resident/repairs' },
          { label: '物业费', path: '/resident/property-fee' },
          { label: '停车位', path: '/resident/parking' },
          { label: '访客登记', path: '/resident/visitor' },
          { label: '服务预约', path: '/resident/repairs?tab=submit' }
        )
        neighborItems.push(
          { label: '邻里圈', path: '/resident/community' },
          { label: '私信', path: '/resident/messages' },
          { label: '二手市场', path: '/resident/second-hand' },
          { label: '失物招领', path: '/resident/lost-found' },
          { label: '技能交换', path: '/resident/skill-share' }
        )
        activityItems.push({ label: '活动列表', path: '/resident/activities' })
      }

      return [
        { label: '社区服务', items: serviceItems },
        { label: '邻里互动', items: neighborItems },
        { label: '社区活动', items: activityItems }
      ]
    },
    notificationItems() {
      const items = [{ label: '通知列表', path: '/notifications' }]
      if (this.role === 'property_admin') {
        items.push({ label: '公告管理', path: '/admin/announcements' })
      } else {
        items.push({ label: '社区公告', path: '/announcements' })
      }
      return items
    }
  },
  mounted() {
    this.refreshUser()
  },
  watch: {
    $route() {
      this.refreshUser()
    }
  },
  methods: {
    refreshUser() {
      this.role = localStorage.getItem('role') || 'resident'
      this.username = localStorage.getItem('username') || ''
      this.realName = localStorage.getItem('realName') || ''
    },
    goHome() {
      this.$router.push(this.homeRoute)
    },
    goProfile() {
      if (this.role === 'property_admin') {
        this.$router.push('/admin/profile')
      } else if (this.role === 'service_provider') {
        this.$router.push('/provider/profile')
      } else {
        this.$router.push('/resident/profile')
      }
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('realName')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
      localStorage.removeItem('address')
      localStorage.removeItem('idCard')
      this.$router.push('/login')
    }
  }
}
</script>
