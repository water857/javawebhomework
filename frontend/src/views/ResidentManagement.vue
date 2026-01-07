<template>
  <div class="container">
    <header>
      <h1>居民管理中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
        <button class="btn" @click="handleBackToHome">返回首页</button>
      </div>
    </header>
    
    <div class="management-container">
      <div class="management-header">
        <h2>社区居民用户列表</h2>
        <div class="search-filter-container">
          <input 
            type="text" 
            class="search-input" 
            placeholder="搜索居民..." 
            v-model="searchKeyword"
            @input="loadResidents"
          >
          <select 
            class="filter-select" 
            v-model="statusFilter"
            @change="loadResidents"
          >
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </div>
        <button class="btn primary" @click="handleAddResident">添加居民用户</button>
      </div>
      
      <div class="residents-table-container">
        <table class="residents-table">
          <thead>
            <tr>
              <th>用户ID</th>
              <th>用户名</th>
              <th>真实姓名</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>身份证号</th>
              <th>居住地址</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="resident in residents" :key="resident.id">
              <td>{{ resident.id }}</td>
              <td>{{ resident.username }}</td>
              <td>{{ resident.realName || '-' }}</td>
              <td>{{ resident.phone || '-' }}</td>
              <td>{{ resident.email || '-' }}</td>
              <td>{{ resident.idCard || '-' }}</td>
              <td>{{ resident.address || '-' }}</td>
              <td>
                <span :class="{'status-value': true, 'active': resident.status === 1, 'inactive': resident.status === 0}">
                  {{ resident.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn edit-btn" @click="handleEditResident(resident)">编辑</button>
                <button 
                  class="btn" 
                  :class="{'primary': resident.status === 0, 'danger': resident.status === 1}" 
                  @click="handleToggleStatus(resident)"
                >
                  {{ resident.status === 1 ? '禁用' : '启用' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 添加/编辑居民模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="handleCloseModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑居民用户' : '添加居民用户' }}</h3>
          <button class="close-btn" @click="handleCloseModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmitResident" class="resident-form">
            <div class="form-group">
              <label for="username">用户名</label>
              <input 
                type="text" 
                id="username" 
                v-model="residentForm.username" 
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
                v-model="residentForm.password" 
                placeholder="请输入密码"
                required
              >
            </div>
            <div class="form-group">
              <label for="realName">真实姓名</label>
              <input 
                type="text" 
                id="realName" 
                v-model="residentForm.realName" 
                placeholder="请输入真实姓名"
              >
            </div>
            <div class="form-group">
              <label for="phone">手机号</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="residentForm.phone" 
                placeholder="请输入手机号"
              >
            </div>
            <div class="form-group">
              <label for="email">邮箱</label>
              <input 
                type="email" 
                id="email" 
                v-model="residentForm.email" 
                placeholder="请输入邮箱"
              >
            </div>
            <div class="form-group">
              <label for="idCard">身份证号</label>
              <input 
                type="text" 
                id="idCard" 
                v-model="residentForm.idCard" 
                placeholder="请输入身份证号"
              >
            </div>
            <div class="form-group">
              <label for="address">居住地址</label>
              <textarea 
                id="address" 
                v-model="residentForm.address" 
                placeholder="请输入居住地址"
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
  name: 'ResidentManagement',
  data() {
    return {
      username: '',
      residents: [],
      showModal: false,
      isEditMode: false,
      residentForm: {
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
    this.username = this.getLocalStorageItem('username')
    // 加载居民用户列表
    this.loadResidents()
    
    // 启动定时器，每30秒更新一次数据
    this.timer = setInterval(() => {
      this.loadResidents()
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
    // 安全获取localStorage项的辅助函数
    getLocalStorageItem(key) {
      if (typeof localStorage !== 'undefined') {
        return localStorage.getItem(key)
      }
      return null
    },
    loadResidents() {
      // 调用后端API获取居民用户列表
      const token = this.getLocalStorageItem('token')
      const params = new URLSearchParams()
      if (this.searchKeyword) params.append('searchKeyword', this.searchKeyword)
      if (this.statusFilter) params.append('status', this.statusFilter)
      const queryString = params.toString() ? `?${params.toString()}` : ''
      
      fetch(`/api/residents${queryString}`, {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + token
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('获取居民列表失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          this.residents = data.data
        }
      })
      .catch(error => {
        console.error('获取居民列表失败:', error)
        alert('获取居民列表失败，请稍后重试')
      })
    },
    handleAddResident() {
      // 重置表单
      this.residentForm = {
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
    handleEditResident(resident) {
      // 填充表单数据
      this.residentForm = {
        id: resident.id,
        username: resident.username,
        password: '', // 编辑时不显示密码
        realName: resident.realName || '',
        phone: resident.phone || '',
        email: resident.email || '',
        idCard: resident.idCard || '',
        address: resident.address || ''
      }
      this.isEditMode = true
      this.showModal = true
    },
    handleCloseModal() {
      this.showModal = false
    },
    handleSubmitResident() {
      if (this.isEditMode) {
        this.updateResident()
      } else {
        this.addResident()
      }
    },
    addResident() {
      // 调用后端API添加居民用户
      fetch('/api/residents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.getLocalStorageItem('token')
        },
        body: JSON.stringify({
          username: this.residentForm.username,
          password: this.residentForm.password,
          realName: this.residentForm.realName,
          phone: this.residentForm.phone,
          email: this.residentForm.email,
          idCard: this.residentForm.idCard,
          address: this.residentForm.address
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('添加居民失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert('居民添加成功')
          this.showModal = false
          this.loadResidents() // 重新加载居民列表
        } else {
          alert('添加失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('添加居民失败:', error)
        alert('添加居民失败，请稍后重试')
      })
    },
    updateResident() {
      // 调用后端API更新居民用户信息
      fetch(`/api/residents/${this.residentForm.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.getLocalStorageItem('token')
        },
        body: JSON.stringify({
          realName: this.residentForm.realName,
          phone: this.residentForm.phone,
          email: this.residentForm.email,
          idCard: this.residentForm.idCard,
          address: this.residentForm.address
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('更新居民信息失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert('居民信息更新成功')
          this.showModal = false
          this.loadResidents() // 重新加载居民列表
        } else {
          alert('更新失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('更新居民信息失败:', error)
        alert('更新居民信息失败，请稍后重试')
      })
    },
    handleToggleStatus(resident) {
      // 确认操作
      const newStatus = resident.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      if (!confirm(`确定要${statusText}用户"${resident.username}"吗？`)) {
        return
      }
      
      // 调用后端API更新用户状态
      fetch(`/api/residents/${resident.id}?action=status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.getLocalStorageItem('token')
        },
        body: JSON.stringify({
          status: newStatus
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('更新用户状态失败')
        }
        return response.json()
      })
      .then(data => {
        if (data.status === 'success') {
          alert(`用户已成功${statusText}`)
          this.loadResidents() // 重新加载居民列表
        } else {
          alert('更新失败: ' + data.message)
        }
      })
      .catch(error => {
        console.error('更新用户状态失败:', error)
        alert('更新用户状态失败，请稍后重试')
      })
    },
    handleProfile() {
      // 跳转到个人中心页面
      this.$router.push('/admin/profile')
    },
    handleLogout() {
      // 清除本地存储的token和用户信息
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        localStorage.removeItem('realName')
        localStorage.removeItem('phone')
        localStorage.removeItem('email')
        localStorage.removeItem('address')
        localStorage.removeItem('idCard')
      }
      
      // 跳转到登录页
      this.$router.push('/login')
    },
    handleBackToHome() {
      // 获取用户角色
      const role = this.getLocalStorageItem('role')
      
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

.residents-table-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.residents-table {
  width: 100%;
  border-collapse: collapse;
}

.residents-table th, .residents-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.residents-table th {
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

.resident-form {
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