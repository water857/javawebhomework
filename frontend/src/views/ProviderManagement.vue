<template>
  <div class="container">
    <header>
      <h1>服务商管理中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
        <button class="btn" @click="handleBackToHome">返回首页</button>
      </div>
    </header>
    
    <div class="management-container">
      <div class="management-header">
        <h2>社区服务商列表</h2>
        <div class="search-filter-container">
          <input 
            type="text" 
            class="search-input" 
            placeholder="搜索服务商..." 
            v-model="searchKeyword"
            @input="loadProviders"
          >
          <select 
            class="filter-select" 
            v-model="statusFilter"
            @change="loadProviders"
          >
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </div>
        <button class="btn primary" @click="handleAddProvider">添加服务商</button>
      </div>
      
      <div class="providers-table-container">
        <table class="providers-table">
          <thead>
            <tr>
              <th>用户ID</th>
              <th>用户名</th>
              <th>真实姓名</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>身份证号</th>
              <th>服务地址</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="provider in providers" :key="provider.id">
              <td>{{ provider.id }}</td>
              <td>{{ provider.username }}</td>
              <td>{{ provider.realName || '-' }}</td>
              <td>{{ provider.phone || '-' }}</td>
              <td>{{ provider.email || '-' }}</td>
              <td>{{ provider.idCard || '-' }}</td>
              <td>{{ provider.address || '-' }}</td>
              <td>
                <span :class="{'status-value': true, 'active': provider.status === 1, 'inactive': provider.status === 0}">
                  {{ provider.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn edit-btn" @click="handleEditProvider(provider)">编辑</button>
                <button 
                  class="btn" 
                  :class="{'primary': provider.status === 0, 'danger': provider.status === 1}" 
                  @click="handleToggleStatus(provider)"
                >
                  {{ provider.status === 1 ? '禁用' : '启用' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 添加/编辑服务商模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="handleCloseModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑服务商' : '添加服务商' }}</h3>
          <button class="close-btn" @click="handleCloseModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmitProvider" class="provider-form">
            <div class="form-group">
              <label for="username">用户名</label>
              <input 
                type="text" 
                id="username" 
                v-model="providerForm.username" 
                :disabled="isEditMode"
                placeholder="请输入用户名"
                required
              >
            </div>
            <div class="form-group" v-if="!isEditMode">
              <label for="password">密码</label>
              <input 
                type="password" 
                id="password" 
                v-model="providerForm.password" 
                placeholder="请输入密码"
                required
              >
            </div>
            <div class="form-group">
              <label for="realName">真实姓名</label>
              <input 
                type="text" 
                id="realName" 
                v-model="providerForm.realName" 
                placeholder="请输入真实姓名"
              >
            </div>
            <div class="form-group">
              <label for="phone">手机号</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="providerForm.phone" 
                placeholder="请输入手机号"
              >
            </div>
            <div class="form-group">
              <label for="email">邮箱</label>
              <input 
                type="email" 
                id="email" 
                v-model="providerForm.email" 
                placeholder="请输入邮箱"
              >
            </div>
            <div class="form-group">
              <label for="idCard">身份证号</label>
              <input 
                type="text" 
                id="idCard" 
                v-model="providerForm.idCard" 
                placeholder="请输入身份证号"
              >
            </div>
            <div class="form-group">
              <label for="address">服务地址</label>
              <textarea 
                id="address" 
                v-model="providerForm.address" 
                placeholder="请输入服务地址"
                rows="3"
              ></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn" @click="handleCloseModal">取消</button>
              <button type="submit" class="btn primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProviderManagement',
  data() {
    return {
      username: '',
      providers: [],
      showModal: false,
      isEditMode: false,
      providerForm: {
        id: '',
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        idCard: '',
        address: ''
      },
      searchKeyword: '',
      statusFilter: '',
      timer: null // 定时器实例
    }
  },
  mounted() {
    // 从本地存储获取用户名
    this.username = localStorage.getItem('username')
    // 加载服务商列表
    this.loadProviders()
    
    // 启动定时器，每30秒更新一次数据
    this.timer = setInterval(() => {
      this.loadProviders()
    }, 30000)
  },
  beforeUnmount() {
    // 清除定时器，避免内存泄漏
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  methods: {
    loadProviders() {
      // 调用后端API获取服务商列表
      const token = localStorage.getItem('token')
      const params = new URLSearchParams()
      if (this.searchKeyword) params.append('searchKeyword', this.searchKeyword)
      if (this.statusFilter) params.append('status', this.statusFilter)
      const queryString = params.toString() ? `?${params.toString()}` : ''
      
      fetch(`/api/providers${queryString}`, {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + token
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('获取服务商列表失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          this.providers = data.data
        }
      })
      .catch(error => {
        console.error('获取服务商列表失败:', error)
        alert('获取服务商列表失败，请稍后重试')
      })
    },
    handleAddProvider() {
      // 重置表单
      this.providerForm = {
        id: '',
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        idCard: '',
        address: ''
      }
      this.isEditMode = false
      this.showModal = true
    },
    handleEditProvider(provider) {
      // 填充表单数据
      this.providerForm = {
        id: provider.id,
        username: provider.username,
        password: '', // 编辑时不显示密码
        realName: provider.realName || '',
        phone: provider.phone || '',
        email: provider.email || '',
        idCard: provider.idCard || '',
        address: provider.address || ''
      }
      this.isEditMode = true
      this.showModal = true
    },
    handleCloseModal() {
      this.showModal = false
    },
    handleSubmitProvider() {
      if (this.isEditMode) {
        this.updateProvider()
      } else {
        this.addProvider()
      }
    },
    addProvider() {
      // 调用后端API添加服务商
      fetch('/api/providers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          username: this.providerForm.username,
          password: this.providerForm.password,
          realName: this.providerForm.realName,
          phone: this.providerForm.phone,
          email: this.providerForm.email,
          idCard: this.providerForm.idCard,
          address: this.providerForm.address
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('添加服务商失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert('服务商添加成功')
          this.showModal = false
          this.loadProviders() // 重新加载服务商列表
        } else {
          alert('添加失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('添加服务商失败:', error)
        alert('添加服务商失败，请稍后重试')
      })
    },
    updateProvider() {
      // 调用后端API更新服务商信息
      fetch(`/api/providers/${this.providerForm.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          realName: this.providerForm.realName,
          phone: this.providerForm.phone,
          email: this.providerForm.email,
          idCard: this.providerForm.idCard,
          address: this.providerForm.address
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('更新服务商信息失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert('服务商信息更新成功')
          this.showModal = false
          this.loadProviders() // 重新加载服务商列表
        } else {
          alert('更新失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('更新服务商信息失败:', error)
        alert('更新服务商信息失败，请稍后重试')
      })
    },
    handleToggleStatus(provider) {
      // 确认操作
      const newStatus = provider.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      if (!confirm(`确定要${statusText}服务商"${provider.username}"吗？`)) {
        return
      }
      
      // 调用后端API更新用户状态
      fetch(`/api/providers/${provider.id}?action=status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          status: newStatus
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('更新服务商状态失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert(`服务商已成功${statusText}`)
          this.loadProviders() // 重新加载服务商列表
        } else {
          alert('更新失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('更新服务商状态失败:', error)
        alert('更新服务商状态失败，请稍后重试')
      })
    },
    handleProfile() {
      // 跳转到个人中心页面
      this.$router.push('/admin/profile')
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
    },
    handleBackToHome() {
      // 获取用户角色
      const role = localStorage.getItem('role')
      
      // 根据角色导航到对应的首页
      if (role === 'resident') {
        this.$router.push('/resident')
      } else if (role === 'property_admin') {
        this.$router.push('/admin')
      } else if (role === 'service_provider') {
        this.$router.push('/provider')
      } else {
        // 默认跳转到登录页
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.management-container {
  margin-top: 20px;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.search-filter-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  width: 250px;
}

.filter-select {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  background-color: #fff;
}

.providers-table-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.providers-table {
  width: 100%;
  border-collapse: collapse;
}

.providers-table th, .providers-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.providers-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

.status-value {
  padding: 5px 10px;
  border-radius: 4px;
  font-weight: bold;
}

.status-value.active {
  background-color: #d4edda;
  color: #155724;
}

.status-value.inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn.primary {
  background-color: #2196F3;
  color: white;
}

.btn.primary:hover {
  background-color: #1976D2;
}

.btn.danger {
  background-color: #f44336;
  color: white;
}

.btn.danger:hover {
  background-color: #d32f2f;
}

.btn.edit-btn {
  background-color: #FFC107;
  color: #333;
}

.btn.edit-btn:hover {
  background-color: #FFB300;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background-color: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.provider-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>