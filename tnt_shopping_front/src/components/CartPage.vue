<template>
  <div class="cart-wrapper">
    <!-- 头部标题 -->
    <div class="cart-header">
      <h2>MY SHOPPING CART</h2>
      <span class="count-badge">{{ cartList.length }} ITEMS</span>
    </div>

    <!-- 购物车列表 -->
    <div class="cart-list" v-loading="loading">

      <!-- 空状态 -->
      <div v-if="!loading && cartList.length === 0" class="empty-cart">
        <div class="empty-text">YOUR CART IS EMPTY</div>
        <div class="empty-sub">购物车空空如也，快去选购吧</div>
      </div>

      <!-- 商品行 -->
      <div
          class="cart-item"
          v-for="(item) in cartList"
          :key="item.id"
          :class="{ 'selected': selection.includes(item.id) }"
      >
        <!-- 1. 勾选框 -->
        <div class="checkbox-area" @click="toggleSelect(item.id)">
          <div class="custom-checkbox" :class="{ checked: selection.includes(item.id) }">
            <el-icon v-if="selection.includes(item.id)"><Check /></el-icon>
          </div>
        </div>

        <!-- 2. 商品图 -->
        <div class="product-img" :style="getBg(item.product)">
          <span v-if="!item.product?.imageUrl" class="no-img">TNT</span>
        </div>

        <!-- 3. 商品信息 -->
        <div class="product-info">
          <div class="p-name">{{ item.product?.name }}</div>
        </div>

        <div class="product-info">
          <div class="p-sku">规格: 默认</div>
        </div>

        <div class="product-info">
          <div class="p-price">¥ {{ item.product?.price }}</div>
        </div>

        <!-- 4. 数量控制 -->
        <div class="quantity-ctrl">
          <button class="qty-btn" @click="changeQty(item, -1)" :disabled="item.quantity <= 1">-</button>
          <input type="text" class="qty-input" v-model="item.quantity" readonly />
          <button class="qty-btn" @click="changeQty(item, 1)">+</button>
        </div>

        <!-- 5. 删除按钮 -->
        <div class="delete-btn" @click="deleteItem(item.id)">
          <el-icon><Delete /></el-icon>
        </div>
      </div>
    </div>

    <!-- 底部结算栏 (吸底) -->
    <div class="checkout-bar">
      <div class="bar-left" @click="toggleSelectAll">
        <div class="custom-checkbox" :class="{ checked: isAllSelected && cartList.length > 0 }">
          <el-icon v-if="isAllSelected && cartList.length > 0"><Check /></el-icon>
        </div>
        <span class="select-all-text">SELECT ALL</span>
      </div>

      <div class="bar-right">
        <div class="total-info">
          <div class="total-label">TOTAL AMOUNT</div>
          <div class="total-price">
            <span class="symbol">¥</span>
            {{ totalPrice.toFixed(2) }}
          </div>
        </div>
        <button class="checkout-btn" @click="handleCheckout" :disabled="selection.length === 0">
          CHECKOUT ({{ selection.length }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Check } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const cartList = ref([])
const selection = ref([]) // 存储被选中的购物车ID
const currentUser = ref('')

// 获取用户信息
const getUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const u = JSON.parse(userStr)
    currentUser.value = u.username
    fetchCart()
  }
}

// 获取购物车数据
const fetchCart = async () => {
  if (!currentUser.value) return
  loading.value = true
  try {
    const res = await proxy.$request.get('/cart/list', {
      params: { username: currentUser.value }
    })
    if (res.data.code === '200') {
      cartList.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

// 计算总价
const totalPrice = computed(() => {
  let total = 0
  cartList.value.forEach(item => {
    if (selection.value.includes(item.id)) {
      total += item.product.price * item.quantity
    }
  })
  return total
})

// 全选状态
const isAllSelected = computed(() => {
  return cartList.value.length > 0 && selection.value.length === cartList.value.length
})

// 切换单选
const toggleSelect = (id) => {
  const idx = selection.value.indexOf(id)
  if (idx > -1) {
    selection.value.splice(idx, 1)
  } else {
    selection.value.push(id)
  }
}

// 切换全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selection.value = []
  } else {
    selection.value = cartList.value.map(item => item.id)
  }
}

// 修改数量
const changeQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return

  try {
    await proxy.$request.post('/cart/update', {
      id: item.id,
      quantity: newQty
    })
    item.quantity = newQty // 前端更新
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

// 删除商品
const deleteItem = (id) => {
  ElMessageBox.confirm('确定要移除该商品吗?', '提示', {
    confirmButtonText: '移除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await proxy.$request.delete(`/cart/delete/${id}`)
      ElMessage.success('已移除')
      // 从列表中移除
      cartList.value = cartList.value.filter(item => item.id !== id)
      // 从选中状态移除
      const idx = selection.value.indexOf(id)
      if (idx > -1) selection.value.splice(idx, 1)
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

// 结算
const handleCheckout = () => {
  if (selection.value.length === 0) return
  ElMessage.success(`结算成功！共支付 ¥${totalPrice.value.toFixed(2)}`)
  // 这里可以扩展为跳转订单确认页逻辑
}

// 辅助：图片背景
const getBg = (product) => {
  if (product && product.imageUrl) {
    return { backgroundImage: `url(${product.imageUrl})`, backgroundSize: 'cover' }
  }
  return { backgroundColor: '#FAD02C' }
}

onMounted(() => {
  getUser()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Anton&family=Roboto:wght@400;700;900&display=swap');

.cart-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  min-height: 600px;
  padding-bottom: 100px; /* 留出底部结算栏空间 */
}

/* 头部 */
.cart-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  border-bottom: 4px solid #000;
  padding-bottom: 15px;
}

.cart-header h2 {
  font-family: 'Anton', sans-serif;
  font-size: 36px;
  margin: 0;
  letter-spacing: 1px;
}

.count-badge {
  background: #FAD02C;
  color: #000;
  font-weight: bold;
  padding: 2px 8px;
  font-size: 14px;
  border: 2px solid #000;
}

/* 列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.empty-cart {
  text-align: center;
  padding: 80px 0;
  color: #999;
}

.empty-text {
  font-family: 'Anton', sans-serif;
  font-size: 32px;
  color: #DDD;
}

/* 单行商品 */
.cart-item {
  display: flex;
  align-items: center;
  background: #FFF;
  border: 2px solid #DDD;
  padding: 15px;
  transition: all 0.2s;
}

.cart-item.selected {
  border-color: #000;
  background: #FFFCF0;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
}

/* 勾选框区域 */
.checkbox-area {
  margin-right: 20px;
  cursor: pointer;
}

.custom-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #000;
  background: #FFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #000;
}

.custom-checkbox.checked {
  background: #FAD02C;
}

/* 图片 */
.product-img {
  width: 80px;
  height: 80px;
  background-color: #FAD02C;
  border: 2px solid #000;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.no-img {
  font-family: 'Anton', sans-serif;
  font-size: 20px;
  color: rgba(0,0,0,0.1);
  transform: rotate(-15deg);
}

/* 信息 */
.product-info {
  flex: 1;
}

.p-name {
  font-weight: 900;
  font-size: 16px;
  color: #000;
  margin-bottom: 5px;
}

.p-sku {
  font-size: 12px;
  color: #888;
  margin-bottom: 5px;
}

.p-price {
  font-family: 'Anton', sans-serif;
  font-size: 20px;
  color: #000;
}

/* 数量控制 */
.quantity-ctrl {
  display: flex;
  border: 2px solid #000;
  margin-right: 30px;
}

.qty-btn {
  width: 30px;
  height: 30px;
  background: #000;
  color: #FAD02C;
  border: none;
  font-weight: bold;
  font-size: 18px;
  cursor: pointer;
}

.qty-btn:disabled {
  background: #555;
  color: #888;
  cursor: not-allowed;
}

.qty-input {
  width: 40px;
  border: none;
  text-align: center;
  font-weight: bold;
  font-size: 14px;
  outline: none;
}

/* 删除按钮 */
.delete-btn {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 5px;
  transition: color 0.2s;
}

.delete-btn:hover {
  color: #F44336;
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 80px;
  background: #FFF;
  border-top: 4px solid #000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  z-index: 999;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: bold;
}

.select-all-text {
  font-family: 'Anton', sans-serif;
  font-size: 18px;
  letter-spacing: 1px;
}

.bar-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-info {
  text-align: right;
}

.total-label {
  font-size: 12px;
  font-weight: bold;
  color: #888;
}

.total-price {
  font-family: 'Anton', sans-serif;
  font-size: 32px;
  color: #000;
  line-height: 1;
}

.symbol {
  font-size: 20px;
}

.checkout-btn {
  height: 50px;
  padding: 0 40px;
  background: #000;
  color: #FAD02C;
  font-family: 'Anton', sans-serif;
  font-size: 20px;
  letter-spacing: 2px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.checkout-btn:hover:not(:disabled) {
  background: #FAD02C;
  color: #000;
}

.checkout-btn:disabled {
  background: #CCC;
  color: #FFF;
  cursor: not-allowed;
}
</style>