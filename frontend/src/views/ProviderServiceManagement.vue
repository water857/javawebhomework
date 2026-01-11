<template>
  <div class="container">
    <header>
      <h1>服务商服务管理</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <div class="service-tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'tasks' }" @click="activeTab = 'tasks'">任务管理</button>
      <button class="tab-btn" :class="{ active: activeTab === 'records' }" @click="activeTab = 'records'">服务记录</button>
      <button class="tab-btn" :class="{ active: activeTab === 'evaluations' }" @click="activeTab = 'evaluations'">服务评价</button>
    </div>
    
    <div class="tab-content">
      <!-- 任务管理标签页 -->
      <div v-if="activeTab === 'tasks'" class="tasks-tab">
        <div class="search-filter">
          <input type="text" v-model="tasksSearchKeyword" placeholder="搜索报修内容或单号" class="search-input">
          <select v-model="tasksStatusFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待受理</option>
            <option value="processing">处理中</option>
            <option value="completed">已完成</option>
          </select>
          <button class="btn" @click="fetchTasks">查询</button>
          <button class="btn btn-primary" @click="refreshTasks">刷新任务</button>
        </div>
        
        <div class="repair-list">
          <div v-if="tasks.length === 0" class="empty-state">
            <p>暂无分配的任务</p>
          </div>
          <div v-else class="repair-item" v-for="task in tasks" :key="task.id">
            <div class="repair-header">
              <h3>任务单号：{{ task.id }}</h3>
              <span class="status" :class="task.status">{{ getStatusText(task.status) }}</span>
            </div>
            
            <div class="repair-info">
              <div class="info-section">
                <p><strong>居民：</strong>{{ task.resident?.realName || task.residentName || task.residentId }}</p>
                <p><strong>报修时间：</strong>{{ formatDate(task.createdAt) }}</p>
                <p><strong>联系电话：</strong>{{ task.resident?.phone || task.phone }}</p>
              </div>
            </div>
            
            <div class="repair-content">
              <p><strong>报修内容：</strong>{{ task.content }}</p>
            </div>
            
            <!-- 报修图片 -->
            <div v-if="task.images && task.images.length > 0" class="repair-images">
              <strong>报修图片：</strong>
              <div class="image-grid">
                <img v-for="(image, index) in task.images" :key="index" :src="image.imageUrl" class="repair-image" @click="viewImage(image.imageUrl)">
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="repair-actions">
              <div class="action-group">
                <select v-model="task.newStatus" class="select-status" :disabled="task.status === 'completed'">
                  <option value="pending">待受理</option>
                  <option value="processing">处理中</option>
                  <option value="completed">已完成</option>
                </select>
                <button class="btn btn-primary" @click="updateTaskStatus(task.id, task.newStatus)" :disabled="task.status === 'completed'">更新进度</button>
              </div>
              <button class="btn btn-info" @click="viewRepairDetail(task.id)">查看详情</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 服务记录标签页 -->
      <div v-else-if="activeTab === 'records'" class="records-tab">
        <div class="search-filter">
          <input type="text" v-model="recordsSearchKeyword" placeholder="搜索服务记录或单号" class="search-input">
          <select v-model="recordsStatusFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待受理</option>
            <option value="processing">处理中</option>
            <option value="completed">已完成</option>
          </select>
          <button class="btn" @click="fetchServiceRecords">查询</button>
          <button class="btn btn-primary" @click="refreshRecords">刷新记录</button>
        </div>
        
        <div class="repair-list">
          <div v-if="serviceRecords.length === 0" class="empty-state">
            <p>暂无服务记录</p>
          </div>
          <div v-else class="repair-item" v-for="record in serviceRecords" :key="record.id">
            <div class="repair-header">
              <h3>服务单号：{{ record.id }}</h3>
              <span class="status" :class="record.status">{{ getStatusText(record.status) }}</span>
            </div>
            
            <div class="repair-info">
              <div class="info-section">
                <p><strong>居民：</strong>{{ record.resident?.realName || record.residentName || record.residentId }}</p>
                <p><strong>报修时间：</strong>{{ formatDate(record.createdAt) }}</p>
                <p><strong>完成时间：</strong>{{ record.status === 'completed' ? formatDate(record.completedTime) : '未完成' }}</p>
              </div>
            </div>
            
            <div class="repair-content">
              <p><strong>报修内容：</strong>{{ record.content }}</p>
            </div>
            
            <!-- 报修图片 -->
            <div v-if="record.images && record.images.length > 0" class="repair-images">
              <strong>报修图片：</strong>
              <div class="image-grid">
                <img v-for="(image, index) in record.images" :key="index" :src="image.imageUrl" class="repair-image" @click="viewImage(image.imageUrl)">
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 服务评价标签页 -->
      <div v-else-if="activeTab === 'evaluations'" class="evaluations-tab">
        <div class="search-filter">
          <input type="text" v-model="evaluationsSearchKeyword" placeholder="搜索评价内容" class="search-input">
          <select v-model="evaluationsRatingFilter" class="filter-select">
            <option value="">全部评分</option>
            <option value="1">1星</option>
            <option value="2">2星</option>
            <option value="3">3星</option>
            <option value="4">4星</option>
            <option value="5">5星</option>
          </select>
          <select v-model="evaluationsTimeFilter" class="filter-select">
            <option value="">全部时间</option>
            <option value="7">最近7天</option>
            <option value="30">最近30天</option>
            <option value="90">最近90天</option>
            <option value="180">最近6个月</option>
          </select>
          <button class="btn" @click="fetchEvaluations">查询</button>
          <button class="btn btn-primary" @click="refreshEvaluations">刷新评价</button>
        </div>
        
        <div class="evaluation-stats">
          <div class="stat-card">
            <h4>平均评分</h4>
            <div class="average-rating">
              <span>{{ averageRating.toFixed(1) }}</span>
              <div class="rating-stars">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= Math.round(averageRating) }">★</span>
              </div>
            </div>
          </div>
          <div class="stat-card">
            <h4>评价总数</h4>
            <span class="stat-number">{{ totalEvaluations }}</span>
          </div>
          <div class="stat-card">
            <h4>5星好评</h4>
            <span class="stat-number">{{ fiveStarCount }}</span>
            <span class="stat-percentage">{{ totalEvaluations > 0 ? Math.round((fiveStarCount / totalEvaluations) * 100) : 0 }}%</span>
          </div>
          <div class="stat-card">
            <h4>好评率</h4>
            <span class="stat-number">{{ (ratingDistribution[4] + ratingDistribution[5]) }}</span>
            <span class="stat-percentage">{{ totalEvaluations > 0 ? Math.round(((ratingDistribution[4] + ratingDistribution[5]) / totalEvaluations) * 100) : 0 }}%</span>
          </div>
        </div>
        
        <div class="rating-distribution">
          <h3>评分分布</h3>
          <div class="distribution-chart">
            <div v-for="star in 5" :key="star" class="distribution-bar">
              <div class="bar-info">
                <span class="star-count">{{ star }}星</span>
                <span class="bar-value">{{ ratingDistribution[star] }}</span>
              </div>
              <div class="bar-container">
                <div class="bar" :style="{ width: getBarWidth(ratingDistribution[star]) + '%' }">
                </div>
              </div>
              <span class="bar-percentage">{{ totalEvaluations > 0 ? Math.round((ratingDistribution[star] / totalEvaluations) * 100) : 0 }}%</span>
            </div>
          </div>
        </div>
        
        <div class="evaluation-list">
          <div v-if="evaluations.length === 0" class="empty-state">
            <p>暂无评价记录</p>
          </div>
          <div v-else class="evaluation-item" v-for="evaluation in evaluations" :key="evaluation.repairId">
            <div class="evaluation-header">
              <div class="evaluation-rating">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= evaluation.rating }">★</span>
              </div>
              <div class="evaluation-meta">
                <span class="evaluation-resident">{{ evaluation.resident?.realName || '匿名居民' }}</span>
                <span class="evaluation-time">{{ formatDate(evaluation.createdAt) }}</span>
              </div>
            </div>
            
            <div class="evaluation-content">
              <p>{{ evaluation.comment || '暂无评价内容' }}</p>
            </div>
            
            <div class="evaluation-repair-info">
              <div class="info-row">
                <span class="info-label">相关服务单：</span>
                <span class="info-value">{{ evaluation.repairId }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">服务内容：</span>
                <span class="info-value">{{ evaluation.repairContent }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">服务时间：</span>
                <span class="info-value">{{ formatDate(evaluation.serviceTime) }}</span>
              </div>
            </div>
            
            <div class="evaluation-footer">
              <button class="btn btn-sm btn-primary" @click="viewEvaluationDetail(evaluation)">查看评价详情</button>
              <button class="btn btn-sm btn-info" @click="viewRepairDetail(evaluation.repairId)">查看服务详情</button>
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
              <p><strong>报修单号：</strong>{{ currentRepair.id }}</p>
              <p><strong>状态：</strong><span class="status" :class="currentRepair.status">{{ getStatusText(currentRepair.status) }}</span></p>
              <p><strong>报修时间：</strong>{{ formatDate(currentRepair.createdAt) }}</p>
              <p v-if="currentRepair.updatedAt"><strong>更新时间：</strong>{{ formatDate(currentRepair.updatedAt) }}</p>
              <p v-if="currentRepair.repairTime"><strong>处理时间：</strong>{{ formatDate(currentRepair.repairTime) }}</p>
              <p v-if="currentRepair.completedTime"><strong>完成时间：</strong>{{ formatDate(currentRepair.completedTime) }}</p>
            </div>
            
            <div class="detail-section">
              <h4>居民信息</h4>
              <p><strong>居民ID：</strong>{{ currentRepair.residentId }}</p>
              <p><strong>姓名：</strong>{{ currentRepair.resident.realName }}</p>
              <p v-if="currentRepair.resident.phone"><strong>手机号：</strong>{{ currentRepair.resident.phone }}</p>
              <p v-if="currentRepair.resident.email"><strong>邮箱：</strong>{{ currentRepair.resident.email }}</p>
              <p v-if="currentRepair.resident.address"><strong>居住地址：</strong>{{ currentRepair.resident.address }}</p>
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
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评价详情模态框 -->
    <div v-if="showEvaluationDetail && currentEvaluation" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>评价详情</h3>
          <button class="close-btn" @click="showEvaluationDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="evaluation-detail">
            <div class="detail-section">
              <h4>评价信息</h4>
              <div class="evaluation-rating-large">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= currentEvaluation.rating }">★</span>
                <span class="rating-number">{{ currentEvaluation.rating }}</span>
              </div>
              <p><strong>评价时间：</strong>{{ formatDate(currentEvaluation.createdAt) }}</p>
              <p><strong>评价内容：</strong></p>
              <div class="evaluation-content-large">
                <p>{{ currentEvaluation.comment || '暂无评价内容' }}</p>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>服务单信息</h4>
              <p><strong>服务单号：</strong>{{ currentEvaluation.repairId }}</p>
              <p><strong>服务内容：</strong>{{ currentEvaluation.repairContent }}</p>
              <p><strong>服务时间：</strong>{{ formatDate(currentEvaluation.serviceTime) }}</p>
              <button class="btn btn-sm btn-info" @click="viewRepairDetail(currentEvaluation.repairId); showEvaluationDetail = false">查看完整服务单</button>
            </div>
            
            <div class="detail-section">
              <h4>居民信息</h4>
              <p><strong>居民姓名：</strong>{{ currentEvaluation.resident?.realName || '匿名居民' }}</p>
              <p v-if="currentEvaluation.resident?.phone"><strong>联系电话：</strong>{{ currentEvaluation.resident.phone }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProviderServiceManagement',
  data() {
    return {
      username: '',
      activeTab: this.$route.query.tab || 'tasks',
      
      // 任务管理数据
      tasks: [],
      tasksSearchKeyword: '',
      tasksStatusFilter: '',
      
      // 服务记录数据
      serviceRecords: [],
      recordsSearchKeyword: '',
      recordsStatusFilter: '',
      
      // 服务评价数据
      evaluations: [],
      filteredEvaluations: [],
      evaluationsSearchKeyword: '',
      evaluationsRatingFilter: '',
      evaluationsTimeFilter: '',
      evaluationsSortBy: 'createdAt', // 创建时间或评分
      evaluationsSortOrder: 'desc', // 升序或降序
      averageRating: 0,
      totalEvaluations: 0,
      fiveStarCount: 0,
      ratingDistribution: {
        1: 0,
        2: 0,
        3: 0,
        4: 0,
        5: 0
      },
      // 分页数据
      currentPage: 1,
      pageSize: 10,
      totalPages: 1,
      
      // 图片预览
      previewImage: '',
      // 详情模态框
      showDetail: false,
      currentRepair: null,
      // 评价详情模态框
      showEvaluationDetail: false,
      currentEvaluation: null
    }
  },
  mounted() {
    this.username = localStorage.getItem('username')
    this.fetchTasks()
    this.fetchServiceRecords()
    this.fetchEvaluations()
  },
  watch: {
    // 实时筛选评价
    evaluationsRatingFilter() {
      this.fetchEvaluations()
    },
    evaluationsTimeFilter() {
      this.fetchEvaluations()
    }
  },
  methods: {
    // 任务管理方法
    fetchTasks() {
      const token = localStorage.getItem('token')
      
      // 构建查询参数
      const params = new URLSearchParams()
      if (this.tasksSearchKeyword) {
        params.append('searchKeyword', this.tasksSearchKeyword)
      }
      if (this.tasksStatusFilter) {
        params.append('status', this.tasksStatusFilter)
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
          // 为每个任务添加newStatus属性，默认值为当前status
          this.tasks = data.data.map(task => ({
            ...task,
            newStatus: task.status
          }))
        } else {
          alert('获取任务列表失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('获取任务列表失败：', error)
        alert('获取任务列表失败，请稍后重试')
      })
    },
    
    refreshTasks() {
      this.fetchTasks()
    },
    
    updateTaskStatus(repairId, status) {
      const token = localStorage.getItem('token')
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
          alert('任务状态更新成功')
          this.fetchTasks()
          this.fetchServiceRecords()
        } else {
          alert('任务状态更新失败：' + data.message)
        }
      })
      .catch(error => {
        console.error('任务状态更新失败：', error)
        alert('任务状态更新失败，请稍后重试')
      })
    },
    
    // 服务记录方法
    fetchServiceRecords() {
      const token = localStorage.getItem('token')
      
      // 构建查询参数
      const params = new URLSearchParams()
      if (this.recordsSearchKeyword) {
        params.append('searchKeyword', this.recordsSearchKeyword)
      }
      if (this.recordsStatusFilter) {
        params.append('status', this.recordsStatusFilter)
      }
      
      fetch(`/api/repair/list?${params.toString()}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          this.serviceRecords = data.data
        } else {
          alert('获取服务记录失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('获取服务记录失败：', error)
        alert('获取服务记录失败，请稍后重试')
      })
    },
    
    refreshRecords() {
      this.fetchServiceRecords()
    },
    
    // 服务评价方法
    fetchEvaluations() {
      const token = localStorage.getItem('token')
      
      // 构建查询参数
      const params = new URLSearchParams()
      if (this.evaluationsSearchKeyword) {
        params.append('searchKeyword', this.evaluationsSearchKeyword)
      }
      
      // 获取维修记录，然后提取评价信息
      fetch(`/api/repair/list?${params.toString()}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.status === 'success') {
          // 提取有评价的维修记录
          const repairEvaluations = []
          if (Array.isArray(data.data)) {
            data.data.forEach(repair => {
              if (repair.evaluation) {
                repairEvaluations.push({
                  repairId: repair.id,
                  repairContent: repair.content,
                  serviceTime: repair.updateTime,
                  ...repair.evaluation
                })
              }
            })
          }
          
          // 应用评分筛选
          let filteredEvaluations = repairEvaluations
          if (this.evaluationsRatingFilter) {
            filteredEvaluations = filteredEvaluations.filter(evaluation => 
              evaluation.rating === parseInt(this.evaluationsRatingFilter)
            )
          }
          
          // 应用时间筛选
          if (this.evaluationsTimeFilter) {
            const days = parseInt(this.evaluationsTimeFilter)
            const cutoffDate = new Date()
            cutoffDate.setDate(cutoffDate.getDate() - days)
            
            filteredEvaluations = filteredEvaluations.filter(evaluation => {
              const evaluationDate = new Date(evaluation.createdAt)
              return evaluationDate >= cutoffDate
            })
          }
          
          // 应用排序
          filteredEvaluations.sort((a, b) => {
            let valueA = a[this.evaluationsSortBy]
            let valueB = b[this.evaluationsSortBy]
            
            // 如果是日期类型，转换为时间戳比较
            if (this.evaluationsSortBy === 'createdAt' || this.evaluationsSortBy === 'serviceTime') {
              valueA = new Date(valueA).getTime()
              valueB = new Date(valueB).getTime()
            }
            
            if (this.evaluationsSortOrder === 'asc') {
              return valueA - valueB
            } else {
              return valueB - valueA
            }
          })
          
          // 保存筛选和排序后的评价，用于统计计算
          this.filteredEvaluations = filteredEvaluations
          
          // 应用分页
          this.totalEvaluations = filteredEvaluations.length
          this.totalPages = Math.ceil(this.totalEvaluations / this.pageSize)
          this.currentPage = 1 // 重置到第一页
          this.updatePageEvaluations()
          
          // 计算统计数据
          this.calculateEvaluationStats()
        } else {
          alert('获取评价记录失败：' + (data.message || '未知错误'))
        }
      })
      .catch(error => {
        console.error('获取评价记录失败：', error)
        alert('获取评价记录失败，请稍后重试')
      })
    },
    
    refreshEvaluations() {
      this.fetchEvaluations()
    },
    
    updatePageEvaluations() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.evaluations = this.filteredEvaluations.slice(start, end)
    },
    
    calculateEvaluationStats() {
      const evalData = this.filteredEvaluations || []
      if (evalData.length === 0) {
        this.averageRating = 0
        this.totalEvaluations = 0
        this.fiveStarCount = 0
        this.ratingDistribution = {
          1: 0,
          2: 0,
          3: 0,
          4: 0,
          5: 0
        }
        return
      }
      
      this.totalEvaluations = evalData.length
      const totalRating = evalData.reduce((sum, evalItem) => sum + evalItem.rating, 0)
      this.averageRating = totalRating / this.totalEvaluations
      
      // 计算评分分布
      this.ratingDistribution = {
        1: evalData.filter(evalItem => evalItem.rating === 1).length,
        2: evalData.filter(evalItem => evalItem.rating === 2).length,
        3: evalData.filter(evalItem => evalItem.rating === 3).length,
        4: evalData.filter(evalItem => evalItem.rating === 4).length,
        5: evalData.filter(evalItem => evalItem.rating === 5).length
      }
      
      this.fiveStarCount = this.ratingDistribution[5]
    },
    
    getBarWidth(value) {
      if (this.totalEvaluations === 0) return 0
      return Math.round((value / this.totalEvaluations) * 100)
    },
    
    // 通用方法
    viewImage(imageUrl) {
      this.previewImage = imageUrl
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
    
    viewRepairDetail(repairId) {
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
          this.currentRepair = data.data
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
    
    handleBack() {
      this.$router.push('/provider')
    },
    
    handleProfile() {
      this.$router.push('/provider/profile')
    },
    
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('realName')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
      localStorage.removeItem('address')
      localStorage.removeItem('idCard')
      this.$router.push('/login')
    },
    
    // 查看评价详情
    viewEvaluationDetail(evaluation) {
      this.currentEvaluation = evaluation
      this.showEvaluationDetail = true
    }
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

.service-tabs {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  padding: 12px 30px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-right: 10px;
  color: #666;
}

.tab-btn.active {
  border-bottom-color: #007bff;
  color: #007bff;
}

.tab-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 25px;
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

/* 任务管理和服务记录样式 */
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

.repair-actions {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.action-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.select-status {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 150px;
}

/* 模态框样式 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

/* 报修详情样式 */
.repair-detail {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.detail-section {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #007bff;
}

.detail-section h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.detail-section p {
  margin: 10px 0;
  color: #555;
}

.no-image {
  color: #999;
  font-style: italic;
  padding: 20px;
}

/* 服务评价样式 */
.evaluation-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

/* 服务评价详情样式 */
.evaluation-detail {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.evaluation-rating-large {
  font-size: 24px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.evaluation-rating-large .star {
  color: #ddd;
  margin-right: 5px;
}

.evaluation-rating-large .star.filled {
  color: #ffc107;
}

.evaluation-rating-large .rating-number {
  margin-left: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.evaluation-content-large {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #007bff;
  margin-top: 10px;
}

.evaluation-content-large p {
  margin: 0;
  color: #333;
  line-height: 1.8;
  font-size: 16px;
}

/* 评价列表样式增强 */
.evaluation-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 25px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.evaluation-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.evaluation-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 14px;
}

/* 模态框样式增强 */
.modal {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
}

.modal-body {
  padding: 25px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s, color 0.2s;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  margin-right: 15px;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-card:last-child {
  margin-right: 0;
}

.stat-card h4 {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 16px;
}

.average-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.average-rating span {
  font-size: 36px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 10px;
}

.rating-stars {
  color: #ffc107;
  font-size: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #28a745;
  display: block;
}

.stat-percentage {
  display: block;
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

/* 评分分布样式 */
.rating-distribution {
  background-color: white;
  border-radius: 8px;
  padding: 25px;
  margin-top: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.rating-distribution h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.distribution-bar {
  display: flex;
  align-items: center;
  gap: 15px;
}

.bar-info {
  width: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.star-count {
  font-weight: bold;
  color: #555;
}

.bar-value {
  color: #666;
  font-size: 14px;
}

.bar-container {
  flex: 1;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.bar {
  height: 100%;
  background-color: #007bff;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.bar-percentage {
  width: 50px;
  text-align: right;
  font-weight: bold;
  color: #666;
  font-size: 14px;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.evaluation-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.evaluation-rating {
  color: #ffc107;
  font-size: 20px;
}

.evaluation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.evaluation-resident {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.evaluation-time {
  font-size: 14px;
  color: #999;
}

.evaluation-content {
  margin-bottom: 15px;
  line-height: 1.6;
  color: #555;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.evaluation-repair-info {
  padding-top: 15px;
}

.evaluation-repair-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.star {
  cursor: pointer;
}

.star.filled {
  color: #ffc107;
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
  padding: 20px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  position: absolute;
  top: 10px;
  right: 15px;
}

.close-btn:hover {
  color: #333;
}

.image-preview {
  max-width: 800px;
  max-height: 90vh;
  position: relative;
}

.image-preview img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
