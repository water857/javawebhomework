import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/css/style.css'
import axios from 'axios'

// 配置axios全局baseURL
axios.defaults.baseURL = 'http://localhost:8080/backend'

const app = createApp(App)
app.use(router)
app.mount('#app')