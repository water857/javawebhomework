<template>
  <div class="payment-history-container">
    <div class="header-section">
      <button class="btn btn-back" @click="goBack">返回首页</button>
      <h2>缴费记录</h2>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-item">
        <label for="payment-method">支付方式：</label>
        <select id="payment-method" v-model="filter.paymentMethod">
          <option value="">全部</option>
          <option value="wechat">微信支付</option>
          <option value="alipay">支付宝</option>
          <option value="bank_transfer">银行转账</option>
          <option value="auto_deduction">自动扣费</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label for="start-date">开始日期：</label>
        <input type="date" id="start-date" v-model="filter.startDate">
      </div>
      
      <div class="filter-item">
        <label for="end-date">结束日期：</label>
        <input type="date" id="end-date" v-model="filter.endDate">
      </div>
      
      <div class="filter-item">
        <label for="min-amount">最小金额：</label>
        <input type="number" id="min-amount" v-model="filter.minAmount" step="0.01" placeholder="0.00">
      </div>
      
      <div class="filter-item">
        <label for="max-amount">最大金额：</label>
        <input type="number" id="max-amount" v-model="filter.maxAmount" step="0.01" placeholder="99999.99">
      </div>
      
      <div class="filter-item">
        <button class="btn btn-primary" @click="getPaymentHistory">查询</button>
        <button class="btn btn-secondary" @click="resetFilter">重置</button>
      </div>
    </div>
    
    <!-- 缴费记录列表 -->
    <div class="history-section">
      <table class="payment-table">
        <thead>
          <tr>
            <th>支付时间</th>
            <th>支付方式</th>
            <th>支付金额</th>
            <th>交易状态</th>
            <th>交易ID</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payment in payments" :key="payment.id">
            <td>{{ formatDateTime(payment.paymentTime) }}</td>
            <td>{{ getPaymentMethodText(payment.paymentMethod) }}</td>
            <td class="amount">¥{{ payment.amount.toFixed(2) }}</td>
            <td>
              <span class="payment-status" :class="payment.status">
                {{ getStatusText(payment.status) }}
              </span>
            </td>
            <td class="transaction-id">{{ payment.transactionId }}</td>
            <td>
              <button class="btn btn-view" @click="viewPaymentDetails(payment)">
                查看详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 空状态 -->
      <div v-if="payments.length === 0" class="empty-state">
        <p>暂无缴费记录</p>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="payments.length > 0">
      <button class="btn btn-secondary" @click="previousPage" :disabled="currentPage === 1">
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button class="btn btn-secondary" @click="nextPage" :disabled="currentPage === totalPages">
        下一页
      </button>
    </div>
    
    <!-- 支付详情模态框 -->
    <div class="modal" v-if="showDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>支付详情</h3>
          <button class="close-btn" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <label>交易ID：</label>
            <span>{{ selectedPayment.transactionId }}</span>
          </div>
          <div class="detail-item">
            <label>支付时间：</label>
            <span>{{ formatDateTime(selectedPayment.paymentTime) }}</span>
          </div>
          <div class="detail-item">
            <label>支付方式：</label>
            <span>{{ getPaymentMethodText(selectedPayment.paymentMethod) }}</span>
          </div>
          <div class="detail-item">
            <label>支付金额：</label>
            <span class="detail-amount">¥{{ selectedPayment.amount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <label>交易状态：</label>
            <span class="payment-status" :class="selectedPayment.status">
              {{ getStatusText(selectedPayment.status) }}
            </span>
          </div>
          <div class="detail-item">
            <label>自动扣费：</label>
            <span>{{ selectedPayment.autoDeduction ? '是' : '否' }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间：</label>
            <span>{{ formatDateTime(selectedPayment.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <label>更新时间：</label>
            <span>{{ formatDateTime(selectedPayment.updatedAt) }}</span>
          </div>
          
          <!-- 缴费凭证 -->
          <div class="receipt-section">
            <h4>缴费凭证</h4>
            <div class="receipt">
              <div class="receipt-header">
                <h5>智能社区物业费缴费凭证</h5>
              </div>
              <div class="receipt-content">
                <div class="receipt-item">
                  <span class="label">交易ID：</span>
                  <span class="value">{{ selectedPayment.transactionId }}</span>
                </div>
                <div class="receipt-item">
                  <span class="label">支付时间：</span>
                  <span class="value">{{ formatDateTime(selectedPayment.paymentTime) }}</span>
                </div>
                <div class="receipt-item">
                  <span class="label">支付方式：</span>
                  <span class="value">{{ getPaymentMethodText(selectedPayment.paymentMethod) }}</span>
                </div>
                <div class="receipt-item">
                  <span class="label">支付金额：</span>
                  <span class="value amount">¥{{ selectedPayment.amount.toFixed(2) }}</span>
                </div>
                <div class="receipt-item">
                  <span class="label">交易状态：</span>
                  <span class="value status">已支付</span>
                </div>
              </div>
              <div class="receipt-footer">
                <p>本凭证仅作为缴费记录凭证，不作为发票使用</p>
              </div>
            </div>
            <div class="receipt-actions">
              <button class="btn btn-primary" @click="printReceipt">打印凭证</button>
              <button class="btn btn-secondary" @click="downloadReceipt">下载凭证</button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { propertyFeeApi } from '../services/api'

export default {
  name: 'ResidentPaymentHistory',
  setup() {
    const payments = ref([])
    const filter = ref({
      paymentMethod: '',
      startDate: '',
      endDate: '',
      minAmount: '',
      maxAmount: ''
    })
    const currentPage = ref(1)
    const totalPages = ref(1)
    const showDetailModal = ref(false)
    const selectedPayment = ref(null)
    
    // 获取缴费记录
    const getPaymentHistory = async () => {
      try {
        const response = await propertyFeeApi.getPaymentHistory(currentPage.value, 10, {
          paymentMethod: filter.value.paymentMethod,
          startDate: filter.value.startDate,
          endDate: filter.value.endDate,
          minAmount: filter.value.minAmount || null,
          maxAmount: filter.value.maxAmount || null
        })
        payments.value = response.data || []
      } catch (error) {
        console.error('获取缴费记录失败:', error)
        alert('获取缴费记录失败，请稍后重试')
      }
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
    
    // 获取支付方式文本
    const getPaymentMethodText = (method) => {
      const methodMap = {
        'wechat': '微信支付',
        'alipay': '支付宝',
        'bank_transfer': '银行转账',
        'auto_deduction': '自动扣费'
      }
      return methodMap[method] || method
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'pending': '待处理',
        'success': '成功',
        'failed': '失败',
        'refunded': '已退款'
      }
      return statusMap[status] || status
    }
    
    // 重置筛选条件
    const resetFilter = () => {
      filter.value = {
        paymentMethod: '',
        startDate: '',
        endDate: '',
        minAmount: '',
        maxAmount: ''
      }
      getPaymentHistory()
    }
    
    // 查看支付详情
    const viewPaymentDetails = (payment) => {
      selectedPayment.value = payment
      showDetailModal.value = true
    }
    
    // 上一页
    const previousPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
        // 这里可以添加分页逻辑
      }
    }
    
    // 下一页
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
        // 这里可以添加分页逻辑
      }
    }
    
    // 打印凭证
    const printReceipt = () => {
      // 打印逻辑
      window.print()
    }
    
    // 下载凭证
    const downloadReceipt = () => {
      // 下载逻辑
      alert('下载功能开发中...')
    }
    
    // 返回首页
    const goBack = () => {
      router.push('/resident')
    }
    
    // 页面加载时获取缴费记录
    onMounted(() => {
      getPaymentHistory()
    })
    
    return {
      payments,
      filter,
      currentPage,
      totalPages,
      showDetailModal,
      selectedPayment,
      getPaymentHistory,
      formatDateTime,
      getPaymentMethodText,
      getStatusText,
      resetFilter,
      viewPaymentDetails,
      previousPage,
      nextPage,
      printReceipt,
      downloadReceipt,
      goBack
    }
  }
}
</script>

<style scoped>
.payment-history-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-back {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  margin-right: 20px;
}

.btn-back:hover {
  background-color: #e0e0e0;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: bold;
  white-space: nowrap;
}

.filter-item select,
.filter-item input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 120px;
}

.filter-item input[type="number"] {
  min-width: 100px;
}

.history-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.payment-table {
  width: 100%;
  border-collapse: collapse;
}

.payment-table th,
.payment-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.payment-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.payment-table tr:hover {
  background-color: #f9f9f9;
}

.amount {
  font-weight: bold;
  color: #2e7d32;
}

.payment-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.payment-status.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.payment-status.success {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.payment-status.failed {
  background-color: #ffebee;
  color: #c62828;
}

.payment-status.refunded {
  background-color: #e3f2fd;
  color: #1565c0;
}

.transaction-id {
  font-family: monospace;
  font-size: 12px;
  color: #666;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  padding: 50px 20px;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 20px 0;
}

.page-info {
  font-weight: bold;
  color: #666;
}

.modal {
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

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #eee;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.detail-item label {
  font-weight: bold;
  min-width: 100px;
  color: #666;
}

.detail-item .detail-amount {
  font-weight: bold;
  color: #2e7d32;
  font-size: 18px;
}

.receipt-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #ddd;
}

.receipt-section h4 {
  margin: 0 0 20px 0;
  text-align: center;
  color: #333;
}

.receipt {
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.receipt-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.receipt-header h5 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.receipt-content {
  margin-bottom: 20px;
}

.receipt-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.receipt-item:last-child {
  border-bottom: none;
}

.receipt-item .label {
  font-weight: bold;
  color: #666;
}

.receipt-item .value {
  color: #333;
}

.receipt-item .amount {
  font-weight: bold;
  color: #2e7d32;
}

.receipt-item .status {
  font-weight: bold;
  color: #2e7d32;
}

.receipt-footer {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.receipt-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background-color: #1976d2;
  color: white;
}

.btn-primary:hover {
  background-color: #1565c0;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.btn-view {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 6px 12px;
  font-size: 14px;
}

.btn-view:hover {
  background-color: #e0e0e0;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
