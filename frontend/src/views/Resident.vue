<template>
  <div class="container">
    <header>
      <h1>居民中心</h1>
      <div class="user-info">
        <span>欢迎您，{{ username }}</span>
        <button class="btn" @click="handleBack">返回首页</button>
        <button class="btn" @click="handleProfile">个人中心</button>
        <button class="btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <!-- 天气温馨提示 -->
    <div class="weather-tips" v-if="weatherTips">
      <h3>🌤️ 今日天气温馨提示</h3>
      <pre>{{ weatherTips }}</pre>
    </div>
    
    <!-- 实时消息通知 -->
    <div class="notification" v-if="notification">
      <h3>📢 通知</h3>
      <p>{{ notification }}</p>
      <button class="close-btn" @click="notification = ''">×</button>
    </div>
    
    <div class="role-features">
      <h2>居民功能</h2>
      <div class="features-grid">
        <div class="feature-card">
          <h3>物业报修</h3>
          <p>提交家中设备故障维修申请</p>
          <button class="btn" @click="handleRepair">立即报修</button>
        </div>
        <div class="feature-card">
          <h3>费用查询</h3>
          <p>查看物业费、水电费等账单信息</p>
          <button class="btn" @click="handlePropertyFee">查询账单</button>
        </div>
        <div class="feature-card">
          <h3>社区公告</h3>
          <p>浏览社区最新通知和活动信息</p>
          <button class="btn" @click="handleAnnouncements">查看公告</button>
        </div>
        <div class="feature-card">
          <h3>邻里圈</h3>
          <p>分享生活动态，与邻居互动交流</p>
          <button class="btn" @click="handleCommunity">进入邻里圈</button>
        </div>
        <div class="feature-card">
          <h3>社区活动</h3>
          <p>查看和参与社区组织的各种活动</p>
          <button class="btn" @click="handleActivities">查看活动</button>
        </div>
        <div class="feature-card">
          <h3>私信会话</h3>
          <p>查看与居民之间的私信对话</p>
          <button class="btn" @click="handleMessages">进入私信</button>
        </div>
        <div class="feature-card">
          <h3>车位申请</h3>
          <p>查看车位并提交申请</p>
          <button class="btn" @click="handleParking">申请车位</button>
        </div>
        <div class="feature-card">
          <h3>访客登记</h3>
          <p>登记访客来访信息</p>
          <button class="btn" @click="handleVisitor">登记访客</button>
        </div>
        <div class="feature-card">
          <h3>二手市场</h3>
          <p>发布和查看二手商品</p>
          <button class="btn" @click="handleSecondHand">进入市场</button>
        </div>
        <div class="feature-card">
          <h3>技能交换</h3>
          <p>发布和查看技能互助信息</p>
          <button class="btn" @click="handleSkillShare">查看技能</button>
        </div>
        <div class="feature-card">
          <h3>失物招领</h3>
          <p>发布丢失或拾到信息</p>
          <button class="btn" @click="handleLostFound">查看列表</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import websocketService from '../services/websocket';
import { weatherApi } from '../services/api';

export default {
  name: 'Resident',
  data() {
    return {
      username: '',
      weatherTips: '',
      notification: '',
      userId: ''
    }
  },
  mounted() {
    // 从本地存储获取用户信息
    this.username = localStorage.getItem('username');
    this.userId = localStorage.getItem('username'); // 假设用户名就是用户ID
    
    // 初始化WebSocket连接
    this.initWebSocket();
    
    // 获取天气信息
    this.getWeatherInfo();
  },
  beforeUnmount() {
    // 断开WebSocket连接
    websocketService.disconnect();
  },
  methods: {
    // 初始化WebSocket
    initWebSocket() {
      if (this.userId) {
        websocketService.connect(this.userId);
        
        // 订阅天气提醒消息
        websocketService.subscribe('weather_tips', (message) => {
          this.weatherTips = message.content;
          this.notification = '收到新的天气温馨提示！';
        });
        
        // 订阅全局消息
        websocketService.subscribe('*', (message) => {
          console.log('收到全局消息:', message);
        });
      }
    },
    
    // 获取天气信息
    async getWeatherInfo() {
      try {
        const response = await weatherApi.getWeatherTips();
        console.log('天气API响应:', response);
        if (response && response.success) {
          this.weatherTips = response.data;
        } else if (response && response.data) {
          // 处理可能的不同响应格式
          this.weatherTips = response.data;
        } else {
          console.error('天气API响应格式异常:', response);
        }
      } catch (error) {
        console.error('获取天气信息失败:', error);
        // 使用模拟数据作为备用
        this.weatherTips = '今日天气：晴，温度：22℃\n风向：东南，风力：2级，湿度：45%\n温馨提示：今天天气宜人，适合户外活动。';
      }
    },
    
    handleProfile() {
      // 跳转到个人中心页面
      this.$router.push('/resident/profile');
    },
    handleBack() {
      this.$router.push('/resident');
    },
    
    handleRepair() {
      // 跳转到报修页面
      this.$router.push('/resident/repairs');
    },
    
    handleLogout() {
      // 断开WebSocket连接
      websocketService.disconnect();
      
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('role');
      localStorage.removeItem('realName');
      localStorage.removeItem('phone');
      localStorage.removeItem('email');
      localStorage.removeItem('address');
      localStorage.removeItem('idCard');
      
      // 跳转到登录页
      this.$router.push('/login');
    },
    
    handleCommunity() {
      // 跳转到邻里圈页面
      this.$router.push('/resident/community');
    },
    
    handleActivities() {
      // 跳转到活动列表页面
      this.$router.push('/resident/activities');
    },
    
    handleAnnouncements() {
      // 跳转到公告列表页面
      this.$router.push('/resident/announcements');
    },
    
    handlePropertyFee() {
      // 跳转到物业费账单查询页面
      this.$router.push('/resident/property-fee');
    },
    handleMessages() {
      this.$router.push('/resident/messages');
    },
    handleParking() {
      this.$router.push('/resident/parking');
    },
    handleVisitor() {
      this.$router.push('/resident/visitor');
    },
    handleSecondHand() {
      this.$router.push('/resident/second-hand');
    },
    handleSkillShare() {
      this.$router.push('/resident/skill-share');
    },
    handleLostFound() {
      this.$router.push('/resident/lost-found');
    }
  }
}
</script>

<style scoped>
.weather-tips {
  background-color: #f0f8ff;
  border: 1px solid #b0e0e6;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.weather-tips h3 {
  margin-top: 0;
  color: #4682b4;
}

.weather-tips pre {
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0;
  color: #333;
}

.notification {
  background-color: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  position: relative;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.notification h3 {
  margin-top: 0;
  color: #856404;
}

.notification p {
  margin: 0;
  color: #856404;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #856404;
}

.close-btn:hover {
  color: #533f03;
}
</style>
