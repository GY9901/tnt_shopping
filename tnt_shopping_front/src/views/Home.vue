<template>
  <div class="home-layout">
    <!-- 顶部导航栏 -->
    <div class="nav-bar">
      <div class="logo-area">TNT SPACE</div>

      <!-- Tab 切换区 -->
      <div class="tabs-area">
        <div
            class="tab-item"
            :class="{ active: currentTab === 'Index' }"
            @click="currentTab = 'Index'"
        >
          首页
        </div>
        <div
            class="tab-item"
            :class="{ active: currentTab === 'Category' }"
            @click="currentTab = 'Category'"
        >
          商品分类
        </div>
        <div
            class="tab-item"
            :class="{ active: currentTab === 'Cart' }"
            @click="currentTab = 'Cart'"
        >
          购物车
        </div>
        <div
            class="tab-item"
            :class="{ active: currentTab === 'Profile' }"
            @click="currentTab = 'Profile'"
        >
          个人中心
        </div>
      </div>

      <div class="user-action">
        <span class="welcome">Hi, {{ username }}</span>
        <button class="logout-btn" @click="logout">EXIT</button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="main-content">
      <!-- 根据 Tab 动态展示组件 -->
      <transition name="fade" mode="out-in">
        <component :is="currentComponent" />
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import IndexPage from '../components/IndexPage.vue'
import CategoryPage from '../components/CategoryPage.vue'
import CartPage from '../components/CartPage.vue'
import ProfilePage from '../components/ProfilePage.vue'

const router = useRouter()
const username = ref('User')
const currentTab = ref('Index') // 默认首页

// 计算当前应该渲染哪个组件
const currentComponent = computed(() => {
  switch (currentTab.value) {
    case 'Index': return IndexPage
    case 'Category': return CategoryPage
    case 'Cart': return CartPage
    case 'Profile': return ProfilePage
    default: return IndexPage
  }
})

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      username.value = user.username
    } catch (e) {
      router.push('/login')
    }
  } else {
    router.push('/login')
  }
})

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.home-layout {
  min-height: 100vh;
  background-color: #FFFDF0; /* 浅黄大背景 */
  display: flex;
  flex-direction: column;
}

/* --- 导航栏 --- */
.nav-bar {
  height: 70px;
  background: #1A1A1A;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  justify-content: space-between;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  position: sticky; /* 吸顶 */
  top: 0;
  z-index: 100;
}

.logo-area {
  font-size: 24px;
  font-weight: 900;
  color: #FAD02C;
  font-family: 'Anton', sans-serif;
  letter-spacing: 2px;
}

.tabs-area {
  display: flex;
  gap: 10px;
  height: 100%;
}

.tab-item {
  height: 100%;
  padding: 0 25px;
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: bold;
  font-size: 15px;
  transition: all 0.3s;
  color: #AAA;
  position: relative;
}

/* Tab 悬停效果 */
.tab-item:hover {
  color: #FFF;
}

/* 激活状态 */
.tab-item.active {
  color: #FAD02C;
  background: rgba(255, 255, 255, 0.05);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: #FAD02C;
}

.user-action {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome {
  font-size: 12px;
  color: #DDD;
}

.logout-btn {
  background: transparent;
  border: 1px solid #FAD02C;
  color: #FAD02C;
  padding: 4px 12px;
  font-size: 12px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: #FAD02C;
  color: #000;
}

/* --- 内容区 --- */
.main-content {
  flex: 1;
  padding: 30px 40px;
}

/* 简单的过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 占位页样式 */
:deep(.placeholder-page) {
  text-align: center;
  font-size: 24px;
  color: #999;
  padding-top: 100px;
  font-weight: bold;
}
</style>