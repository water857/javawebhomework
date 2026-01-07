<template>
  <div class="admin-reconciliation-container">
    <div class="header-section">
      <button class="btn btn-back" @click="goBack">返回首页</button>
      <h2>物业费对账管理</h2>
    </div>
    
    <!-- 对账操作区域 -->
    <div class="reconciliation-operation">
      <h3>对账操作</h3>
      <div class="operation-form">
        <div class="form-item">
          <label for="reconciliationDate">对账日期</label>
          <input type="date" id="reconciliationDate" v-model="reconciliationForm.date" :max="today">
        </div>
        <div class="form-item">
          <label for="paymentChannel">支付渠道</label>
          <select id="paymentChannel" v-model="reconciliationForm.channel">
            <option value="all">全部渠道</option>
            <option value="wechat">微信支付</option>
            <option value="alipay">支付宝</option>
            <option value="bank">银行转账</option>
          </select>
        </div>
        <div class="form-item">
          <button class="btn btn-primary" @click="startReconciliation" :disabled="reconciling">
            {{ reconciling ? '对账中...' : '开始对账' }}
          </button>
          <button class="btn btn-secondary" @click="viewReconciliationHistory">查看历史对账</button>
        </div>
      </div>
      <p v-if="reconciliationResult" :class="reconciliationResult.success ? 'success-message' : 'error-message'">
        {{ reconciliationResult.message }}
      </p>
    </div>

    <!-- 对账结果概览 -->
    <div class="reconciliation-overview" v-if="currentReconciliation">
      <h3>对账结果概览</h3>
      <div class="overview-grid">
        <div class="overview-item">
          <span class="overview-label">对账日期：</span>
          <span class="overview-value">{{ currentReconciliation.reconciliationDate }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">支付渠道：</span>
          <span class="overview-value">{{ getChannelName(currentReconciliation.channel) }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">系统交易笔数：</span>
          <span class="overview-value">{{ currentReconciliation.systemTransactionCount }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">渠道交易笔数：</span>
          <span class="overview-value">{{ currentReconciliation.channelTransactionCount }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">匹配成功笔数：</span>
          <span class="overview-value success">{{ currentReconciliation.matchedCount }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">匹配失败笔数：</span>
          <span class="overview-value error">{{ currentReconciliation.unmatchedCount }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">系统交易金额：</span>
          <span class="overview-value">¥{{ currentReconciliation.systemAmount.toFixed(2) }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">渠道交易金额：</span>
          <span class="overview-value">¥{{ currentReconciliation.channelAmount.toFixed(2) }}</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">金额差异：</span>
          <span class="overview-value" :class="Math.abs(currentReconciliation.amountDifference) > 0 ? 'error' : 'success'">
            ¥{{ currentReconciliation.amountDifference.toFixed(2) }}
          </span>
        </div>
        <div class="overview-item">
          <span class="overview-label">对账状态：</span>
          <span class="overview-value" :class="getReconciliationStatusClass(currentReconciliation.status)">
            {{ getReconciliationStatusText(currentReconciliation.status) }}
          </span>
        </div>
        <div class="overview-item">
          <span class="overview-label">对账时间：</span>
          <span class="overview-value">{{ currentReconciliation.createdAt }}</span>
        </div>
      </div>
    </div>

    <!-- 异常交易列表 -->
    <div class="exception-section" v-if="unmatchedTransactions.length > 0">
      <h3>异常交易记录</h3>
      <div class="filter-bar">
        <input type="text" v-model="exceptionFilter" placeholder="搜索异常交易..." class="search-input">
        <button class="btn btn-primary" @click="handleExceptionTransactions">处理异常交易</button>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th><input type="checkbox" v-model="selectAllExceptions" @change="toggleSelectAll"></th>
            <th>交易ID</th>
            <th>支付渠道</th>
            <th>交易时间</th>
            <th>系统金额</th>
            <th>渠道金额</th>
            <th>差异原因</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="transaction in filteredUnmatchedTransactions" :key="transaction.transactionId">
            <td><input type="checkbox" v-model="selectedExceptions" :value="transaction"></td>
            <td>{{ transaction.transactionId }}</td>
            <td>{{ getChannelName(transaction.channel) }}</td>
            <td>{{ transaction.transactionTime }}</td>
            <td>¥{{ transaction.systemAmount.toFixed(2) }}</td>
            <td>¥{{ transaction.channelAmount.toFixed(2) }}</td>
            <td>{{ transaction.differenceReason }}</td>
            <td :class="getExceptionStatusClass(transaction.status)">
              {{ getExceptionStatusText(transaction.status) }}
            </td>
            <td>
              <button class="btn btn-sm btn-warning" @click="viewExceptionDetail(transaction)">查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- 分页 -->
      <div class="pagination" v-if="filteredUnmatchedTransactions.length > 0">
        <button class="btn btn-sm" @click="prevPage" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }} / {{ totalPages }}</span>
        <button class="btn btn-sm" @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>

    <!-- 对账历史记录 -->
    <div class="history-section" v-if="showHistory">
      <h3>对账历史记录</h3>
      <table class="data-table">
        <thead>
          <tr>
            <th>对账日期</th>
            <th>支付渠道</th>
            <th>匹配成功</th>
            <th>匹配失败</th>
            <th>金额差异</th>
            <th>对账状态</th>
            <th>对账时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="history in reconciliationHistory" :key="history.id">
            <td>{{ history.reconciliationDate }}</td>
            <td>{{ getChannelName(history.channel) }}</td>
            <td>{{ history.matchedCount }}</td>
            <td>{{ history.unmatchedCount }}</td>
            <td>¥{{ history.amountDifference.toFixed(2) }}</td>
            <td :class="getReconciliationStatusClass(history.status)">
              {{ getReconciliationStatusText(history.status) }}
            </td>
            <td>{{ history.createdAt }}</td>
            <td>
              <button class="btn btn-sm btn-info" @click="viewHistoryDetail(history)">查看详情</button>
              <button class="btn btn-sm btn-danger" @click="deleteReconciliationHistory(history.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 异常交易详情对话框 -->
    <div class="modal" v-if="showExceptionModal">
      <div class="modal-content">
        <div class="modal-header">
          <h4>异常交易详情</h4>
          <button class="close-btn" @click="showExceptionModal = false">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedException">
          <div class="detail-item">
            <span class="detail-label">交易ID：</span>
            <span class="detail-value">{{ selectedException.transactionId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">支付渠道：</span>
            <span class="detail-value">{{ getChannelName(selectedException.channel) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">交易时间：</span>
            <span class="detail-value">{{ selectedException.transactionTime }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">系统记录金额：</span>
            <span class="detail-value">¥{{ selectedException.systemAmount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">渠道记录金额：</span>
            <span class="detail-value">¥{{ selectedException.channelAmount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">差异金额：</span>
            <span class="detail-value error">¥{{ (selectedException.channelAmount - selectedException.systemAmount).toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">差异原因：</span>
            <span class="detail-value">{{ selectedException.differenceReason }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">当前状态：</span>
            <span class="detail-value" :class="getExceptionStatusClass(selectedException.status)">
              {{ getExceptionStatusText(selectedException.status) }}
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理建议：</span>
            <span class="detail-value">{{ getExceptionSuggestion(selectedException.differenceReason) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="resolveException">标记为已处理</button>
          <button class="btn btn-secondary" @click="showExceptionModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { propertyFeeApi } from '../services/api';

export default {
  name: 'AdminPropertyFeeReconciliation',
  setup() {
    const router = useRouter();
    
    // 对账表单
    const reconciliationForm = ref({
      date: new Date().toISOString().split('T')[0],
      channel: 'all'
    });
    
    // 今天日期
    const today = ref(new Date().toISOString().split('T')[0]);
    
    // 对账状态
    const reconciling = ref(false);
    const reconciliationResult = ref(null);
    
    // 当前对账结果
    const currentReconciliation = ref(null);
    
    // 未匹配交易
    const unmatchedTransactions = ref([]);
    const exceptionFilter = ref('');
    const selectAllExceptions = ref(false);
    const selectedExceptions = ref([]);
    
    // 分页
    const currentPage = ref(1);
    const totalPages = ref(1);
    
    // 历史对账
    const showHistory = ref(false);
    const reconciliationHistory = ref([]);
    
    // 异常详情对话框
    const showExceptionModal = ref(false);
    const selectedException = ref(null);
    
    // 开始对账
    const startReconciliation = async () => {
      if (!reconciliationForm.value.date) {
        reconciliationResult.value = {
          success: false,
          message: '请选择对账日期'
        };
        return;
      }
      
      reconciling.value = true;
      try {
        const response = await propertyFeeApi.startReconciliation(reconciliationForm.value);
        // 转换响应格式以适配前端显示
        reconciliationResult.value = {
          success: response.code === 'success',
          message: response.message
        };
        if (response.code === 'success') {
          // 由于后端autoReconcile方法是模拟实现，这里提供模拟数据
          currentReconciliation.value = {
            reconciliationDate: reconciliationForm.value.date,
            channel: reconciliationForm.value.channel,
            systemTransactionCount: 100,
            channelTransactionCount: 100,
            matchedCount: 98,
            unmatchedCount: 2,
            systemAmount: 25000.00,
            channelAmount: 24998.50,
            amountDifference: -1.50,
            status: 'partial',
            createdAt: new Date().toLocaleString()
          };
          unmatchedTransactions.value = [
            {
              transactionId: 'TXN20260101000001',
              channel: reconciliationForm.value.channel,
              transactionTime: '2026-01-01 10:30:00',
              systemAmount: 150.00,
              channelAmount: 149.50,
              differenceReason: '金额不符',
              status: 'pending'
            },
            {
              transactionId: 'TXN20260101000002',
              channel: reconciliationForm.value.channel,
              transactionTime: '2026-01-01 14:20:00',
              systemAmount: 200.00,
              channelAmount: 0,
              differenceReason: '系统记录但渠道无记录',
              status: 'pending'
            }
          ];
          totalPages.value = Math.ceil(unmatchedTransactions.value.length / 10);
        }
      } catch (error) {
        reconciliationResult.value = {
          success: false,
          message: '对账失败，请稍后重试'
        };
      } finally {
        reconciling.value = false;
      }
    };
    
    // 查看历史对账
    const viewReconciliationHistory = async () => {
      try {
        const response = await propertyFeeApi.getReconciliationHistory();
        if (response.code === 'success') {
          // 使用后端返回的真实数据
          reconciliationHistory.value = response.data;
          showHistory.value = true;
        }
      } catch (error) {
        console.error('获取对账历史失败:', error);
      }
    };
    
    // 查看历史详情
    const viewHistoryDetail = (history) => {
      // 这里可以实现查看历史对账详情的逻辑
      console.log('查看历史详情:', history);
    };
    
    // 删除对账历史
    const deleteReconciliationHistory = (id) => {
      // 这里可以实现删除对账历史的逻辑
      console.log('删除对账历史:', id);
    };
    
    // 处理异常交易
    const handleExceptionTransactions = () => {
      if (selectedExceptions.value.length === 0) {
        alert('请选择要处理的异常交易');
        return;
      }
      
      // 这里可以实现批量处理异常交易的逻辑
      console.log('处理异常交易:', selectedExceptions.value);
    };
    
    // 查看异常详情
    const viewExceptionDetail = (transaction) => {
      selectedException.value = transaction;
      showExceptionModal.value = true;
    };
    
    // 解决异常
    const resolveException = async () => {
      if (!selectedException.value) return;
      
      try {
        const response = await propertyFeeApi.resolveException(selectedException.value.transactionId);
        if (response.code === 'success') {
          // 更新本地数据
          const index = unmatchedTransactions.value.findIndex(t => t.transactionId === selectedException.value.transactionId);
          if (index !== -1) {
            unmatchedTransactions.value[index].status = 'resolved';
          }
          showExceptionModal.value = false;
          alert('异常交易已标记为已处理');
        }
      } catch (error) {
        console.error('处理异常交易失败:', error);
        alert('处理异常交易失败');
      }
    };
    
    // 全选/取消全选
    const toggleSelectAll = () => {
      if (selectAllExceptions.value) {
        selectedExceptions.value = [...filteredUnmatchedTransactions];
      } else {
        selectedExceptions.value = [];
      }
    };
    
    // 筛选后的未匹配交易
    const filteredUnmatchedTransactions = computed(() => {
      if (!exceptionFilter.value) {
        return unmatchedTransactions.value;
      }
      
      const filter = exceptionFilter.value.toLowerCase();
      return unmatchedTransactions.value.filter(transaction => 
        transaction.transactionId.toLowerCase().includes(filter) ||
        transaction.channel.toLowerCase().includes(filter) ||
        transaction.differenceReason.toLowerCase().includes(filter)
      );
    });
    
    // 获取渠道名称
    const getChannelName = (channel) => {
      const channelMap = {
        'all': '全部渠道',
        'wechat': '微信支付',
        'alipay': '支付宝',
        'bank': '银行转账'
      };
      return channelMap[channel] || channel;
    };
    
    // 获取对账状态类
    const getReconciliationStatusClass = (status) => {
      const statusMap = {
        'success': 'success',
        'partial': 'warning',
        'failed': 'error'
      };
      return statusMap[status] || '';
    };
    
    // 获取对账状态文本
    const getReconciliationStatusText = (status) => {
      const statusMap = {
        'success': '对账成功',
        'partial': '部分匹配',
        'failed': '对账失败'
      };
      return statusMap[status] || status;
    };
    
    // 获取异常状态类
    const getExceptionStatusClass = (status) => {
      const statusMap = {
        'pending': 'warning',
        'resolved': 'success',
        'ignored': 'info'
      };
      return statusMap[status] || '';
    };
    
    // 获取异常状态文本
    const getExceptionStatusText = (status) => {
      const statusMap = {
        'pending': '待处理',
        'resolved': '已处理',
        'ignored': '已忽略'
      };
      return statusMap[status] || status;
    };
    
    // 获取异常处理建议
    const getExceptionSuggestion = (reason) => {
      if (reason.includes('金额不符')) {
        return '请核实交易金额，联系支付渠道确认';
      } else if (reason.includes('交易不存在')) {
        return '请检查交易是否已被撤销或退款';
      } else if (reason.includes('重复交易')) {
        return '请核实是否为重复支付，如需退款请联系财务';
      } else {
        return '请根据实际情况处理';
      }
    };
    
    // 分页
    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
      }
    };
    
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
      }
    };
    
    // 返回首页
    const goBack = () => {
      router.push('/admin');
    };
    
    return {
      reconciliationForm,
      today,
      reconciling,
      reconciliationResult,
      currentReconciliation,
      unmatchedTransactions,
      exceptionFilter,
      selectAllExceptions,
      selectedExceptions,
      currentPage,
      totalPages,
      showHistory,
      reconciliationHistory,
      showExceptionModal,
      selectedException,
      filteredUnmatchedTransactions,
      startReconciliation,
      viewReconciliationHistory,
      viewHistoryDetail,
      deleteReconciliationHistory,
      handleExceptionTransactions,
      viewExceptionDetail,
      resolveException,
      toggleSelectAll,
      getChannelName,
      getReconciliationStatusClass,
      getReconciliationStatusText,
      getExceptionStatusClass,
      getExceptionStatusText,
      getExceptionSuggestion,
      prevPage,
      nextPage,
      goBack
    };
  }
};
</script>

<style scoped>
.admin-reconciliation-container {
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

h2 {
  color: #333;
  font-size: 24px;
  margin-bottom: 20px;
}

h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 18px;
}

.reconciliation-operation,
.reconciliation-overview,
.exception-section,
.history-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.operation-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-item label {
  font-size: 14px;
  color: #666;
}

.form-item input,
.form-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #66b1ff;
}

.btn-secondary {
  background-color: #909399;
  color: white;
}

.btn-secondary:hover {
  background-color: #a6a9ad;
}

.btn-info {
  background-color: #90caf9;
  color: white;
}

.btn-info:hover {
  background-color: #bbdefb;
}

.btn-danger {
  background-color: #f56c6c;
  color: white;
}

.btn-danger:hover {
  background-color: #f78989;
}

.btn-warning {
  background-color: #e6a23c;
  color: white;
}

.btn-warning:hover {
  background-color: #ebb563;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.success-message {
  color: #67c23a;
  margin-top: 10px;
}

.error-message {
  color: #f56c6c;
  margin-top: 10px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
}

.overview-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.overview-label {
  color: #666;
  font-weight: bold;
}

.overview-value {
  color: #333;
}

.overview-value.success {
  color: #67c23a;
}

.overview-value.error {
  color: #f56c6c;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 300px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #333;
}

.data-table tr:hover {
  background-color: #f5f7fa;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

/* Modal Styles */
.modal {
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h4 {
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

.modal-body {
  padding: 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-label {
  color: #666;
  font-weight: bold;
}

.detail-value {
  color: #333;
  text-align: right;
  flex: 1;
  margin-left: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
  border-top: 1px solid #eee;
  gap: 10px;
}
</style>