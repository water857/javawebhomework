import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/css/style.css'
import axios from 'axios'

// 配置axios全局baseURL
axios.defaults.baseURL = '/api'
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const app = createApp(App)
app.use(router)
app.mount('#app')
