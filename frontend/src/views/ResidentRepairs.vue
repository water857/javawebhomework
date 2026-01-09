<template>
  <div class="container">
    <header>
      <h1>我的报修</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="repair-tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">报修历史</button>
      <button class="tab-btn" :class="{ active: activeTab === 'submit' }" @click="activeTab = 'submit'">提交报修</button>
    </div>
    
    <div class="tab-content">
      <!-- 报修历史记录 -->
      <div v-if="activeTab === 'history'" class="history-tab">
        <div class="search-filter">
          <input type="text" v-model="searchKeyword" placeholder="搜索报修内容" class="search-input">
          <select v-model="statusFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待受理</option>
            <option value="processing">处理中</option>
            <option value="completed">已完成</option>
          </select>
          <button class="btn" @click="fetchRepairs">查询</button>
        </div>
        
        <div class="repair-list">
          <div v-if="repairs.length === 0" class="empty-state">
            <p>暂无报修记录</p>
          </div>
          <div v-else class="repair-item" v-for="(repair, index) in repairs" :key="repair.id" @click="viewRepairDetail(repair.id, index + 1)">
            <div class="repair-header">
              <h3>报修单号：{{ index + 1 }}</h3>
              <span class="status" :class="repair.status">{{ getStatusText(repair.status) }}</span>
            </div>
            <div class="repair-content">
              <p><strong>报修内容：</strong>{{ repair.content }}</p>
              <p><strong>报修时间：</strong>{{ formatDate(repair.createdAt) }}</p>
              <p v-if="repair.serviceProvider"><strong>服务商：</strong>{{ repair.serviceProvider.realName }}</p>
              <p v-if="repair.updatedAt"><strong>更新时间：</strong>{{ formatDate(repair.updatedAt) }}</p>
            </div>
            
            <!-- 报修图片 -->
            <div v-if="repair.images && repair.images.length > 0" class="repair-images">
              <strong>报修图片：</strong>
              <div class="image-grid">
                <img v-for="(image, index) in repair.images" :key="index" :src="image.imageUrl" class="repair-image" @click="viewImage(image.imageUrl)">
              </div>
            </div>
            
            <!-- 撤回按钮 -->
            <div v-if="repair.status === 'pending' && repair.status !== 'completed'" class="repair-actions">
              <button class="btn" @click="withdrawRepair(repair.id)">撤回报修</button>
            </div>
            <!-- 评价按钮 -->
            <div v-if="repair.status === 'completed' && !repair.evaluation" class="repair-actions">
              <button class="btn btn-primary" @click="showEvaluateModal(repair.id)">评价报修</button>
            </div>
            
            <!-- 查看评价 -->
            <div v-if="repair.evaluation" class="repair-evaluation">
              <h4>我的评价</h4>
              <div class="rating">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= repair.evaluation.rating }">★</span>
              </div>
              <p>{{ repair.evaluation.comment }}</p>
              <p class="evaluation-time">{{ formatDate(repair.evaluation.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 提交报修请求 -->
      <div v-else-if="activeTab === 'submit'" class="submit-tab">
        <h2>提交报修请求</h2>
        <form @submit.prevent="submitRepair" class="repair-form">
          <div class="form-group">
            <label for="content">报修内容：</label>
            <textarea id="content" v-model="newRepair.content" rows="5" placeholder="请详细描述报修问题" required></textarea>
          </div>
          
          <div class="form-group">
            <label>上传图片（最多3张）：</label>
            <div class="image-upload-area">
              <div v-for="(image, index) in selectedImages" :key="index" class="uploaded-image">
                <img :src="image.src" alt="上传的图片">
                <button type="button" class="remove-image" @click="removeImage(index)">×</button>
              </div>
              <label v-if="selectedImages.length < 3" class="upload-btn">
                <input type="file" accept="image/gif,image/jpeg,image/png,image/jpg" @change="handleImageUpload" multiple>
                <span>+ 上传图片</span>
              </label>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="submitting">提交报修</button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- 评价模态框 -->
    <div v-if="showEvaluate" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>评价报修</h3>
          <button class="close-btn" @click="showEvaluate = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitEvaluation">
            <div class="form-group">
              <label>评分：</label>
              <div class="rating-input">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= evaluateForm.rating }" @click="evaluateForm.rating = n">★</span>
              </div>
            </div>
            
            <div class="form-group">
              <label for="evaluation-content">评价内容：</label>
              <textarea id="evaluation-content" v-model="evaluateForm.content" rows="4" placeholder="请输入您的评价" required></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" class="btn" @click="showEvaluate = false">取消</button>
              <button type="submit" class="btn btn-primary">提交评价</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 图片预览模态框 -->
    <div v-if="previewImage" class="modal">
      <div class="modal-content image-preview">
        <button class="close-btn" @click="previewImage = ''">×</button>
        <img :src="previewImage" alt="图片预览">
      </div>
    </div>
    
    <!-- 报修详情模态框 -->
    <div v-if="showDetail && currentRepair" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>报修详情</h3>
          <button class="close-btn" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="repair-detail">
            <div class="detail-section">
              <h4>基本信息</h4>
              <p><strong>报修单号：</strong>{{ currentRepair.sequentialId || currentRepair.id }}</p>
              <p><strong>状态：</strong><span class="status" :class="currentRepair.status">{{ getStatusText(currentRepair.status) }}</span></p>
              <p><strong>报修时间：</strong>{{ formatDate(currentRepair.createdAt) }}</p>
              <p v-if="currentRepair.updatedAt"><strong>更新时间：</strong>{{ formatDate(currentRepair.updatedAt) }}</p>
            </div>
            
            <div class="detail-section">
              <h4>居民信息</h4>
              <p><strong>用户名：</strong>{{ username }}</p>
              <p><strong>姓名：</strong>{{ userData.realName || username }}</p>
              <p v-if="userData.phone"><strong>手机号：</strong>{{ userData.phone }}</p>
              <p v-if="userData.address"><strong>居住地址：</strong>{{ userData.address }}</p>
              <p v-if="userData.email"><strong>邮箱：</strong>{{ userData.email }}</p>
            </div>
            
            <div class="detail-section">
              <h4>报修内容</h4>
              <p>{{ currentRepair.content }}</p>
            </div>
            
            <!-- 报修图片 -->
            <div class="detail-section">
              <h4>报修图片</h4>
              <div class="image-grid">
                <img v-if="currentRepair.images && currentRepair.images.length > 0" v-for="(image, index) in currentRepair.images" :key="index" :src="image.imageUrl" class="repair-image" @click="viewImage(image.imageUrl)">
                <p v-else class="no-image">暂无报修图片</p>
              </div>
            </div>
            
            <!-- 处理信息 -->
            <div v-if="currentRepair.serviceProvider" class="detail-section">
              <h4>处理信息</h4>
              <p><strong>服务商：</strong>{{ currentRepair.serviceProvider.realName }}</p>
              <p v-if="currentRepair.repairTime"><strong>处理时间：</strong>{{ formatDate(currentRepair.repairTime) }}</p>
              <p v-if="currentRepair.completedTime"><strong>完成时间：</strong>{{ formatDate(currentRepair.completedTime) }}</p>
            </div>
            
            <!-- 评价信息 -->
            <div v-if="currentRepair.evaluation" class="detail-section">
              <h4>我的评价</h4>
              <div class="rating">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= currentRepair.evaluation.rating }">★</span>
              </div>
              <p>{{ currentRepair.evaluation.comment }}</p>
              <p class="evaluation-time">{{ formatDate(currentRepair.evaluation.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResidentRepairs',
  data() {
    return {
      username: '',
      userData: {},
      activeTab: 'history',
      repairs: [],
      searchKeyword: '',
      statusFilter: '',
      newRepair: {
        content: ''
      },
      selectedImages: [],
      submitting: false,
      showEvaluate: false,
      evaluateRepairId: null,
      evaluateForm: {
        rating: 5,
        content: ''
      },
      previewImage: '',
      showDetail: false,
      currentRepair: null,
      refreshInterval: null // 定时刷新定时器
    }
  },
  mounted() {
    this.username = this.getLocalStorageItem('username')
    this.applyTabFromRoute()
    this.fetchUserData()
    this.fetchRepairs()
    
    // 启动定时刷新，每30秒刷新一次报修列表
    this.refreshInterval = setInterval(() => {
      this.fetchRepairs()
    }, 30000)
  },
  methods: {
    applyTabFromRoute() {
      if (this.$route.query.tab === 'submit') {
        this.activeTab = 'submit'
      }
    },
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
    fetchUserData() {
      // 从本地存储获取用户信息
      const userDataString = this.getLocalStorageItem('userData')
      if (userDataString) {
        this.userData = JSON.parse(userDataString)
      } else {
        // 如果本地没有，从后端API获取
        const token = this.getLocalStorageItem('token')
        fetch('/api/user', {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        .then(response => response.json())
        .then(data => {
          // 检查响应是否成功，支持code或status字段
          if (data.code === 'success' || data.status === 'success') {
            this.userData = data.data
            this.setLocalStorageItem('userData', JSON.stringify(data.data))
          }
        })
        .catch(error => {
          console.error('获取用户信息失败：', error)
        })
      }
    },
    
    fetchRepairs() {
      // 调用后端API获取报修列表
      const token = this.getLocalStorageItem('token')
      // 构建查询参数
      const params = new URLSearchParams()
      if (this.searchKeyword) params.append('searchKeyword', this.searchKeyword)
      if (this.statusFilter) params.append('status', this.statusFilter)
      const queryString = params.toString() ? `?${params.toString()}` : ''
      
      fetch(`/api/repair/list${queryString}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => {
        if (!response.ok) {
          // 如果响应状态码不是2xx，直接抛出错误
          throw new Error('网络请求失败，状态码：' + response.status)
        }
        return response.json()
      })
      .then(data => {
        // 检查响应是否成功，支持code或status字段
        if (data.code === 'success' || data.status === 'success') {
          // 过滤掉已撤回的报修
          this.repairs = data.data.filter(repair => repair.status !== 'withdrawn')
          
          // 如果当前有打开的报修详情，刷新详情内容
          if (this.showDetail && this.currentRepair) {
            const sequentialId = this.currentRepair.sequentialId
            this.viewRepairDetail(this.currentRepair.id, sequentialId)
          }
        } else {
          alert('获取报修列表失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('获取报修列表失败：', error)
        alert('获取报修列表失败，请稍后重试')
      })
    },
    
    async submitRepair() {
      if (!this.newRepair.content.trim()) {
        alert('请输入报修内容')
        return
      }
      
      this.submitting = true
      const token = this.getLocalStorageItem('token')
      
      // 上传图片并获取URL
      const imageUrls = await this.uploadImages()
      
      // 准备提交的数据（JSON格式）
      const submitData = {
        title: '报修请求', // 提供默认标题
        content: this.newRepair.content,
        images: imageUrls // 使用上传后获取的图片URL数组
      }
      
      // 调用后端API提交报修
      fetch('/api/repair/submit', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(submitData)
      })
      .then(response => response.json())
      .then(data => {
        this.submitting = false
        // 检查响应是否成功，支持code或status字段
        if (data.code === 'success' || data.status === 'success') {
          alert('报修提交成功')
          this.newRepair.content = ''
          this.selectedImages = []
          this.activeTab = 'history'
          this.fetchRepairs()
        } else {
          alert('报修提交失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        this.submitting = false
        console.error('报修提交失败：', error)
        alert('报修提交失败，请稍后重试')
      })
    },
    
    // 上传图片到服务器并获取URL
    async uploadImages() {
      if (this.selectedImages.length === 0) {
        return []
      }
      
      const token = this.getLocalStorageItem('token')
      const imageUrls = []
      
      for (const image of this.selectedImages) {
        const formData = new FormData()
        formData.append('image', image.file)
        
        try {
          const response = await fetch('/api/repair/upload', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`
            },
            body: formData
          })
          
          const data = await response.json()
          // 检查响应是否成功，支持code或status字段
          if (data.code === 'success' || data.status === 'success') {
            imageUrls.push(data.data)
          } else {
            console.error('图片上传失败：', data.message)
          }
        } catch (error) {
          console.error('图片上传失败：', error)
        }
      }
      
      return imageUrls
    },
    
    handleImageUpload(event) {
      const files = event.target.files
      if (files && files.length > 0) {
        const remainingSlots = 3 - this.selectedImages.length
        const filesToAdd = Math.min(files.length, remainingSlots)
        const vm = this // 保存Vue实例的引用
        
        for (let i = 0; i < filesToAdd; i++) {
          const file = files[i]
          const reader = new FileReader()
          
          reader.onload = function(e) {
            vm.selectedImages.push({
              src: e.target.result,
              file: file
            })
          }
          
          reader.readAsDataURL(file)
        }
      }
    },
    
    removeImage(index) {
      this.selectedImages.splice(index, 1)
    },
    
    showEvaluateModal(repairId) {
      this.evaluateRepairId = repairId
      this.evaluateForm = {
        rating: 5,
        content: ''
      }
      this.showEvaluate = true
    },
    
    submitEvaluation() {
      if (!this.evaluateForm.content.trim()) {
        alert('请输入评价内容')
        return
      }
      
      const token = this.getLocalStorageItem('token')
      
      // 调用后端API提交评价
      fetch(`/api/repair/evaluate/${this.evaluateRepairId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.evaluateForm)
      })
      .then(response => response.json())
      .then(data => {
        // 检查响应是否成功，支持code或status字段
        if (data.code === 'success' || data.status === 'success') {
          alert('评价提交成功')
          this.showEvaluate = false
          this.fetchRepairs()
        } else {
          alert('评价提交失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('评价提交失败：', error)
        alert('评价提交失败，请稍后重试')
      })
    },
    
    viewImage(imageUrl) {
      this.previewImage = imageUrl
    },
    
    viewRepairDetail(repairId, sequentialId) {
      const token = this.getLocalStorageItem('token')
      
      fetch(`/api/repair/detail/${repairId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        // 检查响应是否成功，支持code或status字段
        if (data.code === 'success' || data.status === 'success') {
          this.currentRepair = data.data
          this.currentRepair.sequentialId = sequentialId
          this.showDetail = true
        } else {
          alert('获取报修详情失败：' + data.message)
        }
      })
      .catch(error => {
        console.error('获取报修详情失败：', error)
        alert('获取报修详情失败，请稍后重试')
      })
    },
    
    getStatusText(status) {
      const statusMap = {
        'pending': '待受理',
        'processing': '处理中',
        'completed': '已完成',
        'withdrawn': '已撤回'
      }
      return statusMap[status] || status
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      // 检查日期是否有效
      if (isNaN(date.getTime())) return ''
      return date.toLocaleString()
    },
    
    handleBack() {
      this.$router.push('/resident')
    },
    
    handleProfile() {
      this.$router.push('/resident/profile')
    },
    
    handleLogout() {
      // 清除定时器
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
      
      // 安全地清除localStorage
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
      this.$router.push('/login')
    },
    
    // 组件销毁时清除定时器
    beforeUnmount() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    },
    
    withdrawRepair(repairId) {
      if (confirm('确定要撤回报修吗？')) {
        const token = this.getLocalStorageItem('token')
        
        fetch(`/api/repair/${repairId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        .then(response => response.json())
        .then(data => {
          // 检查响应是否成功，支持code或status字段
          if (data.code === 'success' || data.status === 'success') {
            alert('报修已成功撤回')
            this.fetchRepairs()
          } else {
          alert('撤回报修失败：' + (data.message || '未知错误'))
        }
        })
        .catch(error => {
          console.error('撤回报修失败：', error)
          alert('撤回报修失败，请稍后重试')
        })
      }
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn:hover {
  background-color: #0056b3;
}

.btn-primary {
  background-color: #28a745;
}

.btn-primary:hover {
  background-color: #218838;
}

.repair-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 16px;
  cursor: pointer;
  margin-right: 20px;
}

.tab-btn.active {
  border-bottom-color: #007bff;
  color: #007bff;
  font-weight: bold;
}

.tab-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

/* 历史记录样式 */
.search-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.repair-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.repair-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.repair-header h3 {
  margin: 0;
  color: #333;
}

.status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status.pending {
  background-color: #ffc107;
  color: #856404;
}

.status.processing {
  background-color: #17a2b8;
  color: white;
}

.status.completed {
  background-color: #28a745;
  color: white;
}

.repair-content {
  margin-bottom: 15px;
}

.repair-content p {
  margin: 8px 0;
  color: #555;
}

.repair-images {
  margin-bottom: 15px;
}

.image-grid {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.repair-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;
}

.repair-actions {
  text-align: right;
  margin-top: 15px;
}

.repair-evaluation {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.repair-evaluation h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.rating {
  color: #ddd;
  font-size: 20px;
  margin-bottom: 10px;
}

.star {
  cursor: pointer;
}

.star.filled {
  color: #ffc107;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

/* 提交报修样式 */
.submit-tab h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.repair-form {
  max-width: 600px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  font-size: 14px;
}

.image-upload-area {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.uploaded-image {
  position: relative;
  width: 120px;
  height: 120px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.uploaded-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 20px;
  height: 20px;
  background-color: rgba(255, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #999;
  background-color: #f9f9f9;
}

.upload-btn input[type="file"] {
  display: none;
}

.upload-btn span {
  font-size: 14px;
  margin-top: 8px;
}

.form-actions {
  text-align: right;
  margin-top: 30px;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.close-btn:hover {
  color: #333;
}

.image-preview {
  max-width: 800px;
  max-height: 90vh;
}

.image-preview img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
