import { createRouter, createWebHistory } from 'vue-router'

// 路由懒加载写法
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Home.vue')
    }
]

const router = createRouter({
    // 使用 WebHistory 模式 (URL不带#号)，需要后端支持，开发环境没问题
    history: createWebHistory(),
    routes
})

export default router