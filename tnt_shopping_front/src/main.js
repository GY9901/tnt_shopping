import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 引入所有图标
import axios from 'axios'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 配置全局 Axios
axios.defaults.baseURL = "http://localhost:9090" // 后端地址
app.config.globalProperties.$request = axios

app.use(router)
app.use(ElementPlus)
app.mount('#app')