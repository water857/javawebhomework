<template>
  <div class="admin-property-fee-management">
    <div class="header-section">
      <button class="btn btn-back" @click="goBack">返回首页</button>
      <h2>物业费账单管理</h2>
    </div>
    
    <!-- 功能操作区 -->
    <div class="action-section">
      <button class="btn btn-primary" @click="showGenerateModal = true">
        <i class="icon">+</i> 批量生成账单
      </button>
      <button class="btn btn-secondary" @click="refreshBills">
        <i class="icon">🔄</i> 刷新数据
      </button>
      <button class="btn btn-success" @click="exportBills">
        <i class="icon">📤</i> 导出账单
      </button>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-item">
        <label for="bill-status">账单状态：</label>
        <select id="bill-status" v-model="filter.status">
          <option value="">全部</option>
          <option value="unpaid">未支付</option>
          <option value="paid">已支付</option>
          <option value="overdue">逾期</option>
          <option value="partially_paid">部分支付</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label for="start-period">开始周期：</label>
        <input type="date" id="start-period" v-model="filter.startDate">
      </div>
      
      <div class="filter-item">
        <label for="end-period">结束周期：</label>
        <input type="date" id="end-period" v-model="filter.endDate">
      </div>
      
      <div class="filter-item">
        <button class="btn btn-primary" @click="getBills">查询</button>
        <button class="btn btn-secondary" @click="resetFilter">重置</button>
      </div>
    </div>
    
    <!-- 账单列表 -->
    <div class="bills-section">
      <table class="bills-table">
        <thead>
          <tr>
            <th>账单ID</th>
            <th>居民ID</th>
            <th>费用周期</th>
            <th>总金额</th>
            <th>状态</th>
            <th>截止日期</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="bill in bills" :key="bill.id">
            <td>{{ bill.id }}</td>
            <td>{{ bill.userId }}</td>
            <td>{{ formatDate(bill.periodStart) }} - {{ formatDate(bill.periodEnd) }}</td>
            <td>¥{{ bill.amount.toFixed(2) }}</td>
            <td>
              <span class="bill-status" :class="bill.status">
                {{ getStatusText(bill.status) }}
              </span>
            </td>
            <td>{{ formatDate(bill.dueDate) }}</td>
            <td>{{ formatDateTime(bill.createdAt) }}</td>
            <td class="action-buttons">
              <button class="btn btn-view" @click="viewBillDetails(bill)">
                详情
              </button>
              <button class="btn btn-edit" @click="editBill(bill)">
                修改
              </button>
              <button class="btn btn-danger" @click="deleteBill(bill.id)">
                删除
              </button>
              <button 
                class="btn btn-remind" 
                @click="sendReminder(bill)"
                :disabled="bill.paymentDeadlineReminderSent || false"
                title="发送催缴提醒"
              >
                催缴
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 空状态 -->
      <div v-if="bills.length === 0" class="empty-state">
        <p>暂无账单数据</p>
      </div>
    </div>
    
    <!-- 批量生成账单模态框 -->
    <div class="modal" v-if="showGenerateModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>批量生成账单</h3>
          <button class="close-btn" @click="showGenerateModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 费用数据导入 -->
          <div class="form-group">
            <label>费用数据导入：</label>
            <div class="import-section">
              <input 
                type="file" 
                id="fee-import" 
                accept=".csv,.xlsx" 
                @change="handleFileImport"
                ref="fileInput"
              >
              <button class="btn btn-secondary" @click="$refs.fileInput?.click()">
                <i class="icon">📁</i> 选择文件导入
              </button>
              <p class="help-text">支持CSV和Excel格式文件导入费用数据</p>
            </div>
          </div>
          
          <div class="form-group">
            <label for="period-start">费用周期开始日期：</label>
            <input type="date" id="period-start" v-model="generateForm.periodStart" required>
          </div>
          
          <div class="form-group">
            <label for="period-end">费用周期结束日期：</label>
            <input type="date" id="period-end" v-model="generateForm.periodEnd" required>
          </div>
          
          <div class="form-group">
            <label for="due-date">缴费截止日期：</label>
            <input type="date" id="due-date" v-model="generateForm.dueDate" required>
          </div>
          
          <div class="form-group">
            <label for="fee-items">费用项目：</label>
            <div class="fee-items-container">
              <div class="fee-item-row" v-for="(item, index) in generateForm.feeItems" :key="index">
                <input 
                  type="text" 
                  v-model="item.name" 
                  placeholder="费用名称（如：物业费）"
                  required
                >
                <input 
                  type="number" 
                  v-model="item.amount" 
                  placeholder="金额"
                  step="0.01"
                  required
                >
                <button 
                  class="btn btn-remove" 
                  @click="removeFeeItem(index)"
                  :disabled="generateForm.feeItems.length <= 1"
                >
                  - 移除
                </button>
              </div>
              <button class="btn btn-add" @click="addFeeItem">
                + 添加费用项目
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label for="bill-type">生成范围：</label>
            <select id="bill-type" v-model="generateForm.billType">
              <option value="all">所有居民</option>
              <option value="selected">选择居民</option>
            </select>
          </div>
          
          <div class="form-group" v-if="generateForm.billType === 'selected'">
            <label for="selected-users">选择居民：</label>
            <select id="selected-users" multiple v-model="generateForm.selectedUsers">
              <option v-for="user in residents" :key="user.id" :value="user.id">
                {{ user.id }} - {{ user.realName }} ({{ user.address }})
              </option>
            </select>
          </div>
          
          <!-- 预览生成结果 -->
          <div class="preview-section">
            <h4>预览生成结果：</h4>
            <div class="preview-item">
              <span>预计生成账单数量：</span>
              <span class="preview-value">{{ getPreviewCount() }} 份</span>
            </div>
            <div class="preview-item">
              <span>费用项目总计：</span>
              <span class="preview-value">{{ getTotalAmountPerBill() }} 项</span>
            </div>
            <div class="preview-item">
              <span>每份账单总金额：</span>
              <span class="preview-value">¥{{ calculateTotalAmount() }}</span>
            </div>
            
            <!-- 生成预览按钮 -->
            <button class="btn btn-secondary" @click="previewGeneration" style="margin-top: 10px;">
              <i class="icon">👁️</i> 生成详细预览
            </button>
          </div>
          
          <!-- 详细预览表格 -->
          <div v-if="showPreviewTable" class="detailed-preview-section">
            <h4>详细预览：</h4>
            <div class="preview-table-container">
              <table class="preview-table">
                <thead>
                  <tr>
                    <th>居民姓名</th>
                    <th>地址</th>
                    <th>费用周期</th>
                    <th>截止日期</th>
                    <th>总金额</th>
                    <th>费用明细</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in previewData" :key="index">
                    <td>{{ item.userName }}</td>
                    <td>{{ item.userAddress }}</td>
                    <td>{{ formatDate(item.periodStart) }} - {{ formatDate(item.periodEnd) }}</td>
                    <td>{{ formatDate(item.dueDate) }}</td>
                    <td>¥{{ item.totalAmount.toFixed(2) }}</td>
                    <td>
                      <ul class="fee-items-list">
                        <li v-for="(feeItem, feeIndex) in item.feeItems" :key="feeIndex">
                          {{ feeItem.name }}: ¥{{ feeItem.amount.toFixed(2) }}
                        </li>
                      </ul>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showGenerateModal = false">取消</button>
          <button class="btn btn-primary" @click="generateBills" :disabled="isGenerating">
            {{ isGenerating ? '生成中...' : '确认生成' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 账单详情模态框 -->
    <div class="modal" v-if="showDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <div class="header-buttons">
            <button class="btn btn-back" @click="showDetailModal = false">
              返回
            </button>
            <h3>账单详情</h3>
          </div>
          <button class="close-btn" @click="showDetailModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 居民信息部分 -->
          <div class="detail-section resident-info-section">
            <h4>居民信息：</h4>
            <div class="resident-info-grid">
              <div class="resident-info-item">
                <div class="detail-label">用户名：</div>
                <div class="detail-value">{{ selectedBill.resident?.username || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">姓名：</div>
                <div class="detail-value">{{ selectedBill.resident?.realName || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">手机号：</div>
                <div class="detail-value">{{ selectedBill.resident?.phone || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">邮箱：</div>
                <div class="detail-value">{{ selectedBill.resident?.email || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">地址：</div>
                <div class="detail-value">{{ selectedBill.resident?.address || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">身份证号：</div>
                <div class="detail-value">{{ selectedBill.resident?.idCard || '未知' }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">注册时间：</div>
                <div class="detail-value">{{ formatDateTime(selectedBill.resident?.createdAt) }}</div>
              </div>
              <div class="resident-info-item">
                <div class="detail-label">用户状态：</div>
                <div class="detail-value">{{ selectedBill.resident?.status === 1 ? '启用' : '禁用' }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-row">
            <div class="detail-label">账单ID：</div>
            <div class="detail-value">{{ selectedBill.id }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">居民ID：</div>
            <div class="detail-value">{{ selectedBill.userId }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">费用周期：</div>
            <div class="detail-value">
              {{ formatDate(selectedBill.periodStart) }} 至 {{ formatDate(selectedBill.periodEnd) }}
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-label">总金额：</div>
            <div class="detail-value amount">¥{{ selectedBill.amount.toFixed(2) }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">账单状态：</div>
            <div class="detail-value">
              <span class="bill-status" :class="selectedBill.status">
                {{ getStatusText(selectedBill.status) }}
              </span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-label">缴费截止日期：</div>
            <div class="detail-value">
              {{ formatDate(selectedBill.dueDate) }}
              <span v-if="selectedBill.status === 'overdue'" class="overdue-tag">已逾期</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-label">催缴提醒：</div>
            <div class="detail-value">
              {{ selectedBill.paymentDeadlineReminderSent || false ? '已发送' : '未发送' }}
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
                <tr v-for="(item, index) in (selectedBill.items ? JSON.parse(selectedBill.items) : [{ name: '物业费', amount: selectedBill.amount }])" :key="index">
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
            <tr v-for="payment in (selectedBill.payments || [])" :key="payment.id">
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
            <tr v-if="(selectedBill.payments || []).length === 0">
              <td colspan="6" class="no-data">暂无支付记录</td>
            </tr>
          </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
    
    <!-- 账单编辑模态框 -->
    <div class="modal" v-if="showEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>修改账单</h3>
          <button class="close-btn" @click="showEditModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="edit-status">账单状态：</label>
            <select id="edit-status" v-model="editForm.status">
              <option value="unpaid">未支付</option>
              <option value="paid">已支付</option>
              <option value="overdue">逾期</option>
              <option value="partially_paid">部分支付</option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="edit-due-date">缴费截止日期：</label>
            <input 
              type="date" 
              id="edit-due-date" 
              v-model="editForm.dueDate" 
              required
            >
          </div>
          
          <div class="form-group">
            <label for="edit-reminder">催缴提醒：</label>
            <input 
              type="checkbox" 
              id="edit-reminder" 
              v-model="editForm.paymentDeadlineReminderSent"
            >
            <span>已发送催缴提醒</span>
          </div>
          
          <!-- 费用项目编辑 -->
          <div class="form-group">
            <label for="edit-fee-items">费用项目：</label>
            <div class="fee-items-container">
              <div class="fee-item-row" v-for="(item, index) in editForm.feeItems" :key="index">
                <input 
                  type="text" 
                  v-model="item.name" 
                  placeholder="费用名称（如：物业费）"
                  required
                >
                <input 
                  type="number" 
                  v-model="item.amount" 
                  placeholder="金额"
                  step="0.01"
                  required
                >
                <button 
                  class="btn btn-remove" 
                  @click="removeEditFeeItem(index)"
                  :disabled="editForm.feeItems.length <= 1"
                >
                  - 移除
                </button>
              </div>
              <button class="btn btn-add" @click="addEditFeeItem">
                + 添加费用项目
              </button>
            </div>
          </div>
          
          <!-- 自动计算总金额 -->
          <div class="form-group" style="margin-top: 10px;">
            <label for="auto-amount">总金额（自动计算）：</label>
            <input 
              type="number" 
              id="auto-amount" 
              :value="calculateEditTotalAmount()" 
              step="0.01"
              readonly
              style="background-color: #f5f5f5;"
            >
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
          <button class="btn btn-primary" @click="saveEdit">保存修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { propertyFeeApi, apiRequest } from '../services/api'

export default {
  name: 'AdminPropertyFeeManagement',
  setup() {
    const router = useRouter()
    
    const bills = ref([])
    const residents = ref([])
    const filter = ref({
      status: '',
      startDate: '',
      endDate: ''
    })
    
    // 批量生成账单相关
    const showGenerateModal = ref(false)
    const isGenerating = ref(false)
    const fileInput = ref(null)
    const generateForm = ref({
      periodStart: '',
      periodEnd: '',
      dueDate: '',
      feeItems: [
        { name: '物业费', amount: 100.00 },
        { name: '水费', amount: 50.00 },
        { name: '电费', amount: 80.00 },
        { name: '垃圾处理费', amount: 15.00 }
      ],
      billType: 'all',
      selectedUsers: [],
      // 导入的数据将存储在这里，用于预览和生成
      importedData: null
    })
    // 预览数据
    const previewData = ref([])
    const showPreviewTable = ref(false)
    
    // 账单详情相关
    const showDetailModal = ref(false)
    const selectedBill = ref(null)
    
    // 账单编辑相关
    const showEditModal = ref(false)
    const editForm = ref({})
    
    // 获取所有居民信息
    const getResidents = async () => {
      try {
        // 调用用户管理API获取居民列表
        const response = await apiRequest('/residents')
        residents.value = response.data || []
      } catch (error) {
        console.error('获取居民信息失败:', error)
        alert('获取居民信息失败，请稍后重试')
      }
    }
    
    // 获取账单列表
    const getBills = async () => {
      try {
        const response = await propertyFeeApi.getAllBills({
          status: filter.value.status,
          startDate: filter.value.startDate,
          endDate: filter.value.endDate
        })
        bills.value = response.data || []
        
        // 为每个账单加载支付记录
        for (const bill of bills.value) {
          await loadPaymentsForBill(bill)
        }
      } catch (error) {
        console.error('获取账单列表失败:', error)
        alert('获取账单列表失败，请稍后重试')
      }
    }
    
    // 为账单加载支付记录
    const loadPaymentsForBill = async (bill) => {
      try {
        const response = await propertyFeeApi.getPaymentHistory(1, 10, {
          billId: bill.id
        })
        bill.payments = response.data || []
      } catch (error) {
        console.error(`获取账单 ${bill.id} 的支付记录失败:`, error)
        bill.payments = []
      }
    }
    
    // 刷新账单列表
    const refreshBills = () => {
      getBills()
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
    
    // 导出账单
    const exportBills = () => {
      alert('导出功能开发中...')
    }
    
    // 文件导入处理
    const handleFileImport = async (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      try {
        // 这里可以根据实际需求实现文件解析逻辑
        // 目前模拟导入成功
        alert('文件导入成功！（模拟实现）')
        
        // 模拟导入的数据
        const mockImportedData = {
          feeItems: [
            { name: '物业费', amount: 120.00 },
            { name: '水费', amount: 60.00 },
            { name: '电费', amount: 90.00 },
            { name: '垃圾处理费', amount: 20.00 }
          ]
        }
        
        generateForm.value.feeItems = mockImportedData.feeItems
        generateForm.value.importedData = mockImportedData
        
        // 自动生成预览数据
        await generatePreviewData()
      } catch (error) {
        console.error('文件导入失败:', error)
        alert('文件导入失败：' + error.message)
      }
    }
    
    // 在组件挂载时调用
    onMounted(() => {
      // 获取居民信息
      getResidents()
      // 获取账单列表
      getBills()
    })
    
    // 生成预览数据
    const generatePreviewData = async () => {
      try {
        const usersToGenerateFor = generateForm.value.billType === 'all' 
          ? residents.value.map(user => user)
          : residents.value.filter(user => generateForm.value.selectedUsers.includes(user.id))
        
        const totalAmount = parseFloat(calculateTotalAmount())
        
        // 生成预览数据
        previewData.value = usersToGenerateFor.map(user => ({
          userId: user.id,
          userName: user.realName,
          userAddress: user.address,
          periodStart: generateForm.value.periodStart,
          periodEnd: generateForm.value.periodEnd,
          dueDate: generateForm.value.dueDate,
          totalAmount: totalAmount,
          feeItems: generateForm.value.feeItems
        }))
        
        showPreviewTable.value = true
      } catch (error) {
        console.error('生成预览数据失败:', error)
        alert('生成预览数据失败：' + error.message)
      }
    }
    
    // 预览生成结果
    const previewGeneration = async () => {
      await generatePreviewData()
    }
    
    // 添加费用项目
    const addFeeItem = () => {
      generateForm.value.feeItems.push({
        name: '',
        amount: 0.00
      })
    }
    
    // 移除费用项目
    const removeFeeItem = (index) => {
      if (generateForm.value.feeItems.length > 1) {
        generateForm.value.feeItems.splice(index, 1)
      }
    }
    
    // 获取预览账单数量
    const getPreviewCount = () => {
      if (generateForm.value.billType === 'all') {
        return residents.value.length
      } else {
        return generateForm.value.selectedUsers.length
      }
    }
    
    // 获取每份账单的费用项目总数
    const getTotalAmountPerBill = () => {
      return generateForm.value.feeItems.length
    }
    
    // 计算每份账单的总金额
    const calculateTotalAmount = () => {
      return generateForm.value.feeItems.reduce((total, item) => {
        return total + (item.amount || 0)
      }, 0).toFixed(2)
    }
    
    // 批量生成账单
    const generateBills = async () => {
      isGenerating.value = true
      try {
        // 准备账单数据
        const billsToGenerate = []
        const usersToGenerateFor = generateForm.value.billType === 'all' 
          ? residents.value.map(user => user.id)
          : generateForm.value.selectedUsers
        
        // 计算总金额
        const totalAmount = parseFloat(calculateTotalAmount())
        
        // 为每个用户生成账单
        for (const userId of usersToGenerateFor) {
          billsToGenerate.push({
            userId: userId,
            periodStart: generateForm.value.periodStart,
            periodEnd: generateForm.value.periodEnd,
            amount: totalAmount,
            items: JSON.stringify(generateForm.value.feeItems),
            dueDate: generateForm.value.dueDate,
            status: 'unpaid',
            paymentDeadlineReminderSent: false
          })
        }
        
        // 调用API批量生成账单
        const response = await propertyFeeApi.batchGenerateBills({
          bills: billsToGenerate
        })
        
        alert(`成功生成 ${response.data.count} 份账单！`)
        showGenerateModal.value = false
        getBills()
      } catch (error) {
        console.error('批量生成账单失败:', error)
        alert('批量生成账单失败：' + (error.response?.data?.message || error.message))
      } finally {
        isGenerating.value = false
      }
    }
    
    // 查看账单详情
    const viewBillDetails = async (bill) => {
      try {
        const response = await propertyFeeApi.getBillDetail(bill.id)
        selectedBill.value = response.data.bill || bill
        selectedBill.value.resident = response.data.resident || null
        showDetailModal.value = true
      } catch (error) {
        console.error('获取账单详情失败:', error)
        selectedBill.value = bill
        selectedBill.value.resident = null
        showDetailModal.value = true
      }
    }
    
    // 编辑账单
    const editBill = (bill) => {
      editForm.value = {
        id: bill.id,
        userId: bill.userId, // 添加用户ID
        amount: bill.amount,
        status: bill.status,
        periodStart: new Date(bill.periodStart).toISOString().split('T')[0], // 添加费用周期开始日期
        periodEnd: new Date(bill.periodEnd).toISOString().split('T')[0], // 添加费用周期结束日期
        dueDate: new Date(bill.dueDate).toISOString().split('T')[0],
        paymentDeadlineReminderSent: bill.paymentDeadlineReminderSent || false, // 处理undefined提醒状态
        feeItems: bill.items ? JSON.parse(bill.items) : [{ name: '物业费', amount: bill.amount }] // 处理undefined items
      }
      showEditModal.value = true
    }
    
    // 编辑账单时添加费用项目
    const addEditFeeItem = () => {
      editForm.value.feeItems.push({
        name: '',
        amount: 0.00
      })
    }
    
    // 编辑账单时移除费用项目
    const removeEditFeeItem = (index) => {
      if (editForm.value.feeItems.length > 1) {
        editForm.value.feeItems.splice(index, 1)
      }
    }
    
    // 计算编辑账单时的总金额
    const calculateEditTotalAmount = () => {
      if (!editForm.value.feeItems) return 0
      return editForm.value.feeItems.reduce((total, item) => {
        return total + (item.amount || 0)
      }, 0).toFixed(2)
    }
    
    // 保存编辑
    const saveEdit = async () => {
      try {
        // 更新总金额为自动计算的值
        editForm.value.amount = parseFloat(calculateEditTotalAmount())
        
        // 准备更新数据，包含所有必填字段
        const updateData = {
          ...editForm.value,
          items: JSON.stringify(editForm.value.feeItems),
          // 确保必填字段都有值
          periodStart: editForm.value.periodStart,
          periodEnd: editForm.value.periodEnd
        }
        
        // 调用API更新账单
        const response = await propertyFeeApi.updateBill(editForm.value.id, updateData)
        
        // 更新本地数据
        const index = bills.value.findIndex(bill => bill.id === editForm.value.id)
        if (index !== -1) {
          bills.value[index] = {
            ...bills.value[index],
            ...editForm.value,
            amount: parseFloat(calculateEditTotalAmount()),
            items: JSON.stringify(editForm.value.feeItems)
          }
        }
        
        alert('账单修改成功！')
        showEditModal.value = false
      } catch (error) {
        console.error('修改账单失败:', error)
        alert('修改账单失败：' + (error.response?.data?.message || error.message))
      }
    }
    
    // 删除账单
    const deleteBill = async (billId) => {
      if (!confirm('确定要删除该账单吗？此操作不可恢复！')) {
        return
      }
      
      try {
        // 调用API删除账单
        await propertyFeeApi.deleteBill(billId)
        
        // 删除成功后刷新数据
        alert('账单删除成功！')
        getBills()
      } catch (error) {
        console.error('删除账单失败:', error)
        alert('删除账单失败：' + (error.response?.data?.message || error.message))
      }
    }
    
    // 发送催缴提醒
    const sendReminder = async (bill) => {
      try {
        // 调用API发送催缴提醒
        await propertyFeeApi.sendReminder(bill.id)
        
        // 更新本地数据
        bill.paymentDeadlineReminderSent = true
        alert('催缴提醒发送成功！')
      } catch (error) {
        console.error('发送催缴提醒失败:', error)
        alert('发送催缴提醒失败：' + (error.response?.data?.message || error.message))
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
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
    
    // 返回首页
    const goBack = () => {
      router.push('/admin')
    }
    
    // 页面加载时初始化数据
    onMounted(async () => {
      await getResidents()
      await getBills()
    })
    
    return {
      bills,
      residents,
      filter,
      showGenerateModal,
      isGenerating,
      fileInput,
      generateForm,
      previewData,
      showPreviewTable,
      showDetailModal,
      selectedBill,
      showEditModal,
      editForm,
      getResidents,
      getBills,
      loadPaymentsForBill,
      refreshBills,
      resetFilter,
      exportBills,
      handleFileImport,
      generatePreviewData,
      previewGeneration,
      addFeeItem,
      removeFeeItem,
      addEditFeeItem,
      removeEditFeeItem,
      calculateEditTotalAmount,
      getPreviewCount,
      getTotalAmountPerBill,
      calculateTotalAmount,
      generateBills,
      viewBillDetails,
      editBill,
      saveEdit,
      deleteBill,
      sendReminder,
      formatDate,
      formatDateTime,
      getStatusText,
      getPaymentMethodText,
      getPaymentStatusText,
      goBack
    }
  }
}
</script>

<style scoped>
.admin-property-fee-management {
  max-width: 1400px;
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

.action-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  align-items: center;
}

.action-section .btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
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
  min-width: 150px;
}

.bills-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.bills-table {
  width: 100%;
  border-collapse: collapse;
}

.bills-table th,
.bills-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.bills-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.bills-table tr:hover {
  background-color: #f9f9f9;
}

.bill-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
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

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.action-buttons .btn {
  padding: 4px 10px;
  font-size: 12px;
}

.btn-primary {
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
}

.btn-primary:hover {
  background-color: #1565c0;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.btn-success {
  background-color: #2e7d32;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
}

.btn-success:hover {
  background-color: #276738;
}

.btn-danger {
  background-color: #c62828;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
}

.btn-danger:hover {
  background-color: #b71c1c;
}

.btn-view {
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
}

.btn-view:hover {
  background-color: #1976d2;
}

.btn-edit {
  background-color: #ffc107;
  color: #333;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
}

.btn-edit:hover {
  background-color: #ffb300;
}

.btn-remind {
  background-color: #795548;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
}

.btn-remind:hover {
  background-color: #6d4c41;
}

.btn-remind:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-add {
  background-color: #e8f5e8;
  color: #2e7d32;
  border: 1px dashed #4caf50;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  margin-top: 10px;
  display: inline-block;
}

.btn-add:hover {
  background-color: #c8e6c9;
}

.btn-remove {
  background-color: #ffebee;
  color: #c62828;
  border: 1px dashed #f44336;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
}

.btn-remove:hover {
  background-color: #ffcdd2;
}

.btn-remove:disabled {
  opacity: 0.5;
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
  max-width: 700px;
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-height: 100px;
  resize: vertical;
}

.form-group select[multiple] {
  height: 150px;
}

.fee-items-container {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
  border: 1px dashed #ddd;
}

.fee-item-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.fee-item-row input {
  flex: 1;
}

.fee-item-row input:first-child {
  flex: 2;
}

.preview-section {
  background-color: #f0f8ff;
  padding: 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.preview-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  border-bottom: 1px dashed #b3e5fc;
}

.preview-value {
  font-weight: bold;
  color: #1565c0;
}

/* 导入区域样式 */
.import-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
  flex-wrap: wrap;
}

.import-section input[type="file"] {
  display: none;
}

.help-text {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
  margin-left: 0;
  width: 100%;
}

/* 详细预览区域样式 */
.detailed-preview-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.detailed-preview-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: left;
}

.preview-table-container {
  overflow-x: auto;
  max-height: 300px;
  overflow-y: auto;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.preview-table th,
.preview-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.preview-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
  position: sticky;
  top: 0;
  z-index: 10;
}

.preview-table tr:hover {
  background-color: #f9f9f9;
}

.fee-items-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.fee-items-list li {
  font-size: 12px;
  margin-bottom: 3px;
  color: #666;
}

.fee-items-list li::before {
  content: "• ";
  color: #1976d2;
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

/* 居民信息部分样式 */
.resident-info-section {
  margin-bottom: 20px;
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
}

.resident-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 10px;
}

.resident-info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.resident-info-item .detail-label {
  font-weight: bold;
  color: #666;
  font-size: 14px;
}

.resident-info-item .detail-value {
  color: #333;
  font-size: 14px;
  word-break: break-word;
}

.detail-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.fee-items-table,
.payments-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
}

.fee-items-table th,
.payments-table th {
  background-color: #f5f5f5;
  padding: 10px;
  text-align: left;
  font-weight: bold;
  color: #333;
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
  font-size: 12px;
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

.empty-state {
  text-align: center;
  padding: 50px 20px;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 8px;
}
</style>