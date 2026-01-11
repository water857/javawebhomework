import axios from 'axios';

// 接口服务配置
const apiClient = axios.create({
  baseURL: '/api'
});

apiClient.interceptors.request.use((config) => {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : '';
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 用户相关接口
export const userApi = {
  // 获取用户信息
  getUserInfo() {
    return apiRequest('/user');
  },
  
  // 更新个人资料
  updateProfile(data) {
    return apiRequest('/profile/update', 'PUT', data);
  },
  
  // 更新身份证信息
  updateIdCard(data) {
    return apiRequest('/profile/update-idcard', 'PUT', data);
  },
  
  // 修改密码
  changePassword(data) {
    return apiRequest('/change-password', 'POST', data);
  }
};

// 处理接口响应
function handleResponse(response) {
  const responseData = response.data;
  // 检查响应是否包含code或status字段，如果是，说明是标准响应格式
  if (responseData.code || responseData.status) {
    const status = responseData.code || responseData.status;
    if (status === 'error') {
      throw new Error(responseData.message);
    }
    return responseData;
  }
  return responseData;
}

// 接口请求函数
export async function apiRequest(endpoint, method = 'GET', data = null) {
  try {
    const response = await apiClient.request({
      url: endpoint,
      method,
      data
    });
    return handleResponse(response);
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
}

// 活动相关接口
export const activityApi = {
  // 获取活动列表
  getActivities(page = 1, pageSize = 10, searchQuery = '', statusFilter = '') {
    let url = `/activities?page=${page}&pageSize=${pageSize}`;
    if (searchQuery) {
      url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    if (statusFilter) {
      url += `&status=${statusFilter}`;
    }
    return apiRequest(url);
  },

  // 获取活动详情
  getActivityDetail(activityId) {
    return apiRequest(`/activities/${activityId}`);
  },

  // 发布活动
  publishActivity(activityData) {
    return apiRequest('/activities', 'POST', activityData);
  },

  // 报名活动
  registerForActivity(activityId) {
    return apiRequest(`/activities/register/${activityId}`, 'POST');
  },

  // 签到
  checkin(activityId) {
    return apiRequest(`/activities/checkin/${activityId}`, 'POST');
  },

  // 评价活动
  evaluateActivity(activityId, evaluationData) {
    return apiRequest(`/activities/evaluate/${activityId}`, 'POST', evaluationData);
  },

  // 上传活动图片
  uploadActivityImage(activityId, imageUrl) {
    return apiRequest(`/activities/images/${activityId}`, 'POST', { imageUrl });
  },

  // 生成签到二维码
  generateCheckinQrCode(activityId) {
    return apiRequest(`/activities/qrcode/${activityId}`, 'POST');
  },

  // 取消报名
  cancelRegistration(activityId) {
    return apiRequest(`/activities/cancel/${activityId}`, 'DELETE');
  },

  // 获取用户发布的活动
  getUserActivities(userId, page = 1, pageSize = 10, searchQuery = '', statusFilter = '') {
    let url = `/activities/user/${userId}?page=${page}&pageSize=${pageSize}`;
    if (searchQuery) {
      url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    if (statusFilter) {
      url += `&status=${statusFilter}`;
    }
    return apiRequest(url);
  },

  // 获取活动参与者列表
  getActivityParticipants(activityId, page = 1, pageSize = 10) {
    return apiRequest(`/activities/participants/${activityId}?page=${page}&pageSize=${pageSize}`);
  },

  // 获取活动图片列表
  getActivityImages(activityId) {
    return apiRequest(`/activities/images/${activityId}`);
  },

  // 更新活动
  updateActivity(activityId, activityData) {
    return apiRequest(`/activities/${activityId}`, 'PUT', activityData);
  },

  // 更新活动状态
  updateActivityStatus(activityId, status) {
    return apiRequest(`/activities/status/${activityId}`, 'PUT', { status });
  },

  // 删除活动
  deleteActivity(activityId) {
    return apiRequest(`/activities/${activityId}`, 'DELETE');
  }
};

// 公告相关API
export const announcementApi = {
  // 获取公告列表
  getAnnouncements(page = 1, pageSize = 10, searchQuery = '', statusFilter = null) {
    let url = `/announcements?page=${page}&pageSize=${pageSize}`;
    if (searchQuery) {
      url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    if (statusFilter !== null) {
      url += `&status=${statusFilter}`;
    }
    return apiRequest(url);
  },

  // 获取最新公告
  getLatestAnnouncements(limit = 5) {
    return apiRequest(`/announcements/latest?limit=${limit}`);
  },

  // 获取公告详情
  getAnnouncementDetail(announcementId) {
    return apiRequest(`/announcements/${announcementId}`);
  },

  // 发布公告
  publishAnnouncement(announcementData) {
    return apiRequest('/announcements', 'POST', announcementData);
  },

  // 保存草稿
  saveDraft(announcementData) {
    announcementData.action = 'saveDraft';
    return apiRequest('/announcements', 'POST', announcementData);
  },

  // 更新公告
  updateAnnouncement(announcementId, announcementData) {
    return apiRequest(`/announcements/${announcementId}`, 'PUT', announcementData);
  },

  // 更新公告状态
  updateAnnouncementStatus(announcementId, status) {
    return apiRequest(`/announcements/status/${announcementId}`, 'PUT', { status });
  },

  // 删除公告
  deleteAnnouncement(announcementId) {
    return apiRequest(`/announcements/${announcementId}`, 'DELETE');
  },

  // 获取发布者的公告列表
  getAuthorAnnouncements(authorId, page = 1, pageSize = 10, searchQuery = '', statusFilter = null) {
    let url = `/announcements/author/${authorId}?page=${page}&pageSize=${pageSize}`;
    if (searchQuery) {
      url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    if (statusFilter !== null) {
      url += `&status=${statusFilter}`;
    }
    return apiRequest(url);
  }
};

// 天气相关API
export const weatherApi = {
  // 获取天气信息和温馨提示
  getWeatherTips(city) {
    let url = `/weather`;
    if (city) {
      url += `?city=${city}`;
    }
    return apiRequest(url);
  }
};

// 物业费相关API
export const propertyFeeApi = {
  // 获取居民账单列表
  getBills(page = 1, pageSize = 10, filter = {}) {
    let url = `/property-fee/bills?page=${page}&pageSize=${pageSize}`;
    if (filter.period) url += `&period=${filter.period}`;
    if (filter.status) url += `&status=${filter.status}`;
    if (filter.startDate) url += `&startDate=${filter.startDate}`;
    if (filter.endDate) url += `&endDate=${filter.endDate}`;
    return apiRequest(url);
  },

  // 获取账单详情
  getBillDetail(billId) {
    return apiRequest(`/property-fee/bills/${billId}`);
  },

  // 发起支付
  initiatePayment(data) {
    return apiRequest('/property-fee/pay', 'POST', data);
  },

  // 获取支付记录
  getPaymentHistory(page = 1, pageSize = 10, filter = {}) {
    let url = `/property-fee/payments?page=${page}&pageSize=${pageSize}`;
    if (filter.period) url += `&period=${filter.period}`;
    if (filter.startDate) url += `&startDate=${filter.startDate}`;
    if (filter.endDate) url += `&endDate=${filter.endDate}`;
    if (filter.minAmount) url += `&minAmount=${filter.minAmount}`;
    if (filter.maxAmount) url += `&maxAmount=${filter.maxAmount}`;
    if (filter.billId) url += `&billId=${filter.billId}`;
    return apiRequest(url);
  },

  // 管理员批量生成账单
  batchGenerateBills(data) {
    return apiRequest('/admin/property-fee/bills/batch', 'POST', data);
  },

  // 管理员更新账单
  updateBill(billId, data) {
    return apiRequest(`/admin/property-fee/bills/${billId}`, 'PUT', data);
  },

  // 管理员删除账单
  deleteBill(billId) {
    return apiRequest(`/admin/property-fee/bills/${billId}`, 'DELETE');
  },

  // 管理员获取所有账单
  getAllBills(filter = {}) {
    let url = `/admin/property-fee/bills`;
    let hasParams = false;
    
    if (filter.status) {
      url += `?status=${filter.status}`;
      hasParams = true;
    }
    if (filter.startDate) {
      url += `${hasParams ? '&' : '?'}startDate=${filter.startDate}`;
      hasParams = true;
    }
    if (filter.endDate) {
      url += `${hasParams ? '&' : '?'}endDate=${filter.endDate}`;
    }
    return apiRequest(url);
  },

  // 获取统计数据
  getStatistics(filter = {}) {
    let url = `/admin/property-fee/statistics`;
    let queryParams = [];
    
    if (filter.period) queryParams.push(`period=${filter.period}`);
    if (filter.startDate) queryParams.push(`startDate=${filter.startDate}`);
    if (filter.endDate) queryParams.push(`endDate=${filter.endDate}`);
    if (filter.building) queryParams.push(`building=${filter.building}`);
    
    if (queryParams.length > 0) {
      url += `?${queryParams.join('&')}`;
    }
    
    return apiRequest(url);
  },



  // 开始对账
  startReconciliation(data) {
    return apiRequest('/admin/property-fee/reconcile', 'POST', data);
  },

  // 获取对账历史
  getReconciliationHistory() {
    return apiRequest('/admin/property-fee/reconcile/history');
  },

  // 处理异常交易
  resolveException(transactionId) {
    return apiRequest(`/admin/property-fee/reconcile/resolve/${transactionId}`, 'POST');
  },

  // 发送单个催缴提醒
  sendReminder(billId) {
    return apiRequest(`/admin/property-fee/bills/${billId}/remind`, 'POST');
  },

  // 发送批量催缴提醒
  sendBatchReminders(data) {
    return apiRequest('/admin/property-fee/reminders', 'POST', data);
  }
};
