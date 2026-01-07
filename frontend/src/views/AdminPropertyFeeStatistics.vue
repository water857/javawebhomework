<template>
  <div class="admin-statistics-container">
    <div class="header-section">
      <button class="btn btn-back" @click="goBack">返回首页</button>
      <h2>物业费收费统计</h2>
    </div>
    
    <!-- 统计概览 -->
    <div class="statistics-overview">
      <div class="stat-card">
        <h3>已缴金额</h3>
        <p class="stat-value">¥{{ totalPaidAmount.toFixed(2) }}</p>
        <p class="stat-label">本期已收缴物业费总额</p>
      </div>
      <div class="stat-card">
        <h3>未缴金额</h3>
        <p class="stat-value warning">¥{{ totalUnpaidAmount.toFixed(2) }}</p>
        <p class="stat-label">本期尚未缴纳的物业费总额</p>
      </div>
      <div class="stat-card">
        <h3>缴费率</h3>
        <p class="stat-value">{{ paymentRate.toFixed(1) }}%</p>
        <p class="stat-label">本期物业费缴纳比例</p>
      </div>
      <div class="stat-card">
        <h3>应缴户数</h3>
        <p class="stat-value">{{ totalHouseholds }}</p>
        <p class="stat-label">本期应缴纳物业费的总户数</p>
      </div>
      <div class="stat-card">
        <h3>已缴户数</h3>
        <p class="stat-value">{{ paidHouseholds }}</p>
        <p class="stat-label">本期已缴纳物业费的户数</p>
      </div>
      <div class="stat-card">
        <h3>未缴户数</h3>
        <p class="stat-value warning">{{ unpaidHouseholds }}</p>
        <p class="stat-label">本期尚未缴纳物业费的户数</p>
      </div>
      <div class="stat-card">
        <h3>总账单数</h3>
        <p class="stat-value">{{ totalBills }}</p>
        <p class="stat-label">本期生成的总账单数量</p>
      </div>
      <div class="stat-card">
        <h3>逾期账单</h3>
        <p class="stat-value warning">{{ overdueCount }}</p>
        <p class="stat-label">本期逾期未缴费的账单数量</p>
      </div>
      <div class="stat-card">
        <h3>部分缴费</h3>
        <p class="stat-value">{{ partiallyPaidCount }}</p>
        <p class="stat-label">本期部分缴费的账单数量</p>
      </div>
      <div class="stat-card">
        <h3>平均缴费额</h3>
        <p class="stat-value">¥{{ averagePaymentAmount.toFixed(2) }}</p>
        <p class="stat-label">平均每户缴费金额</p>
      </div>
      <div class="stat-card">
        <h3>总支付笔数</h3>
        <p class="stat-value">{{ totalPayments }}</p>
        <p class="stat-label">本期收到的总支付笔数</p>
      </div>
      <div class="stat-card">
        <h3>支付总额</h3>
        <p class="stat-value">¥{{ totalPaymentAmount.toFixed(2) }}</p>
        <p class="stat-label">本期收到的总支付金额</p>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <h3>筛选条件</h3>
      <div class="filter-form">
        <div class="form-item">
          <label for="period">缴费周期</label>
          <select id="period" v-model="filter.period" @change="fetchStatistics">
            <option value="">全部</option>
            <option v-for="period in availablePeriods" :key="period" :value="period">{{ period }}</option>
          </select>
        </div>
        <div class="form-item">
          <label for="startDate">开始日期</label>
          <input type="date" id="startDate" v-model="filter.startDate" @change="fetchStatistics">
        </div>
        <div class="form-item">
          <label for="endDate">结束日期</label>
          <input type="date" id="endDate" v-model="filter.endDate" @change="fetchStatistics">
        </div>
        <div class="form-item">
          <label for="building">楼栋</label>
          <select id="building" v-model="filter.building" @change="fetchStatistics">
            <option value="">全部</option>
            <option v-for="building in availableBuildings" :key="building" :value="building">{{ building }}</option>
          </select>
        </div>
        <div class="form-item">
          <button class="btn btn-primary" @click="fetchStatistics">查询</button>
          <button class="btn btn-secondary" @click="resetFilter">重置</button>
        </div>
      </div>
    </div>

    <!-- 催缴提醒功能 -->
    <div class="reminder-section">
      <h3>催缴提醒</h3>
      <div class="reminder-form">
        <div class="form-item">
          <label for="reminderPeriod">选择未缴周期</label>
          <select id="reminderPeriod" v-model="reminderForm.period">
            <option v-for="period in availablePeriods" :key="period" :value="period">{{ period }}</option>
          </select>
        </div>
        <div class="form-item">
          <label for="reminderType">提醒方式</label>
          <select id="reminderType" v-model="reminderForm.type">
            <option value="system">系统消息</option>
            <option value="sms">短信提醒</option>
            <option value="email">邮件提醒</option>
          </select>
        </div>
        <div class="form-item">
          <button class="btn btn-warning" @click="sendReminder" :disabled="sendingReminder">
            {{ sendingReminder ? '发送中...' : '发送催缴提醒' }}
          </button>
        </div>
      </div>
      <p v-if="reminderResult" :class="reminderResult.success ? 'success-message' : 'error-message'">
        {{ reminderResult.message }}
      </p>
    </div>

    <!-- 收费趋势图 -->
    <div class="chart-section">
      <h3>收费趋势</h3>
      <div class="chart-container">
        <canvas id="paymentTrendChart"></canvas>
      </div>
    </div>

    <!-- 未缴费用户列表 -->
    <div class="unpaid-section">
      <h3>未缴费用户列表</h3>
      <table class="data-table">
        <thead>
          <tr>
            <th>用户ID</th>
            <th>姓名</th>
            <th>楼栋</th>
            <th>房号</th>
            <th>缴费周期</th>
            <th>应缴金额</th>
            <th>逾期天数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in unpaidUsers" :key="user.userId">
            <td>{{ user.userId }}</td>
            <td>{{ user.userName }}</td>
            <td>{{ user.building }}</td>
            <td>{{ user.roomNumber }}</td>
            <td>{{ user.period }}</td>
            <td>¥{{ user.amount.toFixed(2) }}</td>
            <td>{{ user.overdueDays }}</td>
            <td>
              <button class="btn btn-sm btn-warning" @click="sendSingleReminder(user)">
                发送提醒
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- 分页 -->
      <div class="pagination" v-if="unpaidUsers.length > 0">
        <button class="btn btn-sm" @click="prevPage" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }} / {{ totalPages }}</span>
        <button class="btn btn-sm" @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { propertyFeeApi } from '../services/api';
import Chart from 'chart.js/auto';

export default {
  name: 'AdminPropertyFeeStatistics',
  setup() {
    const router = useRouter();
    
    // 统计数据
    const totalPaidAmount = ref(0);
    const totalUnpaidAmount = ref(0);
    const paymentRate = ref(0);
    const totalHouseholds = ref(0);
    const paidHouseholds = ref(0);
    const unpaidHouseholds = ref(0);
    const totalBills = ref(0);
    const overdueCount = ref(0);
    const partiallyPaidCount = ref(0);
    const totalPayments = ref(0);
    const totalPaymentAmount = ref(0);
    const averagePaymentAmount = ref(0);
    
    // 筛选条件
    const filter = ref({
      period: '',
      startDate: '',
      endDate: '',
      building: ''
    });
    
    // 催缴提醒表单
    const reminderForm = ref({
      period: '',
      type: 'system'
    });
    
    // 催缴结果
    const reminderResult = ref(null);
    const sendingReminder = ref(false);
    
    // 图表实例
    let trendChart = null;
    
    // 可选周期和楼栋
    const availablePeriods = ref(['2024-01', '2024-02', '2024-03', '2024-04', '2024-05', '2024-06']);
    const availableBuildings = ref(['1栋', '2栋', '3栋', '4栋', '5栋', '6栋']);
    
    // 未缴费用户列表
    const unpaidUsers = ref([]);
    const currentPage = ref(1);
    const totalPages = ref(1);
    
    // 初始化
    onMounted(() => {
      fetchStatistics();
    });
    
    // 获取统计数据
    const fetchStatistics = async () => {
      try {
        const response = await propertyFeeApi.getStatistics(filter.value);
        if (response.code === 'success') {
          const data = response.data;
          // 更新基本统计数据
          totalPaidAmount.value = data.totalPaidAmount || 0;
          totalUnpaidAmount.value = data.totalUnpaidAmount || 0;
          paymentRate.value = data.paymentRate || 0;
          totalHouseholds.value = data.totalHouseholds || 0;
          paidHouseholds.value = data.paidHouseholds || 0;
          unpaidHouseholds.value = data.unpaidHouseholds || 0;
          
          // 更新新增的统计字段
          totalBills.value = data.totalBills || 0;
          overdueCount.value = data.overdueCount || 0;
          partiallyPaidCount.value = data.partiallyPaidCount || 0;
          totalPayments.value = data.totalPayments || 0;
          totalPaymentAmount.value = data.totalPaymentAmount || 0;
          averagePaymentAmount.value = data.averagePaymentAmount || 0;
          
          // 更新未缴费用户列表
          unpaidUsers.value = data.unpaidUsers || [];
          totalPages.value = data.totalPages || 1;
          
          // 更新图表
          updateTrendChart(data.trendData || []);
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
      }
    };
    
    // 更新趋势图
    const updateTrendChart = (trendData) => {
      const ctx = document.getElementById('paymentTrendChart');
      if (trendChart) {
        trendChart.destroy();
      }
      
      trendChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: trendData.map(item => item.period),
          datasets: [
            {
              label: '已缴金额',
              data: trendData.map(item => item.paidAmount),
              borderColor: 'rgb(75, 192, 192)',
              backgroundColor: 'rgba(75, 192, 192, 0.2)',
              tension: 0.1
            },
            {
              label: '未缴金额',
              data: trendData.map(item => item.unpaidAmount),
              borderColor: 'rgb(255, 99, 132)',
              backgroundColor: 'rgba(255, 99, 132, 0.2)',
              tension: 0.1
            }
          ]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'top'
            },
            title: {
              display: true,
              text: '物业费收缴趋势'
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: '金额 (元)'
              }
            }
          }
        }
      });
    };
    
    // 发送催缴提醒
    const sendReminder = async () => {
      if (!reminderForm.value.period) {
        reminderResult.value = {
          success: false,
          message: '请选择未缴周期'
        };
        return;
      }
      
      sendingReminder.value = true;
      try {
        const response = await propertyFeeApi.sendBatchReminders(reminderForm.value);
        // 转换响应格式以适配前端显示
        reminderResult.value = {
          success: response.code === 'success',
          message: response.message
        };
      } catch (error) {
        reminderResult.value = {
          success: false,
          message: '发送催缴提醒失败'
        };
      } finally {
        sendingReminder.value = false;
      }
    };
    
    // 发送单个用户催缴提醒
    const sendSingleReminder = async (user) => {
      try {
        const response = await propertyFeeApi.sendBatchReminders({
          period: user.period,
          type: reminderForm.value.type,
          userId: user.userId
        });
        if (response.code === 'success') {
          alert('催缴提醒发送成功');
        } else {
          alert('催缴提醒发送失败: ' + response.message);
        }
      } catch (error) {
        alert('发送催缴提醒失败');
      }
    };
    
    // 重置筛选条件
    const resetFilter = () => {
      filter.value = {
        period: '',
        startDate: '',
        endDate: '',
        building: ''
      };
      fetchStatistics();
    };
    
    // 分页功能
    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        fetchStatistics();
      }
    };
    
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        fetchStatistics();
      }
    };
    
    // 返回首页
    const goBack = () => {
      router.push('/admin');
    };
    
    return {
      totalPaidAmount,
      totalUnpaidAmount,
      paymentRate,
      totalHouseholds,
      paidHouseholds,
      unpaidHouseholds,
      totalBills,
      overdueCount,
      partiallyPaidCount,
      totalPayments,
      totalPaymentAmount,
      averagePaymentAmount,
      filter,
      reminderForm,
      reminderResult,
      sendingReminder,
      availablePeriods,
      availableBuildings,
      unpaidUsers,
      currentPage,
      totalPages,
      fetchStatistics,
      sendReminder,
      sendSingleReminder,
      resetFilter,
      prevPage,
      nextPage,
      goBack
    };
  }
};
</script>

<style scoped>
.admin-statistics-container {
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
  margin-bottom: 20px;
  font-size: 24px;
}

.statistics-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  color: #666;
  font-size: 16px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin: 10px 0;
}

.stat-value.warning {
  color: #ff6b6b;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

.filter-section,
.reminder-section,
.chart-section,
.unpaid-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-section h3,
.reminder-section h3,
.chart-section h3,
.unpaid-section h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 18px;
}

.filter-form,
.reminder-form {
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

.chart-container {
  height: 400px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.success-message {
  color: #67c23a;
  margin-top: 10px;
}

.error-message {
  color: #f56c6c;
  margin-top: 10px;
}
</style>