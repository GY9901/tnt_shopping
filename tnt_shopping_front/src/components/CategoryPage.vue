<template>
  <div class="category-wrapper">
    <!-- 左侧：分类导航 (固定宽) -->
    <div class="sidebar-nav">
      <div class="nav-title">CATEGORY</div>

      <div
          class="nav-tab"
          :class="{ active: currentCategory === '棉花娃娃' }"
          @click="selectCategory('棉花娃娃')"
      >
        <div class="tab-en">PLUSH DOLL</div>
        <div class="tab-cn">棉花娃娃系列</div>
        <div class="tab-icon" v-show="currentCategory === '棉花娃娃'">
          <el-icon><CaretRight /></el-icon>
        </div>
      </div>

      <div
          class="nav-tab"
          :class="{ active: currentCategory === '小卡' }"
          @click="selectCategory('小卡')"
      >
        <div class="tab-en">PHOTO CARD</div>
        <div class="tab-cn">精选小卡系列</div>
        <div class="tab-icon" v-show="currentCategory === '小卡'">
          <el-icon><CaretRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 右侧：商品展示 (自适应) -->
    <div class="main-display">
      <div class="display-header">
        <h2 class="header-title">{{ currentCategory === '棉花娃娃' ? 'DOLL COLLECTION' : 'CARD COLLECTION' }}</h2>
        <span class="header-tag">TOTAL {{ total }} ITEMS</span>
      </div>

      <!-- 商品 2x2 网格 -->
      <div class="product-grid" v-loading="loading">
        <!-- 循环渲染商品卡片 -->
        <div class="product-card" v-for="item in productList" :key="item.id">

          <!-- 上半部：内容展示 (Flex: 左图55% 右信息45%) -->
          <div class="card-body">
            <!-- 左：商品图 (占比变大) -->
            <div class="p-image-box" :style="getBg(item)">
              <span class="no-img" v-if="!item.imageUrl">TNT</span>
            </div>
            <!-- 右：信息列表 -->
            <div class="p-info-box">
              <div class="p-title">{{ item.name }}</div>

              <div class="p-meta">
                <div class="p-price">
                  <span class="symbol">¥</span>
                  <span class="val">{{ item.price }}</span>
                </div>
                <div class="p-stock">
                  <span>库存: {{ item.stock }}</span>
                  <span class="status-dot" :class="{ 'low': item.stock < 50 }"></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 下半部：操作按钮 -->
          <div class="card-actions">
            <button class="btn btn-cart" @click="addToCart(item)">
              加入购物车
            </button>
            <button class="btn btn-buy" @click="buyNow(item)">
              立即购买
            </button>
          </div>

        </div>

        <!-- 空状态占位 -->
        <div class="empty-placeholder" v-if="!loading && productList.length === 0">
          暂无商品数据
        </div>
      </div>

      <!-- 分页组件 (工业风) -->
      <div class="pagination-bar" v-if="total > 0">
        <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
            class="tnt-pagination"
        />
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog
        v-model="paymentVisible"
        title="CASHIER / 收银台"
        width="500px"
        :show-close="false"
        class="tnt-dialog"
        align-center
    >
      <div class="payment-content">
        <div class="order-summary">
          <div class="summary-title">ORDER SUMMARY</div>
          <div class="summary-list">
            <div class="summary-item">
              <span class="s-name">{{ selectedProduct?.name }} x{{ buyQuantity }}</span>
              <span class="s-price">¥ {{ (selectedProduct?.price * buyQuantity).toFixed(2) }}</span>
            </div>
          </div>
          <div class="summary-total">
            <span>TOTAL</span>
            <span class="s-total-price">¥ {{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <div class="payment-method">
          <div class="method-title">PAYMENT METHOD</div>
          <div
              class="method-option"
              :class="{ active: payMethod === 'alipay' }"
              @click="payMethod = 'alipay'"
          >
            <span class="icon alipay">支</span>
            <span>Alipay / 支付宝</span>
            <el-icon class="check-icon" v-if="payMethod === 'alipay'"><Select /></el-icon>
          </div>
          <div
              class="method-option"
              :class="{ active: payMethod === 'wechat' }"
              @click="payMethod = 'wechat'"
          >
            <span class="icon wechat">微</span>
            <span>WeChat Pay / 微信支付</span>
            <el-icon class="check-icon" v-if="payMethod === 'wechat'"><Select /></el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="paymentVisible = false">CANCEL</button>
          <button class="btn-confirm" @click="handlePay" :disabled="isPaying">
            {{ isPaying ? 'PROCESSING...' : 'PAY NOW' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CaretRight, Select } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const currentCategory = ref('棉花娃娃')
const productList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(4) // 每页4个
const total = ref(0)

// 立即购买相关
const paymentVisible = ref(false)
const payMethod = ref('alipay')
const isPaying = ref(false)
const selectedProduct = ref(null)
const buyQuantity = ref(1)

// 计算总价
const totalPrice = computed(() => {
  if (!selectedProduct.value) return 0
  return selectedProduct.value.price * buyQuantity.value
})

// 模拟图片背景色数组
const bgColors = ['#FAD02C', '#FFD166', '#FFFCF0', '#E0E0E0']

// 获取数据
const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await proxy.$request.get('/product/list', {
      params: {
        category: currentCategory.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    if (res.data.code === '200') {
      productList.value = res.data.data.list
      total.value = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

const selectCategory = (cat) => {
  if (currentCategory.value === cat) return
  currentCategory.value = cat
  currentPage.value = 1 // 切换分类重置第一页
  fetchProducts()
}

const handlePageChange = (val) => {
  currentPage.value = val
  fetchProducts()
}

// 动态背景逻辑
const getBg = (item) => {
  if (item.imageUrl) {
    return { backgroundImage: `url(${item.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  const color = bgColors[item.id % bgColors.length]
  return { backgroundColor: color }
}

// 在 CategoryPage.vue 中修改 addToCart
const addToCart = async (item) => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    return
  }
  const user = JSON.parse(userStr)

  try {
    await proxy.$request.post('/cart/add', {
      username: user.username,
      productId: item.id,
      quantity: 1
    })
    ElMessage.success(`[${item.name}] 已加入购物车`)
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}

const buyNow = (item) => {
  selectedProduct.value = item
  buyQuantity.value = 1
  paymentVisible.value = true
}

// 确认支付
const handlePay = async () => {
  if (!selectedProduct.value) return
  
  isPaying.value = true
  
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    isPaying.value = false
    return
  }
  
  const user = JSON.parse(userStr)
  
  // 构造后端需要的订单数据
  const orderData = {
    username: user.username,
    totalAmount: totalPrice.value,
    paymentMethod: payMethod.value.toUpperCase(),
    items: [{
      productName: selectedProduct.value.name,
      productImg: selectedProduct.value.imageUrl,
      price: selectedProduct.value.price,
      quantity: buyQuantity.value,
      productId: selectedProduct.value.id
    }]
  }
  
  try {
    // 模拟网络延迟体验更好
    setTimeout(async () => {
      const res = await proxy.$request.post('/order/create', orderData)
      if (res.data.code === '200') {
        ElMessage.success({
          message: 'PAYMENT SUCCESSFUL / 支付成功',
          duration: 2000
        })
        paymentVisible.value = false
      } else {
        ElMessage.error(res.data.msg || '下单失败')
      }
      isPaying.value = false
    }, 1000)
    
  } catch (e) {
    ElMessage.error('网络异常')
    isPaying.value = false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Anton&family=Roboto:wght@400;700;900&display=swap');

.category-wrapper {
  display: flex;
  gap: 30px;
  max-width: 1100px;
  margin: 0 auto;
  min-height: 600px;
}

/* --- 左侧导航 --- */
.sidebar-nav {
  width: 240px;
  flex-shrink: 0;
  background: #FFF;
  border: 3px solid #000;
  height: fit-content;
}

.nav-title {
  background: #000;
  color: #FAD02C;
  font-family: 'Anton', sans-serif;
  font-size: 24px;
  padding: 15px 20px;
  letter-spacing: 2px;
}

.nav-tab {
  padding: 20px;
  border-bottom: 2px solid #000;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.nav-tab:last-child {
  border-bottom: none;
}

.nav-tab:hover {
  background: #FFF9E6;
}

.nav-tab.active {
  background: #000;
  color: #FAD02C;
}

.tab-en {
  font-family: 'Anton', sans-serif;
  font-size: 18px;
  margin-bottom: 4px;
}

.tab-cn {
  font-size: 14px;
  font-weight: bold;
}

.tab-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
}

/* --- 右侧展示区 --- */
.main-display {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.display-header {
  border-bottom: 4px solid #000;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 8px;
}

.header-title {
  font-family: 'Anton', sans-serif;
  font-size: 36px;
  margin: 0;
  line-height: 1;
  color: #000;
}

.header-tag {
  background: #FAD02C;
  color: #000;
  font-weight: bold;
  padding: 2px 8px;
  font-size: 12px;
  border: 1px solid #000;
}

/* 2x2 Grid 布局 */
.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr; /* 两列 */
  grid-auto-rows: 240px; /* 增加卡片高度，适应大图 */
  gap: 20px;
}

.product-card {
  background: #FFF;
  border: 2px solid #000;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 6px 6px 0px rgba(0,0,0,1); /* 硬阴影 */
}

/* 上半部分：内容区 */
.card-body {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  border-bottom: 2px solid #000;
  overflow: hidden;
}

/* 左图：占比变大 (75%) */
.p-image-box {
  width: 75%; /* 增加宽度占比 */
  height: 100%;
  border-right: 2px solid #000;
  background-color: #EEE;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.no-img {
  font-family: 'Anton', sans-serif;
  font-size: 40px; /* 字体加大 */
  color: rgba(0,0,0,0.1);
  transform: rotate(-15deg);
}

/* 右信息：占比变小 (45%) */
.p-info-box {
  width: 45%;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-color: #FAFAFA;
}

.p-title {
  font-weight: 900;
  font-size: 16px;
  line-height: 1.3;
  color: #000;
  display: -webkit-box;
  -webkit-line-clamp: 3; /* 允许3行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.p-meta {
  margin-top: auto;
}

.p-price {
  color: #000;
}

.symbol {
  font-size: 14px;
  font-weight: bold;
}

.val {
  font-family: 'Anton', sans-serif;
  font-size: 24px;
  margin-left: 2px;
}

.p-stock {
  font-size: 12px;
  color: #666;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #4CAF50;
  border-radius: 50%;
  border: 1px solid #000;
}
.status-dot.low {
  background: #F44336;
}

/* 下半部分：按钮组 */
.card-actions {
  height: 44px;
  display: flex;
}

.btn {
  flex: 1;
  border: none;
  font-weight: 900;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.5px;
}

.btn-cart {
  background: #FAD02C;
  color: #000;
  border-right: 2px solid #000;
}
.btn-cart:hover {
  background: #FFE066;
}

.btn-buy {
  background: #000;
  color: #FAD02C;
}
.btn-buy:hover {
  background: #333;
  color: #FFF;
}

.empty-placeholder {
  grid-column: span 2;
  text-align: center;
  padding: 60px;
  color: #999;
  font-weight: bold;
  border: 2px dashed #CCC;
}

/* 分页条样式 */
.pagination-bar {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* --- 支付弹窗样式 --- */
.order-summary {
  background: #F9F9F9;
  padding: 15px;
  border: 1px dashed #CCC;
  margin-bottom: 20px;
}

.summary-title {
  font-weight: 900;
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.summary-list {
  max-height: 150px;
  overflow-y: auto;
  margin-bottom: 15px;
  padding-right: 5px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
  color: #555;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 2px solid #000;
  padding-top: 10px;
  font-weight: 900;
}

.s-total-price {
  font-family: 'Anton', sans-serif;
  font-size: 24px;
  color: #000;
}

.method-title {
  font-weight: 900;
  font-size: 14px;
  margin-bottom: 15px;
}

.method-option {
  border: 2px solid #EEE;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  margin-bottom: 10px;
  transition: all 0.2s;
  position: relative;
}

.method-option:hover {
  border-color: #CCC;
}

.method-option.active {
  border-color: #000;
  background: #FFFCF0;
}

.method-option .icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFF;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
}
.method-option .alipay { background: #1677FF; }
.method-option .wechat { background: #07C160; }

.check-icon {
  position: absolute;
  right: 15px;
  color: #000;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  gap: 15px;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  height: 44px;
  font-weight: 900;
  letter-spacing: 1px;
  cursor: pointer;
  border: 2px solid #000;
}

.btn-cancel {
  background: #FFF;
  color: #000;
}
.btn-cancel:hover {
  background: #F0F0F0;
}

.btn-confirm {
  background: #000;
  color: #FAD02C;
}
.btn-confirm:hover {
  background: #FAD02C;
  color: #000;
}
.btn-confirm:disabled {
  background: #999;
  color: #DDD;
  cursor: not-allowed;
  border-color: #999;
}

/* 覆盖 Element Dialog 默认样式 */
:deep(.tnt-dialog) {
  border-radius: 0;
  border: 4px solid #000;
  box-shadow: 10px 10px 0 rgba(0,0,0,0.2);
}
:deep(.tnt-dialog .el-dialog__header) {
  border-bottom: 2px solid #000;
  margin-right: 0;
  padding: 20px;
}
:deep(.tnt-dialog .el-dialog__title) {
  font-family: 'Anton', sans-serif;
  font-size: 24px;
  color: #000;
  letter-spacing: 1px;
}
:deep(.tnt-dialog .el-dialog__body) {
  padding: 20px;
}
:deep(.tnt-dialog .el-dialog__footer) {
  padding: 20px;
  border-top: 2px solid #000;
}

/* 覆盖 Element Pagination 为黑黄风格 */
:deep(.tnt-pagination.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #000 !important;
  color: #FAD02C !important;
  border: 1px solid #000;
}

:deep(.tnt-pagination.el-pagination.is-background .el-pager li) {
  background-color: #FFF;
  color: #000;
  border: 1px solid #000;
  border-radius: 0; /* 直角 */
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