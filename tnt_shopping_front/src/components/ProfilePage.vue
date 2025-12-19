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
            :class="{ active: activeTab === 'address' }"
            @click="activeTab = 'address'"
        >
          <el-icon><Location /></el-icon>
          <span>地址管理 / ADDRESS</span>
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
      <div v-if="activeTab === 'info'" class="panel-section">
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
      <div v-if="activeTab === 'orders'" class="panel-section">
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
              
              <!-- 收货地址 -->
              <div class="order-address" v-if="order.address">
                <div class="address-label">收货地址 / DELIVERY ADDRESS</div>
                <div class="address-content">
                  <div>{{ order.address }}</div>
                  <div class="address-phone">联系电话: {{ order.phone }}</div>
                </div>
              </div>
            </div>

            <div class="order-footer">
          <div class="order-date">{{ formatTime(order.createTime) }}</div>
          <div class="order-actions">
            <button 
              v-if="order.status === 'PAYED'" 
              class="btn-cancel-order"
              @click="cancelOrder(order.id)"
            >
              <el-icon><Delete /></el-icon>
              取消订单
            </button>
          </div>
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
      <div v-if="activeTab === 'reviews'" class="panel-section">
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

      <!-- Tab 4: 地址管理 -->
      <div v-if="activeTab === 'address'" class="panel-section">
        <h2 class="panel-title">ADDRESS MANAGEMENT / 地址管理</h2>
        
        <!-- 添加地址按钮 -->
        <div class="action-bar">
          <button class="add-btn" @click="openAddAddressDialog">
            <el-icon><Plus /></el-icon>
            添加新地址
          </button>
        </div>
        
        <!-- 地址列表 -->
        <div class="address-list">
          <!-- 只有当有数据时才渲染表格，避免快速布局变化 -->
          <template v-if="addresses.length > 0">
            <el-table :data="addresses" stripe style="width: 100%" border>
              <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
              <el-table-column prop="phone" label="电话" width="150" align="center"></el-table-column>
              <el-table-column prop="address" label="地址" min-width="300"></el-table-column>
              <el-table-column label="默认地址" width="100" align="center">
                <template #default="scope">
                  <el-icon v-if="scope.row.isDefault"><Check /></el-icon>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center">
                <template #default="scope">
                  <button class="btn-small btn-edit" @click="openEditAddressDialog(scope.row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </button>
                  <button class="btn-small btn-delete" @click="deleteAddress(scope.row.id)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </button>
                  <button class="btn-small btn-default" @click="setDefaultAddress(scope.row.id)" :disabled="scope.row.isDefault">
                    设置默认
                  </button>
                </template>
              </el-table-column>
            </el-table>
          </template>
          
          <!-- 空状态 -->
          <div v-else class="empty-state">
            暂无地址记录，点击"添加新地址"按钮添加地址
          </div>
        </div>
        
        <!-- 地址表单弹窗 -->
        <el-dialog
          v-model="addressDialogVisible"
          :title="isEditing ? '编辑地址' : '添加地址'"
          width="500px"
          center
        >
          <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="addressForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
            <el-form-item label="地址" prop="address">
              <el-input type="textarea" v-model="addressForm.address" :rows="3" placeholder="请输入详细地址"></el-input>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="addressDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveAddress">{{ isEditing ? '保存' : '添加' }}</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
      
      <!-- Tab 5: 关于 -->
      <div v-if="activeTab === 'about'" class="panel-section">
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
import { ElMessage, ElMessageBox, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElTable, ElTableColumn, ElCheckbox, ElPagination } from 'element-plus'
import { User, Goods, ChatDotSquare, InfoFilled, SwitchButton, Location, Plus, Edit, Delete, Check } from '@element-plus/icons-vue'

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

// 地址管理相关
const addresses = ref([])
const addressDialogVisible = ref(false)
const isEditing = ref(false)
const currentAddress = ref({})
const addressFormRef = ref(null)

const addressForm = reactive({
  username: '',
  phone: '',
  address: '',
  isDefault: false
})

const addressRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

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
    
    // 3. 获取地址列表
    fetchAddresses()
  } else {
    router.push('/login')
  }
}

// 获取地址列表
const fetchAddresses = async () => {
  try {
    if (!user.value || !user.value.username) {
      console.error('获取地址列表失败: 用户信息未加载')
      return
    }
    const res = await proxy.$request.get('/address/list', { params: { username: user.value.username } })
    console.log('获取地址列表响应:', res)
    if (res.data && res.data.code === '200') {
      addresses.value = res.data.data || []
    } else {
      console.error('获取地址列表失败: 响应码错误', res.data)
    }
  } catch(e) {
    console.error('获取地址列表失败:', e)
    if (e.response) {
      console.error('响应错误:', e.response.data)
      console.error('响应状态:', e.response.status)
    } else if (e.request) {
      console.error('请求错误:', e.request)
    } else {
      console.error('错误信息:', e.message)
    }
  }
}

// 打开添加地址弹窗
const openAddAddressDialog = () => {
  isEditing.value = false
  addressForm.username = user.value.username
  addressForm.phone = ''
  addressForm.address = ''
  addressForm.isDefault = false
  addressDialogVisible.value = true
}

// 打开编辑地址弹窗
const openEditAddressDialog = (address) => {
  isEditing.value = true
  currentAddress.value = address
  addressForm.username = address.username
  addressForm.phone = address.phone
  addressForm.address = address.address
  addressForm.isDefault = address.isDefault || false
  addressDialogVisible.value = true
}

// 保存地址
const saveAddress = async () => {
  if (!addressFormRef.value) return
  
  addressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (!user.value || !user.value.username) {
          ElMessage.error('保存地址失败: 用户信息未加载')
          return
        }
        let res
        if (isEditing.value) {
          // 更新地址
          res = await proxy.$request.put('/address/update', {
            id: currentAddress.value.id,
            ...addressForm
          })
        } else {
          // 添加地址
          res = await proxy.$request.post('/address/add', addressForm)
        }
        
        if (res.data && res.data.code === '200') {
          ElMessage.success(res.data.msg || '操作成功')
          addressDialogVisible.value = false
          // 延迟刷新地址列表，避免 ResizeObserver 循环
          setTimeout(() => {
            fetchAddresses()
          }, 100)
        } else {
          ElMessage.error(res.data.msg || '操作失败')
        }
      } catch(e) {
        console.error('保存地址失败:', e)
        ElMessage.error('保存地址失败')
      }
    }
  })
}

// 删除地址
const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await proxy.$request.delete(`/address/delete/${id}`)
    if (res.data && res.data.code === '200') {
      ElMessage.success(res.data.msg || '删除成功')
      // 延迟刷新地址列表，避免 ResizeObserver 循环
      setTimeout(() => {
        fetchAddresses()
      }, 100)
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch(e) {
    if (e !== 'cancel') {
      console.error('删除地址失败:', e)
      ElMessage.error('删除地址失败')
    }
  }
}

// 设置默认地址
const setDefaultAddress = async (id) => {
  try {
    if (!user.value || !user.value.username) {
      ElMessage.error('设置默认地址失败: 用户信息未加载')
      return
    }
    const res = await proxy.$request.post('/address/setDefault', {
      id: id,
      username: user.value.username
    })
    if (res.data && res.data.code === '200') {
      ElMessage.success(res.data.msg || '设置成功')
      // 延迟刷新地址列表，避免 ResizeObserver 循环
      setTimeout(() => {
        fetchAddresses()
      }, 100)
    } else {
      ElMessage.error(res.data.msg || '设置失败')
    }
  } catch(e) {
    console.error('设置默认地址失败:', e)
    ElMessage.error('设置默认地址失败')
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

// 取消订单
const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await proxy.$request.put(`/order/cancel/${orderId}`)
    if (res.data.code === '200') {
      ElMessage.success('订单取消成功')
      fetchOrders() // 刷新订单列表
    } else {
      ElMessage.error(res.data.msg || '订单取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消订单失败:', e)
      ElMessage.error('取消订单失败')
    }
  }
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

/* 订单地址样式 */
.order-address {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #EEE;
}

.address-label {
  font-size: 12px;
  font-weight: bold;
  color: #999;
  margin-bottom: 8px;
}

.address-content {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.address-phone {
  margin-top: 5px;
  color: #666;
  font-weight: bold;
}

.order-footer {
  border-top: 1px solid #EEE;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FAFAFA;
  gap: 20px;
}
.order-date { font-size: 12px; color: #999; }
.order-actions {
  display: flex;
  gap: 10px;
}
.btn-cancel-order {
  background: #FFF;
  color: #F56C6C;
  border: 2px solid #F56C6C;
  padding: 6px 12px;
  font-weight: bold;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  font-size: 12px;
}
.btn-cancel-order:hover {
  background: #F56C6C;
  color: #FFF;
}
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

/* 地址管理样式 */
.action-bar {
  margin-bottom: 20px;
  text-align: right;
}

.add-btn {
  background: #FAD02C;
  color: #000;
  border: 2px solid #000;
  padding: 10px 20px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #000;
  color: #FAD02C;
}

.address-list {
  margin-top: 20px;
}

:deep(.address-list .el-table) {
  border: 2px solid #000;
  border-radius: 0;
}

:deep(.address-list .el-table__header-wrapper th) {
  background: #000;
  color: #FAD02C;
  font-weight: bold;
  border-bottom: 2px solid #FFF;
}

:deep(.address-list .el-table__body-wrapper tr) {
  border-bottom: 1px solid #EEE;
}

:deep(.address-list .el-table__body-wrapper tr:hover) {
  background: #FFFDF0;
}

:deep(.address-list .el-table__body-wrapper td) {
  border-bottom: 1px solid #EEE;
}

.btn-small {
  padding: 4px 8px;
  margin: 0 4px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  border: 1px solid #000;
  border-radius: 0;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.btn-edit {
  background: #FFF;
  color: #000;
}

.btn-edit:hover {
  background: #FAD02C;
}

.btn-delete {
  background: #FFF;
  color: #F56C6C;
}

.btn-delete:hover {
  background: #F56C6C;
  color: #FFF;
}

.btn-default {
  background: #FFF;
  color: #67C23A;
}

.btn-default:hover:not(:disabled) {
  background: #67C23A;
  color: #FFF;
}

.btn-default:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

:deep(.el-dialog__header) {
  background: #000;
  color: #FAD02C;
  border-bottom: 2px solid #FAD02C;
}

:deep(.el-dialog__title) {
  font-weight: bold;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #EEE;
}

:deep(.el-form-item__label) {
  font-weight: bold;
}
</style>