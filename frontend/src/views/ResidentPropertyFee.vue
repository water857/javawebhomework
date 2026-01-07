<template>
  <div class="property-fee-container">
    <div class="header-section">
      <button class="btn btn-back" @click="goBack">返回首页</button>
      <h2>物业费账单</h2>
    </div>
    
    <!-- 筛选和排序 -->
    <div class="filter-section">
      <div class="filter-item">
        <label for="status-filter">状态筛选：</label>
        <select id="status-filter" v-model="filter.status">
          <option value="">全部</option>
          <option value="unpaid">未支付</option>
          <option value="paid">已支付</option>
          <option value="overdue">逾期</option>
          <option value="partially_paid">部分支付</option>
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
        <button class="btn btn-primary" @click="getBills">查询</button>
        <button class="btn btn-secondary" @click="resetFilter">重置</button>
      </div>
    </div>
    
    <!-- 账单列表 -->
    <div class="bills-section">
      <div class="bill-card" v-for="bill in bills" :key="bill.id">
        <div class="bill-header">
          <div class="period-info">
            <h3>费用周期：{{ formatDate(bill.periodStart) }} 至 {{ formatDate(bill.periodEnd) }}</h3>
            <span class="bill-status" :class="bill.status">{{ getStatusText(bill.status) }}</span>
          </div>
          <div class="amount-info">
            <span class="total-amount">¥{{ bill.amount.toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="bill-details">
          <h4>费用明细：</h4>
          <ul class="fee-items">
            <li v-for="(item, index) in (bill.items ? JSON.parse(bill.items) : [{ name: '物业费', amount: bill.amount }])" :key="index">
              {{ item.name }}：¥{{ item.amount.toFixed(2) }}
            </li>
          </ul>
          
          <div class="due-date">
            <span>缴费截止日期：{{ formatDate(bill.dueDate) }}</span>
            <span v-if="bill.status === 'overdue'" class="overdue-tag">已逾期</span>
          </div>
          
          <div class="reminder-status">
            <span>催缴提醒：</span>
            <span :class="bill.paymentDeadlineReminderSent || false ? 'reminder-sent' : 'reminder-not-sent'">
              {{ bill.paymentDeadlineReminderSent || false ? '已发送' : '未发送' }}
            </span>
          </div>
        </div>
        
        <div class="bill-actions">
          <button 
            class="btn btn-pay" 
            v-if="bill.status !== 'paid'" 
            @click="payBill(bill)"
          >
            一键支付
          </button>
          <button 
            class="btn btn-view" 
            @click="viewPaymentDetails(bill)"
          >
            查看详情
          </button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="bills.length === 0" class="empty-state">
        <p>暂无账单数据</p>
      </div>
    </div>
    
    <!-- 支付确认模态框 -->
    <div class="modal" v-if="showPaymentModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>支付确认</h3>
          <button class="close-btn" @click="showPaymentModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <p>确认支付账单：</p>
          <p>费用周期：{{ formatDate(selectedBill.periodStart) }} 至 {{ formatDate(selectedBill.periodEnd) }}</p>
          <p>总金额：¥{{ selectedBill.amount.toFixed(2) }}</p>
          
          <div class="payment-method">
            <label>选择支付方式：</label>
            <div class="method-options">
              <label>
                <input type="radio" v-model="selectedPaymentMethod" value="wechat"> 微信支付
              </label>
              <label>
                <input type="radio" v-model="selectedPaymentMethod" value="alipay"> 支付宝
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showPaymentModal = false">取消</button>
          <button class="btn btn-primary" @click="confirmPayment" :disabled="isProcessing">
            {{ isProcessing ? '支付中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 支付结果模态框 -->
    <div class="modal" v-if="showResultModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>支付结果</h3>
          <button class="close-btn" @click="showResultModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="result-icon" :class="paymentResult.status">
            <span v-if="paymentResult.status === 'success'">✓</span>
            <span v-else>✗</span>
          </div>
          <h4>{{ paymentResult.message }}</h4>
          <p v-if="paymentResult.status === 'success'">交易ID：{{ paymentResult.transactionId }}</p>
          <p v-if="paymentResult.status === 'success'">支付时间：{{ formatDateTime(new Date()) }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="showResultModal = false">确定</button>
        </div>
      </div>
    </div>
    
    <!-- 账单详情模态框 -->
    <div class="modal" v-if="showBillDetailModal">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <div class="header-buttons">
            <button class="btn btn-back" @click="showBillDetailModal = false">
              返回
            </button>
            <h3>账单详情</h3>
          </div>
          <button class="close-btn" @click="showBillDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <div class="detail-row">
              <div class="detail-label">账单ID：</div>
              <div class="detail-value">{{ selectedBillDetail.id }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">费用周期：</div>
              <div class="detail-value">
                {{ formatDate(selectedBillDetail.periodStart) }} 至 {{ formatDate(selectedBillDetail.periodEnd) }}
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-label">总金额：</div>
              <div class="detail-value amount">¥{{ selectedBillDetail.amount.toFixed(2) }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">账单状态：</div>
              <div class="detail-value">
                <span class="bill-status" :class="selectedBillDetail.status">
                  {{ getStatusText(selectedBillDetail.status) }}
                </span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-label">缴费截止日期：</div>
              <div class="detail-value">
                {{ formatDate(selectedBillDetail.dueDate) }}
                <span v-if="selectedBillDetail.status === 'overdue'" class="overdue-tag">已逾期</span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-label">催缴提醒：</div>
              <div class="detail-value">
                {{ selectedBillDetail.paymentDeadlineReminderSent || false ? '已发送' : '未发送' }}
              </div>
            </div>
            
            <div class="detail-section">
              <h4>费用明细：</h4>
              <table class="fee-items-table">
                <thead>
                  <tr>
                    <th>费用项目</th>
                    <th>金额</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in (selectedBillDetail.items ? JSON.parse(selectedBillDetail.items) : [{ name: '物业费', amount: selectedBillDetail.amount }])" :key="index">
                    <td>{{ item.name }}</td>
                    <td>¥{{ item.amount.toFixed(2) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <div class="detail-section">
              <h4>相关支付记录：</h4>
              <table class="payments-table">
                <thead>
                  <tr>
                    <th>支付ID</th>
                    <th>交易ID</th>
                    <th>支付金额</th>
                    <th>支付方式</th>
                    <th>支付时间</th>
                    <th>状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="payment in (selectedBillDetail.payments || [])" :key="payment.id">
                    <td>{{ payment.id }}</td>
                    <td class="transaction-id">{{ payment.transactionId }}</td>
                    <td>¥{{ payment.amount.toFixed(2) }}</td>
                    <td>{{ getPaymentMethodText(payment.paymentMethod) }}</td>
                    <td>{{ formatDateTime(payment.paymentTime) }}</td>
                    <td>
                      <span class="payment-status" :class="payment.status">
                        {{ getPaymentStatusText(payment.status) }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="(selectedBillDetail.payments || []).length === 0">
                    <td colspan="6" class="no-data">暂无支付记录</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="showBillDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { propertyFeeApi } from '../services/api'

export default {
  name: 'ResidentPropertyFee',
  setup() {
    const router = useRouter()
    const bills = ref([])
    const filter = ref({
      status: '',
      startDate: '',
      endDate: ''
    })
    const showPaymentModal = ref(false)
    const showResultModal = ref(false)
    const selectedBill = ref(null)
    const selectedPaymentMethod = ref('wechat')
    const isProcessing = ref(false)
    const paymentResult = ref({})
    const showBillDetailModal = ref(false)
    const selectedBillDetail = ref(null)
    
    // 获取账单列表
    const getBills = async () => {
      try {
        const response = await propertyFeeApi.getBills(1, 100, {
          status: filter.value.status,
          startDate: filter.value.startDate,
          endDate: filter.value.endDate
        })
        bills.value = response.data || []
      } catch (error) {
        console.error('获取账单失败:', error)
        alert('获取账单失败，请稍后重试')
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 格式化日期时间
    const formatDateTime = (date) => {
      if (typeof date === 'string') {
        date = new Date(date)
      }
      return date.toLocaleString('zh-CN')
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'unpaid': '未支付',
        'paid': '已支付',
        'overdue': '已逾期',
        'partially_paid': '部分支付'
      }
      return statusMap[status] || status
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
    
    // 获取支付状态文本
    const getPaymentStatusText = (status) => {
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
        status: '',
        startDate: '',
        endDate: ''
      }
      getBills()
    }
    
    // 支付账单
    const payBill = (bill) => {
      selectedBill.value = bill
      selectedPaymentMethod.value = 'wechat'
      showPaymentModal.value = true
    }
    
    // 确认支付
    const confirmPayment = async () => {
      if (!selectedBill.value) return
      
      isProcessing.value = true
      try {
        const response = await propertyFeeApi.initiatePayment({
          billId: selectedBill.value.id,
          paymentMethod: selectedPaymentMethod.value,
          amount: selectedBill.value.amount
        })
        
        paymentResult.value = {
          status: 'success',
          message: '支付成功！',
          transactionId: response.data.transactionId
        }
        
        // 更新账单状态
        getBills()
      } catch (error) {
        console.error('支付失败:', error)
        paymentResult.value = {
          status: 'failed',
          message: error.response?.data?.message || '支付失败，请稍后重试'
        }
      } finally {
        isProcessing.value = false
        showPaymentModal.value = false
        showResultModal.value = true
      }
    }
    
    // 查看账单详情
    const viewPaymentDetails = async (bill) => {
      try {
        // 获取账单详情
        const response = await propertyFeeApi.getBillDetail(bill.id)
        selectedBillDetail.value = response.data.bill || bill
        // 加载支付记录
        const paymentsResponse = await propertyFeeApi.getPaymentHistory(1, 10, {
          billId: bill.id
        })
        selectedBillDetail.value.payments = paymentsResponse.data || []
        showBillDetailModal.value = true
      } catch (error) {
        console.error('获取账单详情失败:', error)
        alert('获取账单详情失败，请稍后重试')
      }
    }
    
    // 自动扣费设置
    const setupAutoDeduction = async () => {
      try {
        // 这里可以根据实际需求调用相应的API方法
        alert('自动扣费设置成功')
      } catch (error) {
        console.error('设置自动扣费失败:', error)
        alert('设置自动扣费失败，请稍后重试')
      }
    }
    
    // 返回首页
    const goBack = () => {
      router.push('/resident')
    }
    
    // 页面加载时获取账单
    onMounted(() => {
      getBills()
    })
    
    return {
      bills,
      filter,
      showPaymentModal,
      showResultModal,
      selectedBill,
      selectedPaymentMethod,
      isProcessing,
      paymentResult,
      showBillDetailModal,
      selectedBillDetail,
      getBills,
      formatDate,
      formatDateTime,
      getStatusText,
      getPaymentMethodText,
      getPaymentStatusText,
      resetFilter,
      payBill,
      confirmPayment,
      viewPaymentDetails,
      setupAutoDeduction,
      goBack
    }
  }
}
</script>

<style scoped>
.property-fee-container {
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
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: bold;
}

.filter-item select,
.filter-item input[type="date"] {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.bills-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.bill-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: transform 0.2s;
}

.bill-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.bill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.period-info h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.bill-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  margin-top: 5px;
}

.bill-status.unpaid {
  background-color: #ffebee;
  color: #c62828;
}

.bill-status.paid {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.bill-status.overdue {
  background-color: #fff3e0;
  color: #ef6c00;
}

.bill-status.partially_paid {
  background-color: #fff9c4;
  color: #f57f17;
}

.amount-info {
  text-align: right;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #c62828;
}

.bill-details {
  margin-bottom: 20px;
}

.bill-details h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
}

.fee-items {
  list-style: none;
  padding: 0;
  margin: 0 0 15px 0;
}

.fee-items li {
  padding: 5px 0;
  border-bottom: 1px dashed #eee;
}

.due-date {
  font-size: 14px;
  color: #666;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reminder-status {
  font-size: 14px;
  color: #666;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reminder-sent {
  color: #ef6c00;
  font-weight: bold;
}

.reminder-not-sent {
  color: #2e7d32;
  font-weight: bold;
}

.overdue-tag {
  color: #ef6c00;
  font-weight: bold;
}

.bill-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
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

.btn-pay {
  background-color: #c62828;
  color: white;
}

.btn-pay:hover {
  background-color: #b71c1c;
}

.btn-view {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-view:hover {
  background-color: #e0e0e0;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  max-width: 500px;
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

.header-buttons {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-buttons .btn-back {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
  font-weight: normal;
}

.header-buttons .btn-back:hover {
  background-color: #e0e0e0;
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
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.payment-method {
  margin-top: 20px;
}

.payment-method label {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

.method-options {
  display: flex;
  gap: 20px;
}

.method-options label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: normal;
  cursor: pointer;
}

.result-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 36px;
  margin: 0 auto 20px;
  font-weight: bold;
}

.result-icon.success {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.result-icon.failed {
  background-color: #ffebee;
  color: #c62828;
}

.modal-body h4 {
  text-align: center;
  margin: 0 0 10px 0;
  color: #333;
}

.modal-body p {
  text-align: center;
  color: #666;
  margin: 5px 0;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px 20px;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 8px;
}

/* 账单详情模态框样式 */
.detail-modal {
  max-width: 800px;
  width: 95%;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  text-align: left;
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.detail-row {
  display: flex;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.detail-label {
  font-weight: bold;
  min-width: 120px;
  color: #666;
}

.detail-value {
  flex: 1;
  color: #333;
}

.detail-value.amount {
  font-weight: bold;
  color: #c62828;
  font-size: 18px;
}

.fee-items-table,
.payments-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.fee-items-table th,
.payments-table th {
  background-color: #f5f5f5;
  padding: 10px;
  text-align: left;
  font-weight: bold;
  color: #666;
  border-bottom: 1px solid #eee;
}

.fee-items-table td,
.payments-table td {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.fee-items-table tr:hover,
.payments-table tr:hover {
  background-color: #f9f9f9;
}

.transaction-id {
  font-family: monospace;
  font-size: 14px;
  color: #666;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 20px;
}

.payment-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.payment-status.success {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.payment-status.failed {
  background-color: #ffebee;
  color: #c62828;
}

.payment-status.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.payment-status.refunded {
  background-color: #e3f2fd;
  color: #1565c0;
}

.overdue-tag {
  color: #ef6c00;
  font-weight: bold;
  margin-left: 10px;
}
</style>
