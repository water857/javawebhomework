<template>
  <div class="container">
    <header>
      <h1>报修管理</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="repair-management">
      <div class="search-filter">
        <div class="resident-search">
          <input type="text" v-model="residentSearchKeyword" placeholder="搜索居民姓名或电话" class="search-input">
          <div v-if="residents.length > 0 && residentSearchKeyword" class="resident-dropdown">
            <div v-for="resident in filteredResidents" :key="resident.id" class="resident-item" @click="selectResident(resident)">
              <span>{{ resident.realName }}</span>
              <span class="resident-phone">{{ resident.phone }}</span>
            </div>
          </div>
        </div>
        <select v-model="selectedResidentId" class="filter-select">
          <option value="">全部居民</option>
          <option v-for="resident in residents" :key="resident.id" :value="resident.id">{{ resident.realName }} ({{ resident.phone }})</option>
        </select>
        <input type="text" v-model="searchKeyword" placeholder="搜索报修内容或单号" class="search-input">
        <select v-model="statusFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="pending">待受理</option>
          <option value="processing">处理中</option>
          <option value="completed">已完成</option>
        </select>
        <button class="btn" @click="fetchRepairs">查询</button>
        <button class="btn btn-primary" @click="refreshList">刷新列表</button>
      </div>
      
      <div class="repair-list">
        <div v-if="repairs.length === 0" class="empty-state">
          <p>暂无报修记录</p>
        </div>
        <div v-else class="repair-item" v-for="repair in repairs" :key="repair.id">
          <div class="repair-header">
            <h3>报修单号：BR{{ String(repair.id).padStart(6, '0') }}</h3>
            <span class="status" :class="repair.status">{{ getStatusText(repair.status) }}</span>
          </div>
          
          <div class="repair-info">
            <div class="info-section">
              <p><strong>居民：</strong>{{ repair.resident?.realName || repair.residentId }}</p>
              <p><strong>报修时间：</strong>{{ formatDate(repair.createdAt) }}</p>
              <p><strong>联系电话：</strong>{{ repair.resident?.phone || '-' }}</p>
              <p><strong>地址：</strong>{{ repair.resident?.address || '未获取到地址信息' }}</p>
            </div>
            
            <div class="info-section">
              <p><strong>服务商：</strong>{{ repair.serviceProvider?.realName || '未分配' }}</p>
              <p v-if="repair.updatedAt"><strong>更新时间：</strong>{{ formatDate(repair.updatedAt) }}</p>
            </div>
          </div>
          
          <div class="repair-content">
      <h4 v-if="repair.title"><strong>报修标题：</strong>{{ repair.title }}</h4>
      <p><strong>报修内容：</strong>{{ repair.content }}</p>
    </div>
          
          <!-- 报修图片 -->
          <div v-if="getRepairImages(repair).length > 0" class="repair-images">
            <strong>报修图片：</strong>
            <div class="image-grid">
              <img v-for="(imageUrl, index) in getRepairImages(repair)" :key="index" :src="imageUrl" class="repair-image" @click="viewImage(imageUrl)">
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="repair-actions">
            <!-- 查看详情 -->
            <button class="btn btn-primary" @click="viewRepairDetail(repair)">查看详情</button>
            
            <!-- 分配服务商 -->
            <div v-if="repair.status === 'pending' && repair.status !== 'completed'" class="action-group">
              <select v-model="repair.providerId" class="select-provider">
                <option value="">选择服务商</option>
                <option v-for="provider in providers" :key="provider.id" :value="provider.id">{{ provider.username }}</option>
              </select>
              <button class="btn btn-primary" @click="assignRepair(repair.id, repair.providerId)">分配任务</button>
            </div>
            
            <!-- 更新状态 -->
            <div v-if="repair.status !== 'completed'" class="action-group">
              <select v-model="repair.status" class="select-status">
                <option value="pending">待受理</option>
                <option value="processing">处理中</option>
                <option value="completed" :disabled="role !== 'service_provider'">已完成</option>
              </select>
              <button class="btn btn-primary" @click="updateRepairStatus(repair.id, repair.status)">更新状态</button>
            </div>
            
            <!-- 查看评价 -->
            <button v-if="repair.status === 'completed' && repair.evaluation" class="btn" @click="viewEvaluation(repair.id)">查看评价</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评价详情模态框 -->
    <div v-if="showEvaluation" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>报修评价详情</h3>
          <button class="close-btn" @click="showEvaluation = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="currentEvaluation" class="evaluation-detail">
            <div class="rating">
              <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= currentEvaluation.rating }">★</span>
            </div>
            <p class="evaluation-content">{{ currentEvaluation.comment }}</p>
            <p class="evaluation-time">评价时间：{{ formatDate(currentEvaluation.createdAt) }}</p>
          </div>
          <div v-else class="no-evaluation-modal">
            暂无评价
          </div>
        </div>
      </div>
    </div>
    
    <!-- 报修详情模态框 -->
    <div v-if="showRepairDetail" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>报修详情</h3>
          <button class="close-btn" @click="showRepairDetail = false">×</button>
        </div>
        <div v-if="currentRepair" class="modal-body">
            <div class="repair-detail">
              <div class="detail-section">
                <h4>居民信息</h4>
                <p><strong>居民ID：</strong>{{ currentRepair.residentId }}</p>
                <p><strong>姓名：</strong>{{ currentRepair.resident?.realName || '未获取到姓名信息' }}</p>
                <p><strong>电话号码：</strong>{{ currentRepair.resident?.phone || '未获取到电话信息' }}</p>
                <p><strong>邮箱：</strong>{{ currentRepair.resident?.email || '未获取到邮箱信息' }}</p>
                <p><strong>地址：</strong>{{ currentRepair.resident?.address || '未获取到地址信息' }}</p>
              </div>
              
              <div class="detail-section">
                <h4>报修信息</h4>
                <p><strong>报修单号：</strong>BR{{ String(currentRepair.id).padStart(6, '0') }}</p>
                <p><strong>报修时间：</strong>{{ formatDate(currentRepair.createdAt) }}</p>
                <p><strong>报修标题：</strong>{{ currentRepair.title || '无标题' }}</p>
                <p><strong>报修内容：</strong>{{ currentRepair.content }}</p>
                <p v-if="currentRepair.resident"><strong>报修地址：</strong>{{ currentRepair.resident.address }}</p>
                <p v-if="currentRepair.resident"><strong>联系电话：</strong>{{ currentRepair.resident.phone }}</p>
                
                <!-- 报修图片 -->
                <div class="repair-images">
                  <strong>报修图片：</strong>
                  <div v-if="getRepairImages(currentRepair).length > 0" class="image-grid">
                    <img v-for="(imageUrl, index) in getRepairImages(currentRepair)" :key="index" :src="imageUrl" class="repair-image" @click="viewImage(imageUrl)">
                  </div>
                  <div v-else class="no-images">
                    暂无报修图片
                  </div>
                </div>
              </div>
              
              <div class="detail-section">
                <h4>处理信息</h4>
                <p><strong>服务商：</strong>{{ currentRepair.serviceProvider ? currentRepair.serviceProvider.realName : '未分配' }}</p>
                <p><strong>当前状态：</strong><span class="status" :class="currentRepair.status">{{ getStatusText(currentRepair.status) }}</span></p>
                <p v-if="currentRepair.updatedAt"><strong>更新时间：</strong>{{ formatDate(currentRepair.updatedAt) }}</p>
              </div>
              
              <!-- 评价信息 -->
              <div class="detail-section">
                <h4>评价信息</h4>
                <div v-if="currentRepair.evaluation" class="evaluation-info">
                  <div class="rating">
                    <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= currentRepair.evaluation.rating }">★</span>
                  </div>
                  <p class="evaluation-content">{{ currentRepair.evaluation.comment }}</p>
                  <p class="evaluation-time">评价时间：{{ formatDate(currentRepair.evaluation.createdAt) }}</p>
                </div>
                <div v-else class="no-evaluation">
                  暂无评价
                </div>
              </div>
            </div>
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
  </div>
</template>

<script>
export default {
  name: 'AdminRepairManagement',
  data() {
    return {
      username: '',
      role: '',
      repairs: [],
      providers: [],
      residents: [],
      selectedResidentId: '',
      searchKeyword: '',
      statusFilter: '',
      residentSearchKeyword: '',
      showEvaluation: false,
      currentEvaluation: null,
      previewImage: '',
      showRepairDetail: false,
      currentRepair: null,
      refreshInterval: null // 定时刷新定时器
    }
  },
  mounted() {
    this.username = this.getLocalStorageItem('username')
    this.role = this.getLocalStorageItem('role')
    this.fetchRepairs()
    this.fetchProviders()
    this.fetchResidents()
    
    // 启动定时刷新，每30秒刷新一次报修列表
    this.refreshInterval = setInterval(() => {
      this.fetchRepairs()
    }, 30000)
  },
  computed: {
    filteredResidents() {
      if (!this.residentSearchKeyword) return []
      return this.residents.filter(resident => {
        return resident.realName.toLowerCase().includes(this.residentSearchKeyword.toLowerCase()) ||
               resident.phone.includes(this.residentSearchKeyword)
      })
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
    // 安全设置localStorage项的辅助函数
    setLocalStorageItem(key, value) {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(key, value)
      }
    },
    selectResident(resident) {
      this.selectedResidentId = resident.id
      this.residentSearchKeyword = ''
      this.fetchRepairs()
    },
    fetchRepairs() {
      // 调用后端API获取所有报修列表，并传递搜索和筛选参数
      const token = localStorage.getItem('token')
      
      // 构建查询参数
      const params = new URLSearchParams()
      if (this.searchKeyword) {
        params.append('searchKeyword', this.searchKeyword)
      }
      if (this.statusFilter) {
        params.append('status', this.statusFilter)
      }
      if (this.selectedResidentId) {
        params.append('residentId', this.selectedResidentId)
      }
      
      fetch(`/api/repair/list?${params.toString()}`, {
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
        if (data.status === 'success') {
          this.repairs = data.data
          
          // 如果当前有打开的报修详情，刷新详情内容
          if (this.showRepairDetail && this.currentRepair) {
            const updatedRepair = this.repairs.find(r => r.id === this.currentRepair.id)
            if (updatedRepair) {
              this.viewRepairDetail(updatedRepair)
            }
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
    
    fetchProviders() {
      // 调用后端API获取服务商列表
      const token = localStorage.getItem('token')
      fetch('/api/providers', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          this.providers = data.data
        } else {
          alert('获取服务商列表失败：' + data.message)
        }
      })
      .catch(error => {
        console.error('获取服务商列表失败：', error)
        alert('获取服务商列表失败，请稍后重试')
      })
    },
    
    fetchResidents() {
      // 调用后端API获取居民列表
      const token = localStorage.getItem('token')
      fetch('/api/residents', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          this.residents = data.data
        } else {
          alert('获取居民列表失败：' + data.message)
        }
      })
      .catch(error => {
        console.error('获取居民列表失败：', error)
        alert('获取居民列表失败，请稍后重试')
      })
    },
    
    assignRepair(repairId, providerId) {
      if (!providerId) {
        alert('请选择服务商')
        return
      }
      
      const token = localStorage.getItem('token')
      fetch(`/api/repair/assign/${repairId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ providerId: providerId })
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          alert('任务分配成功')
          this.fetchRepairs()
        } else {
          alert('任务分配失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('任务分配失败：', error)
        alert('任务分配失败，请稍后重试')
      })
    },
    
    updateRepairStatus(repairId, status) {
      // 如果用户是业务管理员且尝试将状态更新为"已完成"，直接提示没有权限
      if (this.role === 'property_admin' && status === 'completed') {
        alert('您没有权限将报修单状态更新为"已完成"')
        return
      }
      
      const token = this.getLocalStorageItem('token')
      fetch(`/api/repair/status/${repairId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status })
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          alert('状态更新成功')
          this.fetchRepairs()
        } else {
          alert('状态更新失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('状态更新失败：', error)
        alert('状态更新失败，请稍后重试')
      })
    },
    
    viewEvaluation(repairId) {
      const token = localStorage.getItem('token')
      fetch(`/api/repair/detail/${repairId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          this.currentEvaluation = data.data.evaluation
          this.showEvaluation = true
        } else {
          alert('获取报修详情失败：' + data.message)
        }
      })
      .catch(error => {
        console.error('获取报修详情失败：', error)
        alert('获取报修详情失败，请稍后重试')
      })
    },
    
    viewImage(imageUrl) {
      this.previewImage = imageUrl
    },
    
    viewRepairDetail(repair) {
      // 先显示基础信息
      const resident = this.residents.find(r => r.id === repair.residentId)
      let repairWithResidentInfo = repair
      if (resident) {
        repairWithResidentInfo = {
          ...repair,
          residentName: resident.realName,
          residentPhone: resident.phone,
          residentEmail: resident.email,
          residentAddress: resident.address
        }
      }
      this.currentRepair = repairWithResidentInfo
      this.showRepairDetail = true
      
      // 从API获取完整的报修详情，包括图片
      const token = localStorage.getItem('token')
      fetch(`/api/repair/detail/${repair.id}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          // 合并API返回的完整信息到当前报修记录
          const fullRepairInfo = data.data
          this.currentRepair = {
            ...fullRepairInfo,
            // 保留之前的居民信息作为备选
            residentName: fullRepairInfo.resident?.realName || fullRepairInfo.residentName || this.currentRepair.residentName,
            residentPhone: fullRepairInfo.resident?.phone || fullRepairInfo.residentPhone || this.currentRepair.residentPhone,
            residentEmail: fullRepairInfo.resident?.email || fullRepairInfo.residentEmail || this.currentRepair.residentEmail,
            residentAddress: fullRepairInfo.resident?.address || fullRepairInfo.residentAddress || this.currentRepair.residentAddress
          }
        } else {
          console.error('获取完整报修详情失败：', data.message)
        }
      })
      .catch(error => {
        console.error('获取完整报修详情失败：', error)
      })
    },
    
    getRepairImages(repair) {
      // 处理不同的图片数据结构
      if (!repair) return []
      
      // 检查是否有images字段
      if (!repair.images) return []
      
      // 处理可能的图片字段命名变化
      const imagesData = repair.images || repair.pictures || repair.attachments
      
      // 如果imagesData是数组
      if (Array.isArray(imagesData)) {
        return imagesData.map(image => {
          // 如果是对象，返回imageUrl、url、path或src属性
          if (typeof image === 'object' && image !== null) {
            return image.imageUrl || image.url || image.path || image.src || ''
          }
          // 如果是字符串，直接返回
          return image
        }).filter(imageUrl => typeof imageUrl === 'string' && imageUrl.trim())
      }
      
      // 如果imagesData是字符串，转换为数组
      if (typeof imagesData === 'string') {
        // 处理可能的JSON字符串
        try {
          const parsedImages = JSON.parse(imagesData)
          if (Array.isArray(parsedImages)) {
            return this.getRepairImages({ images: parsedImages })
          }
        } catch (e) {
          // 如果不是JSON字符串，直接返回
          return [imagesData]
        }
      }
      
      // 处理单个图片对象
      if (typeof imagesData === 'object' && imagesData !== null) {
        const imageUrl = imagesData.imageUrl || imagesData.url || imagesData.path || imagesData.src
        return imageUrl ? [imageUrl] : []
      }
      
      return []
    },
    
    refreshList() {
      this.fetchRepairs()
    },
    
    // 组件销毁时清除定时器
    beforeUnmount() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        'pending': '待受理',
        'processing': '处理中',
        'completed': '已完成'
      }
      return statusMap[status] || status
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    
    handleBack() {
      this.$router.push('/admin')
    },
    
    handleProfile() {
      this.$router.push('/admin/profile')
    },
    
    handleLogout() {
      // 清除定时器
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
      
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
  }
}
</script>

<style scoped>
.container {
  max-width: 1400px;
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

.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  align-items: center;
  flex-wrap: wrap;
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
  min-width: 150px;
}

.repair-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.repair-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 25px;
  background-color: #fafafa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.repair-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
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

.repair-info {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.info-section {
  flex: 1;
  min-width: 250px;
}

.info-section p {
  margin: 10px 0;
  color: #555;
}

.repair-content {
  margin-bottom: 20px;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  border-left: 4px solid #007bff;
}

.repair-content p {
  margin: 0;
  color: #333;
  line-height: 1.6;
}

.repair-images {
  margin-bottom: 20px;
}

.image-grid {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.repair-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;
  transition: transform 0.2s;
}

.repair-image:hover {
  transform: scale(1.05);
}

.no-images {
  margin-top: 10px;
  color: #999;
  font-style: italic;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

/* 评价相关样式 */
.evaluation-info {
  margin-top: 10px;
}

.no-evaluation, .no-evaluation-modal {
  margin-top: 10px;
  color: #999;
  font-style: italic;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.no-evaluation-modal {
  text-align: center;
  font-size: 16px;
  padding: 40px;
}

.rating {
  margin: 10px 0;
  font-size: 24px;
}

.star {
  color: #ccc;
  margin-right: 5px;
  cursor: default;
}

.star.filled {
  color: #ffc107;
}

.evaluation-content {
  margin: 15px 0;
  line-height: 1.6;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 4px solid #28a745;
}

.evaluation-time {
  color: #666;
  font-size: 14px;
}

.repair-actions {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.action-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.select-provider, .select-status {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 180px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  background-color: white;
  border-radius: 8px;
  border: 1px dashed #ddd;
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
  padding: 25px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
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

.evaluation-detail {
  padding: 20px;
}

.rating {
  color: #ffc107;
  font-size: 24px;
  margin-bottom: 15px;
}

.star {
  cursor: pointer;
}

.star.filled {
  color: #ffc107;
}

.evaluation-content {
  margin: 15px 0;
  line-height: 1.6;
  color: #555;
  font-size: 16px;
}

.evaluation-time {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
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

/* 报修详情样式 */
.repair-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.detail-section h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  border-bottom: 2px solid #007bff;
  padding-bottom: 8px;
}

.detail-section p {
  margin: 10px 0;
  color: #555;
}
</style>