<template>
  <div class="community-container">
    <div class="community-header">
    <div class="header-left">
      <h1>邻里圈</h1>
    </div>
    <div class="header-actions">
      <button @click="showNotification" class="notification-btn">
        通知
        <span v-if="unreadNotifications > 0" class="notification-badge">{{ unreadNotifications }}</span>
      </button>
    </div>
  </div>

    <!-- 发布动态 -->
    <div class="post-create">
      <div class="user-avatar">
        <img :src="userAvatar" alt="用户头像" />
      </div>
      <div class="post-form">
        <textarea
          v-model="newPost.content"
          placeholder="分享你的生活点滴..."
          class="post-content"
        ></textarea>
        
        <!-- 图片上传预览 -->
        <div v-if="newPost.images.length > 0" class="image-preview-container">
          <div
            v-for="(image, index) in newPost.images"
            :key="index"
            class="image-preview"
          >
            <img :src="image" alt="预览图" />
            <button @click="removeImage(index)" class="remove-image">×</button>
          </div>
        </div>
        
        <div class="post-actions">
          <label class="upload-btn">
            <input
              type="file"
              accept="image/*"
              multiple
              @change="handleImageUpload"
              style="display: none"
            />
            <span class="icon">📷</span> 上传图片
          </label>
          
          <!-- 标签选择 -->
          <div class="tag-selector">
            <span class="icon">🏷️</span>
            <select v-model="selectedTagId" class="tag-select">
              <option value="">选择标签</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">
                {{ tag.name }}
              </option>
            </select>
            <button @click="addTag" class="add-tag-btn">添加</button>
          </div>
          
          <!-- 隐私设置固定为公开 -->
          <button @click="publishPost" class="publish-btn">发布</button>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <div class="filter-options">
        <button
          v-for="filter in filters"
          :key="filter.value"
          :class="['filter-btn', { active: currentFilter === filter.value }]"
          @click="setFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>
      
      <div class="search-box">
        <input
          v-model="searchKeyword"
          @input="handleSearch"
          placeholder="搜索动态..."
          class="search-input"
        />
        <button @click="handleSearch" class="search-btn">搜索</button>
      </div>
    </div>

    <!-- 动态列表 -->
    <div class="post-list">
      <div v-for="post in posts" :key="post.id" class="post-item">
        <div class="post-header">
          <div class="post-user">
            <div class="user-avatar">
              <img :src="userAvatar" alt="用户头像" />
            </div>
            <div class="user-info">
              <div class="user-name">{{ getUserName(post.userId, post) }}</div>
              <div class="post-time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
          
          <!-- 隐私标识 -->
          <div class="privacy-indicator" :title="getPrivacyText(post.privacy)">
            {{ post.privacy === 'public' ? '🌍' : '🔒' }}
          </div>
        </div>
        
        <!-- 动态内容 -->
        <div class="post-content">{{ post.content }}</div>
        
        <!-- 动态图片 -->
        <div v-if="post.images && post.images.length > 0" class="post-images">
          <img
            v-for="(image, index) in post.images"
            :key="index"
            :src="image.imageUrl"
            alt="动态图片"
            class="post-image"
          />
        </div>
        
        <!-- 动态标签 -->
        <div v-if="post.tags && post.tags.length > 0" class="post-tags">
          <span
            v-for="tag in post.tags"
            :key="tag.id"
            class="post-tag"
            @click="filterByTag(tag.id)"
          >
            #{{ tag.name }}
          </span>
        </div>
        
        <!-- 互动统计 -->
        <div class="post-stats">
          <span class="stat-item">
            <span class="icon">👁️</span> {{ post.viewCount }}
          </span>
          <span class="stat-item">
            <span class="icon">❤️</span> {{ post.likeCount }}
          </span>
          <span class="stat-item">
            <span class="icon">💬</span> {{ post.commentCount }}
          </span>
        </div>
        
        <!-- 互动按钮 -->
        <div class="post-actions">
          <button
            :class="['action-btn', { active: isLiked(post.id) }]"
            @click="toggleLike(post.id)"
          >
            <span class="icon">❤️</span> {{ isLiked(post.id) ? '取消点赞' : '点赞' }}
          </button>
          <button @click="showComments(post.id)" class="action-btn">
            <span class="icon">💬</span> 评论
          </button>
        </div>
        
        <!-- 评论区 -->
        <div v-if="openCommentsPostId === post.id" class="comments-section">
          <div v-if="getPostComments(post.id).length > 0" class="comment-list">
            <div
              v-for="comment in getPostComments(post.id)"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-avatar">
                <img :src="userAvatar" alt="用户头像" />
              </div>
              <div class="comment-content">
                <div class="comment-user">{{ comment.user ? (comment.user.realName || comment.user.username) : getUserName(comment.userId) }}</div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
              </div>
              
              <!-- 显示回复 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
                <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
                >
                  <div class="comment-avatar">
                    <img :src="userAvatar" alt="用户头像" />
                  </div>
                  <div class="comment-content">
                    <div class="comment-user">{{ reply.user ? (reply.user.realName || reply.user.username) : getUserName(reply.userId) }}</div>
                    <div class="comment-text">{{ reply.content }}</div>
                    <div class="comment-time">{{ formatTime(reply.createdAt) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 发表评论 -->
          <div class="comment-form">
            <input
              v-model="newComment.content"
              :data-post-id="post.id"
              placeholder="写下你的评论..."
              class="comment-input"
              @keyup.enter="addComment(post.id)"
            />
            <button @click="addComment(post.id)" class="comment-btn">发送</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div class="load-more">
      <button @click="loadMorePosts" v-if="hasMorePosts" class="load-more-btn">
        加载更多
      </button>
      <div v-else class="no-more">没有更多动态了</div>
    </div>

    <!-- 通知弹窗 -->
    <div v-if="showNotificationModal" class="notification-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>通知</h3>
          <button @click="showNotificationModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div v-if="notifications.length === 0" class="no-notifications">
            暂无通知
          </div>
          <div v-else class="notification-list">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              :class="['notification-item', { read: notification.isRead }]"
              @click="markNotificationAsRead(notification.id)"
            >
              <div class="notification-content">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { apiRequest } from '../services/api.js'

export default {
  name: 'ResidentCommunity',
  data() {
    return {
      // 用户信息
      userAvatar: 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22 width%3D%2250%22 height%3D%2250%22 viewBox%3D%220 0 50 50%22%3E%3Ccircle cx%3D%2225%22 cy%3D%2225%22 r%3D%2225%22 fill%3D%22%234CAF50%22%2F%3E%3Ctext x%3D%2225%22 y%3D%2235%22 font-family%3D%22Arial%22 font-size%3D%2230%22 text-anchor%3D%22middle%22 fill%3D%22white%22%3E👤%3C%2Ftext%3E%3C%2Fsvg%3E',
      userId: parseInt(this.getLocalStorageItem('userId') || '0'),
      
      // 新动态
      newPost: {
        content: '',
        privacy: 'public',
        images: [],
        tagIds: []
      },
      selectedTagId: '',
      
      // 标签
      tags: [],
      residents: [],
      
      // 动态列表
      posts: [],
      page: 1,
      pageSize: 10,
      hasMorePosts: true,
      
      // 筛选和搜索
      currentFilter: 'all',
      filters: [
        { value: 'all', label: '全部' },
        { value: 'time', label: '最新' },
        { value: 'popularity', label: '热门' }
      ],
      searchKeyword: '',
      
      // 评论
      openCommentsPostId: null,
      comments: {},
      loadingComments: new Set(),
      newComment: { content: '' },
      
      // 点赞状态
      likedPosts: new Set(),
      
      // 通知
      notifications: [],
      unreadNotifications: 0,
      showNotificationModal: false,
      
      // 轮询定时器
      pollingTimer: null,
      pollingInterval: 10000 // 10秒轮询一次
    }
  },
  
  mounted() {
    this.loadTags()
    this.loadResidents()
    this.loadTimeline()
    this.loadNotifications()
    // 启动轮询
    this.startPolling()
  },
  
  beforeUnmount() {
    this.stopPolling()
  },
  
  methods: {
    // 安全获取localStorage项的辅助函数
    getLocalStorageItem(key) {
      if (typeof localStorage !== 'undefined') {
        return localStorage.getItem(key)
      }
      return null
    },
    // 启动轮询
    startPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
      }
      this.pollingTimer = setInterval(() => {
        this.pollForUpdates()
      }, this.pollingInterval)
    },
    
    // 停止轮询
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
      }
    },
    
    // 轮询检查更新
    async pollForUpdates() {
      // 检查新通知
      await this.checkNewNotifications()
      
      // 检查新动态（仅在时间线视图）
      if (this.currentFilter === 'all' && this.page === 1) {
        await this.checkNewPosts()
      }
    },
    
    // 检查新通知
    async checkNewNotifications() {
      try {
        const response = await apiRequest('/community/notifications?page=1&pageSize=10')
        const newNotifications = response.data
        const oldUnreadCount = this.unreadNotifications
        const newUnreadCount = newNotifications.filter(n => !n.isRead).length
        
        // 更新通知列表和未读数
        this.notifications = newNotifications
        this.unreadNotifications = newUnreadCount
        
        // 如果有新通知，显示提示
        if (newUnreadCount > oldUnreadCount) {
          console.log(`收到 ${newUnreadCount - oldUnreadCount} 条新通知`)
        }
      } catch (error) {
        console.error('检查新通知失败:', error)
      }
    },
    
    // 检查新动态
    async checkNewPosts() {
      try {
        const response = await apiRequest('/community/timeline?page=1&pageSize=10')
        const newPosts = response.data
        
        // 检查是否有新帖子
        if (newPosts.length > 0 && this.posts.length > 0) {
          const latestPostId = this.posts[0].id
          const newPostsToAdd = []
          
          for (const post of newPosts) {
            if (post.id > latestPostId) {
              newPostsToAdd.push(post)
            }
          }
          
          // 添加新帖子到列表顶部
          if (newPostsToAdd.length > 0) {
            newPostsToAdd.reverse().forEach(post => {
              this.posts.unshift(post)
              this.checkLikeStatus(post.id)
            })
            console.log(`收到 ${newPostsToAdd.length} 条新动态`)
          }
        }
      } catch (error) {
        console.error('检查新动态失败:', error)
      }
    },
    
    // 加载标签
    async loadTags() {
      try {
        const response = await apiRequest('/community/tags')
        this.tags = response.data
      } catch (error) {
        console.error('加载标签失败:', error)
      }
    },
    
    // 加载居民列表
    async loadResidents() {
      try {
        const response = await apiRequest('/residents')
        // 确保residents始终是一个数组
        this.residents = Array.isArray(response.data) ? response.data : []
      } catch (error) {
        console.error('加载居民列表失败:', error)
        // 发生错误时确保residents是一个数组
        this.residents = []
      }
    },
    
    // 加载时间线
    async loadTimeline() {
      try {
        let endpoint = `/community/timeline?page=${this.page}&pageSize=${this.pageSize}`
        
        if (this.currentFilter === 'time') {
          endpoint = `/community/filter/time?timeRange=latest&page=${this.page}&pageSize=${this.pageSize}`
        } else if (this.currentFilter === 'popularity') {
          endpoint = `/community/filter/popularity?page=${this.page}&pageSize=${this.pageSize}`
        }
        
        const response = await apiRequest(endpoint)
        
        const newPosts = response.data
        if (newPosts.length < this.pageSize) {
          this.hasMorePosts = false
        }
        
        // 调试日志：查看post.id的实际值
        console.log('从后端返回的posts:', newPosts)
        newPosts.forEach(post => {
          console.log('post.id:', post.id, '类型:', typeof post.id)
        })
        
        this.posts = [...this.posts, ...newPosts]
        
        // 加载点赞状态
        newPosts.forEach(post => {
          this.checkLikeStatus(post.id)
        })
        
        this.page++
      } catch (error) {
        console.error('加载时间线失败:', error)
      }
    },
    
    // 加载更多
    loadMorePosts() {
      this.loadTimeline()
    },
    
    // 图片上传
    async handleImageUpload(event) {
      const files = event.target.files
      if (files.length === 0) return
      
      // 遍历所有选择的文件
      for (let i = 0; i < files.length; i++) {
        const formData = new FormData()
        formData.append('image', files[i])
        
        try {
          // 图片上传需要使用axios直接调用，因为formData不能使用apiRequest
          const response = await axios.post('/community/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
          
          this.newPost.images.push(response.data.data)
        } catch (error) {
          console.error('上传图片失败:', error)
        }
      }
    },
    
    // 移除图片
    removeImage(index) {
      this.newPost.images.splice(index, 1)
    },
    
    // 添加标签
    addTag() {
      if (this.selectedTagId && !this.newPost.tagIds.includes(parseInt(this.selectedTagId))) {
        this.newPost.tagIds.push(parseInt(this.selectedTagId))
        this.selectedTagId = ''
      }
    },
    
    // 发布动态
    async publishPost() {
      if (!this.newPost.content.trim()) return
      
      try {
        const response = await apiRequest('/community/publish', 'POST', this.newPost)
        
        // 添加到动态列表顶部
        this.posts.unshift(response.data)
        
        // 清空表单
        this.newPost = {
          content: '',
          privacy: 'public',
          images: [],
          tagIds: []
        }
      } catch (error) {
        console.error('发布动态失败:', error)
      }
    },
    
    // 切换点赞
    async toggleLike(postId) {
      try {
        // 确保postId是一个有效的数字
        const numericPostId = parseInt(postId)
        if (isNaN(numericPostId)) {
          console.error('无效的postId:', postId)
          return
        }
        await apiRequest(`/community/like/${numericPostId}`, 'POST')
        
        // 更新点赞状态
        if (this.likedPosts.has(numericPostId)) {
          this.likedPosts.delete(numericPostId)
          // 更新点赞数
          const post = this.posts.find(p => p.id === numericPostId)
          if (post) post.likeCount--
        } else {
          this.likedPosts.add(numericPostId)
          // 更新点赞数
          const post = this.posts.find(p => p.id === numericPostId)
          if (post) post.likeCount++
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
      }
    },
    
    // 检查点赞状态
    async checkLikeStatus(postId) {
      try {
        // 确保postId是一个有效的数字
        const numericPostId = parseInt(postId)
        if (isNaN(numericPostId)) {
          console.error('无效的postId:', postId)
          return
        }
        const response = await apiRequest(`/community/like/${numericPostId}`)
        
        if (response.data) {
          this.likedPosts.add(numericPostId)
        }
      } catch (error) {
        console.error('检查点赞状态失败:', error)
      }
    },
    
    // 是否点赞
    isLiked(postId) {
      return this.likedPosts.has(parseInt(postId))
    },
    
    // 显示/隐藏评论
    showComments(postId) {
      if (this.openCommentsPostId === postId) {
        this.openCommentsPostId = null
      } else {
        this.openCommentsPostId = postId
        this.loadComments(postId)
      }
    },
    
    // 加载评论
    async loadComments(postId) {
      // 确保postId是一个有效的数字
      const numericPostId = parseInt(postId)
      if (isNaN(numericPostId)) {
        console.error('加载评论失败: 无效的postId:', postId)
        return
      }
      
      try {
        // 设置加载状态
        this.loadingComments.add(numericPostId)
        
        console.log(`正在加载帖子 ${numericPostId} 的评论`)
        const response = await apiRequest(`/community/post/comments/${numericPostId}`)
        
        // 验证响应数据结构
        if (Array.isArray(response.data)) {
          // 使用一致的数字类型作为键
          this.comments[numericPostId] = response.data
          console.log(`成功加载帖子 ${numericPostId} 的评论，共 ${response.data.length} 条`)
        } else {
          console.error('加载评论失败: 无效的评论数据格式:', response.data)
        }
      } catch (error) {
        console.error(`加载帖子 ${numericPostId} 的评论失败:`, error || '未知错误')
        if (error.response) {
          // 服务器返回了错误状态码
          console.error('HTTP状态:', error.response.status)
          if (error.response.status === 401) {
            console.error('未授权访问，请重新登录')
          } else if (error.response.status === 404) {
            console.error('请求的评论不存在')
          } else {
            console.error('服务器错误:', error.response.data || '未知错误')
          }
        } else if (error.request) {
          // 请求已发送但没有收到响应
          console.error('服务器无响应，请检查网络连接或服务器状态')
        } else {
          // 请求配置错误
          console.error('请求错误:', error.message || '未知错误')
        }
      } finally {
        // 清除加载状态
        this.loadingComments.delete(numericPostId)
        console.log(`帖子 ${numericPostId} 的评论加载完成或失败`)
      }
    },
    
    // 添加评论
    async addComment(postId) {
      const content = this.newComment.content.trim()
      if (!content) return
      
      try {
        // 确保postId是一个有效的数字
        const numericPostId = parseInt(postId)
        if (isNaN(numericPostId)) {
          console.error('无效的postId:', postId)
          console.error('无效的帖子ID')
          return
        }
        const response = await apiRequest(`/community/comment/${numericPostId}`, 'POST', { content })
        
        // 添加到评论列表（使用一致的数字类型作为键）
        if (!this.comments[numericPostId]) {
          this.comments[numericPostId] = []
        }
        this.comments[numericPostId].push(response.data)
        
        // 更新评论数
        const post = this.posts.find(p => p.id === numericPostId)
        if (post) post.commentCount++
        
        // 清空输入
        this.newComment.content = ''
      } catch (error) {
        console.error('添加评论失败:', error || '未知错误')
      }
    },
    
    // 获取评论
    getPostComments(postId) {
      const numericPostId = parseInt(postId)
      return this.comments[numericPostId] || []
    },
    
    // 筛选
    setFilter(filter) {
      this.currentFilter = filter
      this.page = 1
      this.posts = []
      this.hasMorePosts = true
      this.loadTimeline()
    },
    
    // 搜索
    handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.page = 1
        this.posts = []
        this.hasMorePosts = true
        this.loadTimeline()
        return
      }
      
      this.searchPosts()
    },
    
    // 搜索动态
    async searchPosts() {
      try {
        const response = await apiRequest(`/community/search?keyword=${this.searchKeyword}&page=1&pageSize=${this.pageSize}`)
        
        this.posts = response.data
        this.hasMorePosts = response.data.length === this.pageSize
      } catch (error) {
        console.error('搜索动态失败:', error)
      }
    },
    
    // 按标签筛选
    async filterByTag(tagId) {
      try {
        const response = await apiRequest(`/community/tags/${tagId}?page=1&pageSize=${this.pageSize}`)
        
        this.posts = response.data
        this.hasMorePosts = response.data.length === this.pageSize
      } catch (error) {
        console.error('按标签筛选失败:', error)
      }
    },
    
    // 加载通知
    async loadNotifications() {
      try {
        const response = await apiRequest('/community/notifications')
        
        this.notifications = response.data
        this.unreadNotifications = this.notifications.filter(n => !n.isRead).length
      } catch (error) {
        console.error('加载通知失败:', error)
      }
    },
    
    // 显示通知
    showNotification() {
      this.showNotificationModal = true
    },
    
    // 标记通知已读
    async markNotificationAsRead(notificationId) {
      try {
        await apiRequest(`/community/notification/read/${notificationId}`, 'POST')
        
        // 更新本地状态
        const notification = this.notifications.find(n => n.id === notificationId)
        if (notification) {
          notification.isRead = true
          this.unreadNotifications--
        }
      } catch (error) {
        console.error('标记通知已读失败:', error)
      }
    },
    
    // 获取用户名
    getUserName(userId, post) {
      // 优先从post.user中获取真实姓名
      if (post && post.user && post.user.realName) {
        return post.user.realName
      }
      
      // 确保residents是数组
      if (!Array.isArray(this.residents)) {
        return '用户'
      }
      
      const user = this.residents.find(r => r.id === userId)
      return user ? (user.realName || user.name || '用户') : '用户'
    },
    
    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      const minutes = Math.floor(diff / (1000 * 60))
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (minutes < 60) {
        return `${minutes}分钟前`
      } else if (hours < 24) {
        return `${hours}小时前`
      } else {
        return `${days}天前`
      }
    },
    
    // 返回首页
    
    // 获取隐私文本
    getPrivacyText(privacy) {
      return privacy === 'public' ? '公开' : '指定用户可见'
    }
  }
}
</script>

<style scoped>
.community-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ddd;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.community-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.notification-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  position: relative;
}

.notification-badge {
  background-color: red;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  position: absolute;
  top: -5px;
  right: -5px;
}

.post-create {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-form {
  flex: 1;
}

.post-content {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  min-height: 100px;
  resize: vertical;
  font-size: 14px;
  margin-bottom: 10px;
}

.image-preview-container {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 5px;
  right: 5px;
  background-color: rgba(255, 0, 0, 0.8);
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.upload-btn, .tag-selector {
  display: flex;
  align-items: center;
  gap: 5px;
  background-color: #f0f0f0;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.icon {
  font-size: 16px;
}

.tag-select {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
}

.add-tag-btn, .add-user-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.user-selector {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 10px;
}

.selected-users {
  display: flex;
  gap: 5px;
  margin-top: 5px;
}

.selected-user {
  background-color: #e0e0e0;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.remove-user {
  background-color: transparent;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.publish-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: auto;
}

.publish-btn:hover {
  background-color: #45a049;
}

.filter-bar {
  background-color: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-options {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.filter-btn {
  background-color: #f0f0f0;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
}

.filter-btn.active {
  background-color: #4CAF50;
  color: white;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
}

.search-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.post-list {
  margin-bottom: 20px;
}

.post-item {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: bold;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.privacy-indicator {
  font-size: 18px;
  cursor: help;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
  color: #333;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 15px;
}

.post-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.post-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.post-tag {
  background-color: #e0e0e0;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
}

.post-tag:hover {
  background-color: #d0d0d0;
}

.post-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-actions {
  display: flex;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.action-btn {
  background-color: transparent;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.action-btn:hover {
  background-color: #f0f0f0;
}

.action-btn.active {
  color: #ff6b6b;
}

.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-list {
  margin-bottom: 15px;
}

.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.comment-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.4;
  margin: 5px 0;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-form {
  display: flex;
  gap: 10px;
}

.comment-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
}

.comment-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.load-more-btn {
  background-color: #f0f0f0;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.no-more {
  color: #999;
  font-size: 14px;
}

.notification-modal {
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
  width: 400px;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background-color: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(80vh - 60px);
}

.no-notifications {
  text-align: center;
  color: #999;
  padding: 20px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-item {
  padding: 15px;
  border-radius: 4px;
  background-color: #f9f9f9;
  cursor: pointer;
}

.notification-item.read {
  background-color: #f0f0f0;
  opacity: 0.7;
}

.notification-content {
  font-size: 14px;
  margin-bottom: 5px;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

@media (max-width: 600px) {
  .community-container {
    padding: 10px;
  }
  
  .post-create {
    flex-direction: column;
  }
  
  .post-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .publish-btn {
    margin-left: 0;
    width: 100%;
  }
  
  .filter-options {
    flex-wrap: wrap;
  }
  
  .post-images {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
  
  .post-image {
    height: 100px;
  }
}
</style>
