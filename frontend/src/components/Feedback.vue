<template>
  <div class="feedback-container">
    <!-- 成功提示 -->
    <div v-if="showSuccess" class="feedback success">
      <div class="feedback-content">
        <i class="icon-check-circle"></i>
        <div class="feedback-text">
          <div class="feedback-title">操作成功</div>
          <div class="feedback-message">{{ successMessage }}</div>
        </div>
        <button class="close-btn" @click="hideSuccess">×</button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="showError" class="feedback error">
      <div class="feedback-content">
        <i class="icon-exclamation-circle"></i>
        <div class="feedback-text">
          <div class="feedback-title">操作失败</div>
          <div class="feedback-message">{{ errorMessage }}</div>
        </div>
        <button class="close-btn" @click="hideError">×</button>
      </div>
    </div>

    <!-- 加载提示 -->
    <div v-if="showLoading" class="feedback loading">
      <div class="feedback-content">
        <div class="loading-spinner"></div>
        <div class="feedback-text">
          <div class="feedback-message">{{ loadingMessage }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Feedback',
  data() {
    return {
      showSuccess: false,
      showError: false,
      showLoading: false,
      successMessage: '',
      errorMessage: '',
      loadingMessage: '加载中...',
      timer: null
    }
  },
  methods: {
    // 显示成功提示
    showSuccessMessage(message, duration = 3000) {
      this.successMessage = message
      this.showSuccess = true
      this.clearTimer()
      if (duration > 0) {
        this.timer = setTimeout(() => {
          this.hideSuccess()
        }, duration)
      }
    },

    // 显示错误提示
    showErrorMessage(message, duration = 5000) {
      this.errorMessage = message
      this.showError = true
      this.clearTimer()
      if (duration > 0) {
        this.timer = setTimeout(() => {
          this.hideError()
        }, duration)
      }
    },

    // 显示加载提示
    showLoadingMessage(message = '加载中...') {
      this.loadingMessage = message
      this.showLoading = true
    },

    // 隐藏成功提示
    hideSuccess() {
      this.showSuccess = false
      this.successMessage = ''
      this.clearTimer()
    },

    // 隐藏错误提示
    hideError() {
      this.showError = false
      this.errorMessage = ''
      this.clearTimer()
    },

    // 隐藏加载提示
    hideLoading() {
      this.showLoading = false
    },

    // 清除定时器
    clearTimer() {
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
    }
  },
  beforeDestroy() {
    this.clearTimer()
  }
}
</script>

<style scoped>
.feedback-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feedback {
  min-width: 300px;
  max-width: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.feedback-content {
  display: flex;
  align-items: center;
  padding: 16px 20px;
}

.feedback.success {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.feedback.error {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.feedback.loading {
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.feedback i {
  font-size: 24px;
  margin-right: 12px;
}

.feedback-text {
  flex: 1;
}

.feedback-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.feedback-message {
  font-size: 14px;
  line-height: 1.4;
}

.close-btn {
  background: none;
  border: none;
  color: inherit;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  margin-left: 12px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: #856404;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>