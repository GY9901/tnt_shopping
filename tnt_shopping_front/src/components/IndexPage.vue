<template>
  <div class="index-container">

    <!-- 模块一：顶部大轮播图 (MAIN) -->
    <div class="top-section">
      <el-carousel trigger="click" height="300px" class="main-carousel">
        <el-carousel-item v-for="item in bannerData.MAIN" :key="item.id">
          <div
              class="carousel-content"
              :style="getBackgroundStyle(item)"
          >
            <!-- 如果有图片，文字可以根据设计需求决定是否隐藏，这里保留文字覆盖在图上 -->
            <div class="text-overlay" v-if="!item.imageUrl">
              <h1 class="carousel-text">{{ item.title }}</h1>
              <p class="carousel-sub" v-if="item.subTitle">{{ item.subTitle }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 模块二：底部左右分栏 -->
    <div class="bottom-section">

      <!-- 左侧：双层小轮播图 -->
      <div class="left-col">
        <!-- 左上轮播 (EXCLUSIVE) -->
        <div class="sub-carousel-wrapper">
          <div class="section-title">EXCLUSIVE / 独家花絮</div>
          <el-carousel indicator-position="none" arrow="always" height="180px">
            <el-carousel-item v-for="item in bannerData.EXCLUSIVE" :key="item.id">
              <div class="sub-carousel-content" :style="getBackgroundStyle(item)">
                <span class="sub-text" :class="{ 'light-text': isDarkColor(item.bgColor) }">
                  {{ item.title }}
                </span>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 左下轮播 (HOT) -->
        <div class="sub-carousel-wrapper">
          <div class="section-title">HOT SALES / 热销单品</div>
          <el-carousel indicator-position="none" arrow="always" height="180px">
            <el-carousel-item v-for="item in bannerData.HOT" :key="item.id">
              <div class="sub-carousel-content" :style="getBackgroundStyle(item)">
                 <span class="sub-text" :class="{ 'light-text': isDarkColor(item.bgColor) }">
                  {{ item.title }}
                </span>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>

      <!-- 右侧：留言板 -->
      <div class="right-col">
        <div class="message-board">
          <div class="board-header">
            <h3>MESSAGE BOARD</h3>
            <span class="count">{{ messages.length }} MESSAGES</span>
          </div>

          <div class="message-list" ref="msgListRef">
            <div v-if="messages.length === 0" class="empty-state">
              暂无留言，快来抢沙发~
            </div>
            <div v-else class="msg-item" v-for="(msg, index) in messages" :key="index">
              <div class="msg-avatar">
                {{ msg.user ? msg.user.charAt(0).toUpperCase() : '?' }}
              </div>
              <div class="msg-content">
                <div class="msg-user">{{ msg.user }} <span class="msg-time">{{ msg.time }}</span></div>
                <div class="msg-text">{{ msg.text }}</div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <el-input
                v-model="newMessage"
                placeholder="写下你的应援留言..."
                @keyup.enter="postMessage"
                class="tnt-input"
            >
              <template #append>
                <el-button @click="postMessage" class="send-btn" :loading="sending">SEND</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()

// --- 轮播图数据 ---
const bannerData = reactive({
  MAIN: [],      // 顶部
  EXCLUSIVE: [], // 左上
  HOT: []        // 左下
})

// 加载轮播图数据
const fetchBanners = async () => {
  try {
    const res = await proxy.$request.get('/carousel/list')
    if (res.data.code === '200') {
      const data = res.data.data
      bannerData.MAIN = data.MAIN || []
      bannerData.EXCLUSIVE = data.EXCLUSIVE || []
      bannerData.HOT = data.HOT || []
    }
  } catch (e) {
    console.error('获取轮播图失败', e)
  }
}

// 辅助函数：生成背景样式 (优先图片，其次颜色)
const getBackgroundStyle = (item) => {
  if (item.imageUrl) {
    return {
      backgroundImage: `url(${item.imageUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {
    backgroundColor: item.bgColor || '#1A1A1A'
  }
}

// 辅助函数：简单判断背景色深浅以调整文字颜色 (针对 mock 数据)
const isDarkColor = (hex) => {
  if (!hex) return true
  // 简单逻辑：如果是白色或浅色背景，返回 false
  const lightColors = ['#FFFFFF', '#ffffff', '#FFF', '#fff', '#FAD02C']
  return !lightColors.includes(hex)
}

// --- 留言板逻辑 ---
const newMessage = ref('')
const msgListRef = ref(null)
const currentUser = ref('User')
const messages = ref([]) // 移除默认假数据
const sending = ref(false)

// 格式化时间函数
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  // 返回 HH:mm 格式
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取留言列表
const fetchMessages = async () => {
  try {
    const res = await proxy.$request.get('/message/list')
    if (res.data.code === '200') {
      // 将后端数据转换为前端展示格式
      messages.value = res.data.data.map(m => ({
        user: m.username,
        text: m.content,
        time: formatTime(m.createTime)
      }))

      // 滚动到底部
      nextTick(() => {
        if (msgListRef.value) {
          msgListRef.value.scrollTop = msgListRef.value.scrollHeight
        }
      })
    }
  } catch (e) {
    console.error('获取留言失败', e)
  }
}

const postMessage = async () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }

  sending.value = true
  try {
    const res = await proxy.$request.post('/message/add', {
      username: currentUser.value,
      content: newMessage.value
    })

    if (res.data.code === '200') {
      ElMessage.success('留言发布成功')
      newMessage.value = ''
      // 重新获取最新留言
      await fetchMessages()
    } else {
      ElMessage.error(res.data.msg || '留言失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，留言失败')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchBanners() // 获取轮播图
  fetchMessages() // 获取留言

  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const u = JSON.parse(userStr)
      currentUser.value = u.username
    } catch(e) {
      e.toString()
    }
  }
})
</script>

<style scoped>
.index-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

/* --- 顶部大轮播 --- */
.top-section {
  margin-bottom: 20px;
  border: 2px solid #000;
}

.carousel-content {
  width: 100%;
  height: 100%;
  /* 默认背景，会被动态style覆盖 */
  background-color: #FAD02C;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #000;
}

.text-overlay {
  text-align: center;
}

.carousel-text {
  font-size: 48px;
  font-weight: 900;
  margin: 0;
  font-family: 'Anton', sans-serif;
  letter-spacing: 2px;
}

.carousel-sub {
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  border-top: 2px solid #000;
  padding-top: 10px;
  margin-top: 10px;
}

/* --- 底部左右分栏 --- */
.bottom-section {
  display: flex;
  gap: 20px;
  height: 450px;
}

/* 左侧列 */
.left-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sub-carousel-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  background: #000;
  color: #FAD02C;
  padding: 5px 10px;
  display: inline-block;
  margin-bottom: 0;
  width: fit-content;
}

.sub-carousel-content {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-size: 24px;
  border: 2px solid #000;
  background-repeat: no-repeat;
}

.sub-text {
  color: #000; /* 默认黑字 */
}
.sub-text.light-text {
  color: #FFF; /* 深色背景用白字 */
}

/* 右侧列：留言板 */
.right-col {
  width: 350px;
  border: 2px solid #000;
  background: #FFF;
  display: flex;
  flex-direction: column;
}

.message-board {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.board-header {
  background: #000;
  color: #FAD02C;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.board-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
}

.count {
  font-size: 12px;
  background: #FAD02C;
  color: #000;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: bold;
}

.message-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #FAFAFA;
}

.empty-state {
  text-align: center;
  color: #999;
  margin-top: 50px;
  font-size: 14px;
}

.msg-item {
  display: flex;
  margin-bottom: 15px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  background: #000;
  color: #FAD02C;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 10px;
  flex-shrink: 0;
}

.msg-content {
  background: #FFF;
  border: 1px solid #EEE;
  padding: 8px 12px;
  border-radius: 0 10px 10px 10px;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.05);
  max-width: 80%;
}

.msg-user {
  font-size: 12px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.msg-time {
  font-weight: normal;
  color: #999;
  margin-left: 5px;
  font-size: 10px;
}

.msg-text {
  font-size: 14px;
  color: #555;
  word-break: break-all;
}

.input-area {
  padding: 15px;
  border-top: 2px solid #000;
  background: #FFF;
}

/* 覆盖 Element 样式 */
:deep(.tnt-input .el-input__wrapper) {
  border-radius: 0;
  box-shadow: none !important;
  border: 2px solid #000;
}

:deep(.tnt-input .el-input-group__append) {
  background-color: #000;
  border: 2px solid #000;
  border-left: 0;
  color: #FAD02C;
  border-radius: 0;
  font-weight: bold;
}

:deep(.send-btn:hover) {
  color: #FFF;
}
</style>