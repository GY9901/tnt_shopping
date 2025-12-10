<template>
  <div class="profile-layout">

    <!-- 左侧导航栏 -->
    <div class="side-nav">
      <div class="user-brief">
        <div class="avatar">{{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}</div>
        <div class="username">{{ user.username }}</div>
        <div class="role">TNT VIP MEMBER</div>
      </div>

      <div class="nav-menu">
        <div
            class="nav-item"
            :class="{ active: activeTab === 'info' }"
            @click="activeTab = 'info'"
        >
          <el-icon><User /></el-icon>
          <span>个人信息 / INFO</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeTab === 'orders' }"
            @click="activeTab = 'orders'"
        >
          <el-icon><Goods /></el-icon>
          <span>我的订单 / ORDERS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeTab === 'reviews' }"
            @click="activeTab = 'reviews'"
        >
          <el-icon><ChatDotSquare /></el-icon>
          <span>我的评论 / REVIEWS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeTab === 'about' }"
            @click="activeTab = 'about'"
        >
          <el-icon><InfoFilled /></el-icon>
          <span>关于网站 / ABOUT</span>
        </div>
        <div class="nav-item logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录 / EXIT</span>
        </div>
      </div>
    </div>

    <!-- 右侧内容展示区 -->
    <div class="content-panel">

      <!-- Tab 1: 个人信息 -->
      <div v-if="activeTab === 'info'" class="panel-section fade-in">
        <h2 class="panel-title">ACCOUNT INFORMATION</h2>

        <!-- 基础信息展示 -->
        <div class="info-card">
          <div class="info-row">
            <label>MEMBER ID</label>
            <span>#{{ String(user.id || 0).padStart(6, '0') }}</span>
          </div>
          <div class="info-row">
            <label>USERNAME</label>
            <span>{{ user.username }}</span>
          </div>
          <div class="info-row">
            <label>ROLE</label>
            <span class="tag">普通用户</span>
          </div>
          <div class="info-row">
            <label>JOIN DATE</label>
            <!-- 这里的日期现在绑定了 user.createTime -->
            <span>{{ formatDate(user.createTime) }}</span>
          </div>
        </div>

        <!-- 修改密码表单 -->
        <h3 class="sub-title">SECURITY SETTINGS</h3>
        <div class="password-form">
          <el-form :model="pwdForm" :rules="rules" ref="pwdFormRef" label-position="top">

            <!-- 新增：旧密码输入框 -->
            <el-form-item label="CURRENT PASSWORD / 当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password class="custom-input" placeholder="请输入当前密码以验证身份"/>
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="NEW PASSWORD / 新密码" prop="password">
                  <el-input v-model="pwdForm.password" type="password" show-password class="custom-input" placeholder="设置新密码"/>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="CONFIRM PASSWORD / 确认密码" prop="confirm">
                  <el-input v-model="pwdForm.confirm" type="password" show-password class="custom-input" placeholder="再次输入新密码"/>
                </el-form-item>
              </el-col>
            </el-row>
            <button class="action-btn" @click.prevent="updatePassword">UPDATE PASSWORD</button>
          </el-form>
        </div>
      </div>

      <!-- Tab 2: 我的订单 -->
      <div v-if="activeTab === 'orders'" class="panel-section fade-in">
        <h2 class="panel-title">MY ORDER HISTORY</h2>

        <div v-if="orderList.length === 0" class="empty-state">
          暂无订单记录
        </div>

        <div class="order-list" v-loading="ordersLoading">
          <div class="order-card" v-for="order in orderList" :key="order.id">
            <div class="order-header">
              <div class="header-left">
                <span class="order-no">NO. {{ order.orderNo }}</span>
                <span class="pay-method-tag" :class="order.paymentMethod">
                  {{ order.paymentMethod === 'WECHAT' ? 'WeChat Pay' : 'Alipay' }}
                </span>
              </div>
              <span class="order-status" :class="order.status">{{ order.status }}</span>
            </div>

            <div class="order-body">
              <!-- 商品列表 -->
              <div class="order-items">
                <div class="item-row" v-for="(item, idx) in order.items" :key="idx">
                  <div class="item-name">{{ item.productName }}</div>
                  <div class="item-qty">x{{ item.quantity }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                </div>
              </div>
            </div>

            <div class="order-footer">
              <div class="order-date">{{ formatTime(order.createTime) }}</div>
              <div class="order-total">
                TOTAL: <span>¥{{ order.totalAmount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单分页条 -->
        <div class="pagination-container" v-if="orderTotal > 0">
          <el-pagination
              background
              layout="prev, pager, next"
              :total="orderTotal"
              :page-size="orderPageSize"
              :current-page="orderPage"
              @current-change="handleOrderPageChange"
              class="tnt-pagination"
          />
        </div>
      </div>

      <!-- Tab 3: 我的评论 -->
      <div v-if="activeTab === 'reviews'" class="panel-section fade-in">
        <h2 class="panel-title">MY REVIEWS</h2>

        <div v-if="reviewList.length === 0" class="empty-state">
          你还没有发布过任何评论
        </div>

        <div class="review-list">
          <div class="review-item" v-for="msg in reviewList" :key="msg.id">
            <div class="review-content">" {{ msg.content }} "</div>
            <div class="review-date">{{ formatTime(msg.createTime) }} · Posted in Homepage</div>
            <div class="quote-icon">”</div>
          </div>
        </div>
      </div>

      <!-- Tab 4: 关于 -->
      <div v-if="activeTab === 'about'" class="panel-section fade-in">
        <h2 class="panel-title">ABOUT TNT SPACE</h2>
        <div class="about-content">
          <p class="highlight">TEENS IN TIMES (TNT)</p>
          <p>时代少年团（Teens in Times、TNT）是由时代峰峻推出的中国内地男子演唱组合。</p>
          <p>本网站是粉丝自制交流与周边模拟购物平台，旨在为爆米花们提供一个集展示、交流、模拟下单于一体的空间。</p>

          <div class="tech-stack">
            <h4>TECH STACK</h4>
            <div class="tags">
              <span>Vue 3</span>
              <span>Spring Boot</span>
              <span>MySQL</span>
              <span>Element Plus</span>
            </div>
          </div>

          <div class="copyright">
            &copy; 2025 TNT FAN CLUB. DESIGNED FOR POPCORN.
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Goods, ChatDotSquare, InfoFilled, SwitchButton } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const activeTab = ref('info') // 默认Tab
const user = ref({})
const orderList = ref([])
const reviewList = ref([])

// 订单分页
const orderPage = ref(1)
const orderPageSize = ref(5)
const orderTotal = ref(0)
const ordersLoading = ref(false)

// 密码表单 (新增 oldPassword)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', password: '', confirm: '' })

// 校验规则
const validatePass2 = (rule, value, callback) => {
  if (value !== pwdForm.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}
const rules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirm: [{ validator: validatePass2, trigger: 'blur' }]
}

// 格式化时间 (精确到分)
const formatTime = (str) => {
  if(!str) return ''
  return str.replace('T', ' ').substring(0, 19)
}

// 格式化日期 (YYYY-MM-DD) 用于 Join Date
const formatDate = (str) => {
  if(!str) return 'Loading...'
  const date = new Date(str)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 获取数据
const fetchData = async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)

    // 1. 获取订单
    fetchOrders()

    // 2. 获取评论
    try {
      const resMsg = await proxy.$request.get('/message/my', { params: { username: user.value.username } })
      if (resMsg.data.code === '200') reviewList.value = resMsg.data.data
    } catch(e) { console.error(e) }
  } else {
    router.push('/login')
  }
}

const fetchOrders = async () => {
  ordersLoading.value = true
  try {
    const res = await proxy.$request.get('/order/list', {
      params: {
        username: user.value.username,
        pageNum: orderPage.value,
        pageSize: orderPageSize.value
      }
    })
    if (res.data.code === '200') {
      orderList.value = res.data.data.list
      orderTotal.value = res.data.data.total
    }
  } catch(e) {
    console.error(e)
  } finally {
    ordersLoading.value = false
  }
}

const handleOrderPageChange = (val) => {
  orderPage.value = val
  fetchOrders()
}

// 修改密码
const updatePassword = () => {
  if (!pwdFormRef.value) return
  pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await proxy.$request.post('/user/update', {
          id: user.value.id,
          oldPassword: pwdForm.oldPassword, // 传给后端校验
          password: pwdForm.password
        })
        if (res.data.code === '200') {
          ElMessage.success('密码修改成功，请重新登录')
          localStorage.removeItem('user')
          router.push('/login')
        } else {
          ElMessage.error(res.data.msg)
        }
      } catch (e) {
        ElMessage.error('请求失败')
      }
    }
  })
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录吗?', '提示', { confirmButtonText: '确认', cancelButtonText: '取消' })
      .then(() => {
        localStorage.removeItem('user')
        router.push('/login')
      })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Anton&family=Roboto:wght@400;700;900&display=swap');

.profile-layout {
  display: flex;
  min-height: 600px;
  max-width: 1200px;
  margin: 0 auto;
  gap: 40px;
}

/* --- 左侧导航 --- */
.side-nav {
  width: 280px;
  flex-shrink: 0;
  background: #FFF;
  border: 3px solid #000;
  height: fit-content;
  padding-bottom: 20px;
}

.user-brief {
  background: #FAD02C;
  padding: 40px 20px;
  text-align: center;
  border-bottom: 3px solid #000;
  margin-bottom: 10px;
}

.avatar {
  width: 80px;
  height: 80px;
  background: #000;
  color: #FAD02C;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Anton', sans-serif;
  font-size: 40px;
  margin: 0 auto 15px;
  border: 3px solid #FFF;
}

.user-brief .username {
  font-weight: 900;
  font-size: 20px;
  margin-bottom: 5px;
}

.user-brief .role {
  font-size: 12px;
  font-weight: bold;
  background: #FFF;
  display: inline-block;
  padding: 2px 8px;
  border: 1px solid #000;
}

.nav-item {
  padding: 15px 30px;
  font-weight: 900;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.2s;
  color: #888;
}

.nav-item:hover {
  color: #000;
  background: #FFFDF0;
  padding-left: 35px;
}

.nav-item.active {
  color: #000;
  background: #FAD02C;
  border-top: 2px solid #000;
  border-bottom: 2px solid #000;
}

.nav-item.logout {
  margin-top: 20px;
  color: #F56C6C;
  border-top: 1px dashed #CCC;
}

/* --- 右侧内容区 --- */
.content-panel {
  flex: 1;
  background: #FFF;
  border: 3px solid #000;
  padding: 40px;
  position: relative;
}

.content-panel::after {
  content: '';
  position: absolute;
  top: 10px;
  left: 10px;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.1);
  z-index: -1;
}

.panel-title {
  font-family: 'Anton', sans-serif;
  font-size: 32px;
  margin: 0 0 30px 0;
  border-bottom: 4px solid #FAD02C;
  display: inline-block;
  padding-right: 20px;
}

.sub-title {
  font-family: 'Anton', sans-serif;
  font-size: 20px;
  margin: 40px 0 20px;
  color: #666;
}

.fade-in {
  animation: fadeIn 0.4s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Info Card */
.info-card {
  background: #FAFAFA;
  padding: 20px;
  border: 1px solid #EEE;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px dashed #DDD;
  font-weight: bold;
}
.info-row label { color: #999; font-size: 12px; }
.info-row span { color: #000; }

/* 表单样式覆盖 */
:deep(.custom-input .el-input__wrapper) {
  border-radius: 0;
  border: 2px solid #000;
  box-shadow: none !important;
}
.action-btn {
  background: #000;
  color: #FAD02C;
  border: none;
  padding: 12px 30px;
  font-family: 'Anton', sans-serif;
  font-size: 16px;
  cursor: pointer;
  margin-top: 20px;
}
.action-btn:hover {
  background: #FAD02C;
  color: #000;
}

/* 订单样式 */
.order-card {
  border: 2px solid #000;
  margin-bottom: 20px;
  transition: box-shadow 0.2s;
}
.order-card:hover {
  box-shadow: 5px 5px 0 #FAD02C;
}

.order-header {
  background: #000;
  color: #FFF;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  font-weight: bold;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.pay-method-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  background: #333;
  color: #CCC;
}
.pay-method-tag.ALIPAY { color: #1677FF; background: #E6F7FF; }
.pay-method-tag.WECHAT { color: #07C160; background: #F6FFED; }

.order-status.COMPLETED { color: #67C23A; }
.order-status.PENDING { color: #E6A23C; }

.order-body {
  padding: 15px 20px;
  background: #FFF;
}
.item-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: bold;
  color: #555;
}
.item-name { flex: 1; }
.item-qty { width: 50px; text-align: center; color: #999; }
.item-price { width: 80px; text-align: right; }

.order-footer {
  border-top: 1px solid #EEE;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FAFAFA;
}
.order-date { font-size: 12px; color: #999; }
.order-total { font-weight: 900; font-size: 18px; }
.order-total span { color: #FAD02C; -webkit-text-stroke: 0.5px #000; }

/* 评论样式 */
.review-item {
  background: #FFFDF0;
  border-left: 4px solid #FAD02C;
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
}
.review-content {
  font-weight: bold;
  font-size: 16px;
  line-height: 1.5;
  margin-bottom: 10px;
}
.review-date {
  font-size: 12px;
  color: #999;
}
.quote-icon {
  position: absolute;
  right: 20px;
  bottom: -10px;
  font-size: 60px;
  color: rgba(250, 208, 44, 0.2);
  font-family: serif;
}

/* About 样式 */
.about-content {
  font-size: 15px;
  line-height: 1.8;
}
.about-content .highlight {
  font-weight: 900;
  font-size: 24px;
  margin-bottom: 20px;
  background: #FAD02C;
  display: inline-block;
  padding: 0 5px;
}
.tech-stack {
  margin-top: 40px;
  border: 2px solid #000;
  padding: 20px;
}
.tech-stack h4 { margin-top: 0; font-family: 'Anton'; }
.tags span {
  background: #000;
  color: #FFF;
  padding: 4px 10px;
  font-size: 12px;
  margin-right: 10px;
  font-weight: bold;
}
.copyright {
  margin-top: 30px;
  font-size: 12px;
  color: #BBB;
  font-weight: bold;
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: #999;
  border: 2px dashed #EEE;
  font-weight: bold;
}

/* 分页条样式 */
.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

:deep(.tnt-pagination.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #000 !important;
  color: #FAD02C !important;
  border: 1px solid #000;
}

:deep(.tnt-pagination.el-pagination.is-background .el-pager li) {
  background-color: #FFF;
  color: #000;
  border: 1px solid #000;
  border-radius: 0;
  font-weight: bold;
}

:deep(.tnt-pagination.el-pagination.is-background .btn-prev),
:deep(.tnt-pagination.el-pagination.is-background .btn-next) {
  background-color: #FFF;
  border: 1px solid #000;
  border-radius: 0;
  color: #000;
}
</style>