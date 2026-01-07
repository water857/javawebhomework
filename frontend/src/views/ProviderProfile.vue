<template>
  <div class="container">
    <header>
      <h1>服务商个人中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ userData.realName || userData.username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="profile-container">
      <aside class="profile-sidebar">
        <nav>
          <ul>
            <li :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">
              <i class="icon-user"></i> 个人资料
            </li>
            <li :class="{ active: activeTab === 'verify' }" @click="activeTab = 'verify'">
              <i class="icon-idcard"></i> 实名认证
            </li>
            <li :class="{ active: activeTab === 'password' }" @click="activeTab = 'password'">
              <i class="icon-lock"></i> 修改密码
            </li>
          </ul>
        </nav>
      </aside>
      
      <main class="profile-content">
        <!-- 个人资料管理 -->
        <div v-if="activeTab === 'profile'" class="tab-content">
          <h2>个人资料管理</h2>
          <form @submit.prevent="updateProfile" class="profile-form">
            <div class="form-group">
              <label for="username">账号</label>
              <input type="text" id="username" v-model="userData.username" disabled>
            </div>
            <div class="form-group">
              <label for="realName">真实姓名</label>
              <input type="text" id="realName" v-model="userData.realName" placeholder="请输入真实姓名">
              <span v-if="errors.realName" class="error-message">{{ errors.realName }}</span>
            </div>
            <div class="form-group">
              <label for="phone">手机号</label>
              <input type="tel" id="phone" v-model="userData.phone" placeholder="请输入手机号">
              <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
            </div>
            <div class="form-group">
              <label for="email">邮箱</label>
              <input type="email" id="email" v-model="userData.email" placeholder="请输入邮箱">
              <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
            </div>
            <div class="form-group">
              <label for="address">居住地址</label>
              <textarea id="address" v-model="userData.address" placeholder="请输入居住地址"></textarea>
              <span v-if="errors.address" class="error-message">{{ errors.address }}</span>
            </div>
            <div class="form-group">
              <label for="role">身份</label>
              <input type="text" id="role" v-model="userData.role" disabled>
            </div>
            <button type="submit" class="btn primary">保存修改</button>
          </form>
        </div>
        
        <!-- 实名认证状态 -->
        <div v-if="activeTab === 'verify'" class="tab-content">
          <h2>实名认证状态</h2>
          <div class="verify-status">
            <div class="status-item">
              <span class="status-label">身份证号：</span>
              <span v-if="userData.idCard" class="status-value">{{ userData.idCard }}</span>
              <span v-else class="status-value not-filled">未填写</span>
            </div>
            <div class="status-item">
              <span class="status-label">认证状态：</span>
              <span :class="{ 'status-value': true, 'verified': userData.idCard, 'not-verified': !userData.idCard }">
                {{ userData.idCard ? '已认证' : '未认证' }}
              </span>
            </div>
          </div>
          <form @submit.prevent="updateIdCard" class="id-card-form">
            <div class="form-group">
              <label for="idCard">身份证号</label>
              <input type="text" id="idCard" v-model="userData.idCard" placeholder="请输入身份证号">
              <span v-if="errors.idCard" class="error-message">{{ errors.idCard }}</span>
            </div>
            <button type="submit" class="btn primary">保存身份证号</button>
          </form>
        </div>
        
        <!-- 修改密码 -->
        <div v-if="activeTab === 'password'" class="tab-content">
          <h2>修改密码</h2>
          <form @submit.prevent="changePassword" class="password-form">
            <div class="form-group">
              <label for="oldPassword">原密码</label>
              <input type="password" id="oldPassword" v-model="passwordData.oldPassword" placeholder="请输入原密码">
            </div>
            <div class="form-group">
              <label for="newPassword">新密码</label>
              <input type="password" id="newPassword" v-model="passwordData.newPassword" placeholder="请输入新密码">
              <span v-if="errors.newPassword" class="error-message">{{ errors.newPassword }}</span>
            </div>
            <div class="form-group">
              <label for="confirmPassword">确认新密码</label>
              <input type="password" id="confirmPassword" v-model="passwordData.confirmPassword" placeholder="请再次输入新密码">
              <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
            </div>
            <button type="submit" class="btn primary">修改密码</button>
          </form>
        </div>
      </main>
    </div>
    
    <!-- 反馈组件 -->
    <Feedback ref="feedback" />
  </div>
</template>

<script>
import { userApi } from '../services/api'
import { 
  validatePhone, 
  validateEmail, 
  validateIdCard, 
  validatePassword, 
  validateRealName, 
  validateAddress,
  validationMessages 
} from '../utils/validators'
import Feedback from '../components/Feedback.vue'

export default {
  name: 'ProviderProfile',
  components: {
    Feedback
  },
  data() {
    return {
      activeTab: 'profile',
      userData: {
        username: '',
        realName: '',
        phone: '',
        email: '',
        address: '',
        idCard: '',
        role: 'service_provider'
      },
      passwordData: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      errors: {
        realName: '',
        phone: '',
        email: '',
        address: '',
        idCard: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  mounted() {
    // 从本地存储获取用户信息
    this.loadUserData()
  },
  methods: {
    // 安全获取localStorage项的辅助函数
    getLocalStorageItem(key) {
      if (typeof localStorage !== 'undefined') {
        return localStorage.getItem(key)
      }
      return null
    },
    // 安全设置localStorage项的辅助函数
    setLocalStorageItem(key, value) {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(key, value)
      }
    },
    async loadUserData() {
      try {
        this.$refs.feedback.showLoadingMessage('加载用户信息中...')
        // 使用统一的API服务获取用户信息
        const data = await userApi.getUserInfo()
        // 检查响应是否成功，支持code或status字段
        if (data.code === 'success' || data.status === 'success') {
          // 更新用户数据
          this.userData = data.data
          // 同时更新localStorage，保持数据同步
        this.setLocalStorageItem('userData', JSON.stringify(data.data))
          this.$refs.feedback.showSuccessMessage('用户信息加载成功')
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        this.$refs.feedback.showErrorMessage(`获取用户信息失败: ${error.message}`)
        // 如果API调用失败，再尝试从localStorage获取
        const storedUserData = this.getLocalStorageItem('userData')
        if (storedUserData) {
          this.userData = JSON.parse(storedUserData)
        } else {
          const username = this.getLocalStorageItem('username')
          this.userData.username = username
        }
      } finally {
        this.$refs.feedback.hideLoading()
      }
    },
    // 验证个人资料表单
    validateProfileForm() {
      let isValid = true
      this.errors = {
        realName: '',
        phone: '',
        email: '',
        address: ''
      }

      if (this.userData.realName && !validateRealName(this.userData.realName)) {
        this.errors.realName = validationMessages.realName
        isValid = false
      }

      if (this.userData.phone && !validatePhone(this.userData.phone)) {
        this.errors.phone = validationMessages.phone
        isValid = false
      }

      if (this.userData.email && !validateEmail(this.userData.email)) {
        this.errors.email = validationMessages.email
        isValid = false
      }

      if (this.userData.address && !validateAddress(this.userData.address)) {
        this.errors.address = validationMessages.address
        isValid = false
      }

      return isValid
    },
    async updateProfile() {
      if (!this.validateProfileForm()) {
        return
      }

      try {
        this.$refs.feedback.showLoadingMessage('保存个人资料中...')
        // 使用统一的API服务更新个人资料
        const data = await userApi.updateProfile({
          realName: this.userData.realName,
          phone: this.userData.phone,
          email: this.userData.email,
          address: this.userData.address
        })
        
        if (data.code === 'success') {
          localStorage.setItem('userData', JSON.stringify(this.userData))
          this.$refs.feedback.showSuccessMessage(data.message || '个人资料已更新')
        } else {
          this.$refs.feedback.showErrorMessage(data.message || '更新失败')
        }
      } catch (error) {
        console.error('更新个人资料失败:', error)
        this.$refs.feedback.showErrorMessage(`更新个人资料失败: ${error.message}`)
      } finally {
        this.$refs.feedback.hideLoading()
      }
    },
    // 验证身份证表单
    validateIdCardForm() {
      let isValid = true
      this.errors.idCard = ''

      if (!this.userData.idCard) {
        this.errors.idCard = '请输入身份证号'
        isValid = false
      } else if (!validateIdCard(this.userData.idCard)) {
        this.errors.idCard = validationMessages.idCard
        isValid = false
      }

      return isValid
    },
    async updateIdCard() {
      if (!this.validateIdCardForm()) {
        return
      }

      try {
        this.$refs.feedback.showLoadingMessage('保存身份证信息中...')
        // 使用统一的API服务更新身份证信息
        const data = await userApi.updateIdCard({
          idCard: this.userData.idCard
        })
        
        if (data.code === 'success') {
          localStorage.setItem('userData', JSON.stringify(this.userData))
          this.$refs.feedback.showSuccessMessage(data.message || '身份证信息已更新')
        } else {
          this.$refs.feedback.showErrorMessage(data.message || '更新失败')
        }
      } catch (error) {
        console.error('更新身份证信息失败:', error)
        this.$refs.feedback.showErrorMessage(`更新身份证信息失败: ${error.message}`)
      } finally {
        this.$refs.feedback.hideLoading()
      }
    },
    // 验证密码表单
    validatePasswordForm() {
      let isValid = true
      this.errors = {
        newPassword: '',
        confirmPassword: ''
      }

      if (!this.passwordData.oldPassword) {
        this.$refs.feedback.showErrorMessage('请输入原密码')
        isValid = false
      }

      if (!this.passwordData.newPassword) {
        this.errors.newPassword = '请输入新密码'
        isValid = false
      } else if (!validatePassword(this.passwordData.newPassword)) {
        this.errors.newPassword = validationMessages.password
        isValid = false
      }

      if (!this.passwordData.confirmPassword) {
        this.errors.confirmPassword = '请确认新密码'
        isValid = false
      } else if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
        this.errors.confirmPassword = validationMessages.confirmPassword
        isValid = false
      }

      return isValid
    },
    async changePassword() {
      if (!this.validatePasswordForm()) {
        return
      }
      
      try {
        this.$refs.feedback.showLoadingMessage('修改密码中...')
        // 使用统一的API服务修改密码
        const data = await userApi.changePassword({
          username: this.userData.username,
          oldPassword: this.passwordData.oldPassword,
          newPassword: this.passwordData.newPassword
        })
        
        if (data.code === 'success') {
          this.$refs.feedback.showSuccessMessage(data.message || '密码已修改成功')
          // 清空表单
          this.passwordData = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        } else {
          this.$refs.feedback.showErrorMessage(data.message || '修改密码失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        this.$refs.feedback.showErrorMessage(`修改密码失败: ${error.message}`)
      } finally {
        this.$refs.feedback.hideLoading()
      }
    },
    handleBack() {
      // 返回服务商首页
      this.$router.push('/provider')
    },
    handleLogout() {
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('userData')
      localStorage.removeItem('passwordTemp')
      
      // 跳转到登录页
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  margin-top: 20px;
}

.profile-sidebar {
  width: 250px;
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 20px;
}

.profile-sidebar nav ul {
  list-style: none;
  padding: 0;
}

.profile-sidebar nav ul li {
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
}

.profile-sidebar nav ul li:hover {
  background-color: #e0e0e0;
}

.profile-sidebar nav ul li.active {
  background-color: #FF9800;
  color: white;
}

.profile-content {
  flex: 1;
  margin-left: 20px;
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.tab-content {
  margin-top: 20px;
}

.form-group {
  margin-bottom: 20px;
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
  height: 100px;
  resize: vertical;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.btn.primary {
  background-color: #FF9800;
  color: white;
}

.btn.primary:hover {
  background-color: #F57C00;
}

.verify-status {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.status-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.status-label {
  font-weight: bold;
  width: 100px;
}

.status-value {
  padding: 5px 10px;
  border-radius: 4px;
}

.status-value.verified {
  background-color: #d4edda;
  color: #155724;
}

.status-value.not-verified {
  background-color: #f8d7da;
  color: #721c24;
}

.status-value.not-filled {
  color: #888;
  font-style: italic;
}

.error-message {
  color: #dc3545;
  font-size: 14px;
  margin-top: 5px;
  display: block;
}
</style>