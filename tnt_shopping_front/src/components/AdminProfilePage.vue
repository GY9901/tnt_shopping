<template>
  <div class="profile-layout">
    <!-- 左侧导航栏 -->
    <div class="side-nav">
      <div class="user-brief">
        <div class="avatar">ADM</div>
        <div class="username">Administrator</div>
        <div class="role">SYSTEM ADMIN</div>
      </div>

      <div class="nav-menu">
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'user' }"
            @click="handleMenuSelect('user')"
        >
          <i class="el-icon-user-solid"></i>
          <span>个人用户信息 / USERS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'product' }"
            @click="handleMenuSelect('product')"
        >
          <i class="el-icon-s-goods"></i>
          <span>商品信息管理 / PRODUCTS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'order' }"
            @click="handleMenuSelect('order')"
        >
          <i class="el-icon-s-order"></i>
          <span>订单管理 / ORDERS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'comment' }"
            @click="handleMenuSelect('comment')"
        >
          <i class="el-icon-chat-dot-square"></i>
          <span>评论管理 / COMMENTS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'about' }"
            @click="handleMenuSelect('about')"
        >
          <i class="el-icon-info-filled"></i>
          <span>关于网站 / ABOUT</span>
        </div>

        <div class="nav-spacer"></div>
      </div>
    </div>

    <!-- 右侧主体内容 -->
    <div class="main-content">

      <!-- 用户管理模块 -->
      <div v-if="activeMenu === 'user'" class="content-section">
        <div class="header-row">
          <h2 class="section-title">USER MANAGEMENT / 用户列表</h2>
        </div>

        <!-- 搜索栏 -->
        <div class="search-panel">
          <div class="search-actions">
            <button class="tnt-btn primary" @click="addUser">
              <i class="el-icon-plus"></i> ADD USER
            </button>
          </div>
          <div class="search-item">
            <span class="search-label">USERNAME:</span>
            <!-- 注意：在 Vue 2 中监听组件根元素原生事件需要  修饰符 -->
            <el-input
                v-model="searchUsername"
                placeholder="请输入用户名搜索..."
                class="tnt-input"
                @keyup.enter="loadUsers"
                clearable
                @clear="loadUsers">
            </el-input>
          </div>
          <div class="search-actions">
            <button class="tnt-btn primary" @click="loadUsers">
              <i class="el-icon-search"></i> SEARCH
            </button>
            <button class="tnt-btn" @click="resetSearch">
              <i class="el-icon-refresh"></i> RESET
            </button>
          </div>
        </div>

        <div class="table-container">
          <el-table
              :data="userList"
              style="width: 100%"
              header-row-class-name="tnt-table-header"
              row-class-name="tnt-table-row"
              v-loading="loading">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="USERNAME"></el-table-column>
            <el-table-column prop="phone" label="PHONE"></el-table-column>
            <el-table-column label="ROLE" width="140">
              <template v-slot="scope">
                <span :class="scope.row.role === 'admin' ? 'role-tag admin' : 'role-tag user'">
                  {{ scope.row.role === 'admin' ? 'ADMIN' : 'USER' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="ACTION" width="200">
              <template v-slot="scope">
                <!-- 核心修改：如果是 admin，则不显示操作按钮 -->
                <div v-if="scope.row.role !== 'admin'">
                  <button class="tnt-btn-sm" @click="editUser(scope.row)">EDIT</button>
                  <button class="tnt-btn-sm danger" @click="deleteUser(scope.row)">DEL</button>
                </div>
                <div v-else>
                  <span style="color: #999; font-weight: bold; font-size: 12px; letter-spacing: 1px;">
                    <i class="el-icon-lock"></i> PROTECTED
                  </span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <div style="padding: 20px; text-align: right;">
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
      </div>

      <!-- 商品管理模块 -->
      <div v-if="activeMenu === 'product'" class="content-section">
        <div class="header-row">
          <h2 class="section-title">PRODUCT LIST / 商品列表</h2>
        </div>
        
        <!-- 搜索栏 -->
        <div class="search-panel">
          <div class="search-actions">
            <button class="tnt-btn primary" @click="addProduct">
              <i class="el-icon-plus"></i> NEW PRODUCT
            </button>
          </div>
          <div class="search-item">
            <span class="search-label">PRODUCT NAME:</span>
            <el-input
                v-model="searchProductName"
                placeholder="请输入商品名称搜索..."
                class="tnt-input"
                @keyup.enter="searchProducts"
                clearable
                @clear="searchProducts">
            </el-input>
          </div>
          <div class="search-actions">
            <button class="tnt-btn primary" @click="searchProducts">
              <i class="el-icon-search"></i> SEARCH
            </button>
            <button class="tnt-btn" @click="resetProductSearch">
              <i class="el-icon-refresh"></i> RESET
            </button>
          </div>
        </div>

        <div class="table-container">
          <el-table
              :data="productList"
              style="width: 100%"
              header-row-class-name="tnt-table-header"
              row-class-name="tnt-table-row">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column label="IMAGE" width="120">
              <template v-slot="scope">
                <div class="img-wrapper">
                  <img v-if="scope.row && scope.row.imageUrl" :src="scope.row.imageUrl" alt="prod">
                  <img v-else src="" alt="prod">
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="NAME"></el-table-column>
            <el-table-column prop="price" label="PRICE" width="120">
              <template v-slot="scope">¥ {{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="STOCK" width="100"></el-table-column>
            <el-table-column prop="category" label="CATEGORY" width="150"></el-table-column>
            <el-table-column label="ACTION" width="200">
              <template v-slot="scope">
                <button class="tnt-btn-sm" @click="editProduct(scope.row)">EDIT</button>
                <button class="tnt-btn-sm danger" @click="deleteProduct(scope.row)">DEL</button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 商品分页组件 -->
          <div style="padding: 20px; text-align: right;">
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
      </div>

      <!-- 订单管理模块 -->
      <div v-if="activeMenu === 'order'" class="content-section">
        <div class="header-row">
          <h2 class="section-title">ORDER MANAGEMENT / 订单管理</h2>
        </div>
        
        <!-- 搜索栏 -->
        <div class="search-panel">
          <div class="search-item">
            <span class="search-label">USER NAME:</span>
            <el-input
                v-model="username"
                placeholder="请输入用户名称搜索..."
                class="tnt-input"
                @keyup.enter="searchOrders"
                clearable
                @clear="searchOrders">
            </el-input>
          </div>
          <div class="search-actions">
            <button class="tnt-btn primary" @click="searchOrders">
              <i class="el-icon-search"></i> SEARCH
            </button>
            <button class="tnt-btn" @click="resetOrderSearch">
              <i class="el-icon-refresh"></i> RESET
            </button>
          </div>
        </div>

        <div class="table-container">
          <el-table
              :data="orderList"
              style="width: 100%"
              header-row-class-name="tnt-table-header"
              row-class-name="tnt-table-row"
              v-loading="loading">
            <el-table-column prop="id" label="ID" width="50"></el-table-column>
            <el-table-column prop="orderNo" label="ORDER NO" width="150"></el-table-column>
            <el-table-column prop="username" label="USER" width="80"></el-table-column>
            <el-table-column prop="paymentMethod" label="PAY METHOD" width="80"></el-table-column>
            <el-table-column prop="totalAmount" label="TOTAL PRICE" width="120">
              <template v-slot="scope">¥ {{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="ORDER TIME" width="120">
              <template v-slot="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="STATUS" width="120">
              <template v-slot="scope">
                <template v-if="scope.row.status === 'COMPLETED'">
                  <span class="status-tag completed">完成</span>
                </template>
                <template v-else>
                  <button class="tnt-btn-sm" @click="shipOrder(scope.row)">发货</button>
                </template>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template v-slot="scope">
                <button class="tnt-btn-sm danger" @click="deleteOrder(scope.row)">删除</button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 订单分页组件 -->
          <div style="padding: 20px; text-align: right;">
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
      </div>

      <!-- 评论管理模块 -->
      <div v-if="activeMenu === 'comment'" class="content-section">
        <div class="header-row">
          <h2 class="section-title">COMMENT MANAGEMENT / 评论管理</h2>
        </div>
        
        <!-- 搜索栏 -->
        <div class="search-panel">
          <div class="search-item">
            <span class="search-label">USER NAME:</span>
            <el-input
                v-model="searchCommentUsername"
                placeholder="请输入用户名称搜索..."
                class="tnt-input"
                @keyup.enter="searchComments"
                clearable
                @clear="searchComments">
            </el-input>
          </div>
          <div class="search-actions">
            <button class="tnt-btn primary" @click="searchComments">
              <i class="el-icon-search"></i> SEARCH
            </button>
            <button class="tnt-btn" @click="resetCommentSearch">
              <i class="el-icon-refresh"></i> RESET
            </button>
          </div>
        </div>

        <div class="table-container">
          <el-table
              :data="commentList"
              style="width: 100%"
              header-row-class-name="tnt-table-header"
              row-class-name="tnt-table-row"
              v-loading="loading">
            <el-table-column prop="id" label="ID" width="50"></el-table-column>
            <el-table-column prop="username" label="USER" width="100"></el-table-column>
            <el-table-column prop="content" label="COMMENT CONTENT" min-width="300"></el-table-column>
            <el-table-column prop="createTime" label="COMMENT TIME" width="150">
              <template v-slot="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template v-slot="scope">
                <button class="tnt-btn-sm danger" @click="deleteComment(scope.row)">删除</button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 评论分页组件 -->
          <div style="padding: 20px; text-align: right;">
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
      </div>

      <!-- 关于模块 -->
      <div v-if="activeMenu === 'about'" class="content-section">
        <h2 class="section-title">ABOUT TNT SPACE</h2>
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

    <!-- 用户编辑/新增弹窗 -->
    <el-dialog
        :title="isAddingUser ? 'ADD USER INFO' : 'EDIT USER INFO'"
        v-model="userDialogVisible"
        width="400px"
        custom-class="tnt-dialog">
      <el-form :model="userForm" label-width="100px" label-position="left">
        <el-form-item label="USERNAME" required>
          <el-input v-model="userForm.username" :disabled="!isAddingUser" placeholder="输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="PASSWORD" required>
          <el-input v-model="userForm.password" show-password placeholder="输入密码"></el-input>
        </el-form-item>
        <el-form-item label="PHONE">
          <el-input v-model="userForm.phone" placeholder="输入电话"></el-input>
        </el-form-item>
        <!-- 不允许修改角色 -->
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="tnt-btn-sm" @click="userDialogVisible = false">CANCEL</button>
          <button class="tnt-btn-sm primary" @click="saveUser">{{ isAddingUser ? 'ADD' : 'SAVE' }}</button>
        </div>
      </template>
    </el-dialog>

    <!-- 商品编辑弹窗 -->
    <el-dialog
        :title="productDialogTitle.toUpperCase()"
        v-model="productDialogVisible"
        width="500px"
        custom-class="tnt-dialog">
      <el-form :model="productForm" label-width="100px" label-position="left">
        <el-form-item label="NAME">
          <el-input v-model="productForm.name"></el-input>
        </el-form-item>
        <el-form-item label="IMAGE URL">
          <el-input v-model="productForm.imageUrl"></el-input>
        </el-form-item>
        <el-form-item label="PRICE">
          <el-input v-model="productForm.price" type="number"></el-input>
        </el-form-item>
        <el-form-item label="STOCK">
          <el-input v-model="productForm.stock" type="number"></el-input>
        </el-form-item>
        <el-form-item label="CATEGORY">
          <el-select v-model="productForm.category" placeholder="选择分类">
            <el-option label="棉花娃娃" value="棉花娃娃"></el-option>
            <el-option label="小卡" value="小卡"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="tnt-btn-sm" @click="productDialogVisible = false">CANCEL</button>
          <button class="tnt-btn-sm primary" @click="saveProduct">SAVE</button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script>
// 引入 axios
import axios from 'axios'

export default {
  name: 'AdminProfilePage',
  data() {
    return {
      activeMenu: 'user',
      loading: false,

      // 用户数据与分页
      userList: [],
      searchUsername: '',
      currentPage: 1,
      pageSize: 6,
      total: 0,

      // 商品数据
      productList: [],
      searchProductName: '',
      
      // 订单数据
      orderList: [],
      username: '',
      
      // 评论数据
      commentList: [],
      searchCommentUsername: '',

      // 弹窗控制
      userDialogVisible: false,
      productDialogVisible: false,
      productDialogTitle: '新增商品',
      userForm: { username: '', password: '', phone: '' },
      productForm: { name: '', imageUrl: '', price: 0, stock: 0, category: '', description: '' },
      isAddingUser: false
    }
  },
  created() {
    this.loadUsers();
  },
  beforeUnmount() {
    // 组件卸载前清理所有响应式数据
    this.userForm = { username: '', password: '', phone: '' };
    this.productForm = { name: '', imageUrl: '', price: 0, stock: 0, category: '', description: '' };
    this.userList = [];
    this.productList = [];
  },
  methods: {
    handleMenuSelect(index) {
      // 关闭所有弹窗并重置表单数据
      this.userDialogVisible = false;
      this.productDialogVisible = false;
      this.userForm = { username: '', password: '', phone: '' };
      this.productForm = { name: '', imageUrl: '', price: 0, stock: 0, category: '', description: '' };
      
      this.activeMenu = index;
      if (index === 'user') this.loadUsers();
      if (index === 'product') this.loadProducts();
      if (index === 'order') this.loadOrders();
      if (index === 'comment') this.loadComments();
    },
    handlePageChange(page) {
      this.currentPage = page;
      // 根据当前激活的菜单选择加载不同的数据
      if (this.activeMenu === 'user') {
        this.loadUsers();
      } else if (this.activeMenu === 'product') {
        this.loadProducts();
      } else if (this.activeMenu === 'order') {
        this.loadOrders();
      }
    },

    // 重置搜索
    resetSearch() {
      this.searchUsername = '';
      this.currentPage = 1;
      this.loadUsers();
    },
    // 商品搜索
    searchProducts() {
      this.currentPage = 1;
      this.loadProducts(this.searchProductName);
    },
    // 重置商品搜索
    resetProductSearch() {
      this.searchProductName = '';
      this.currentPage = 1;
      this.loadProducts('');
    },
    // 加载订单列表
    async loadOrders() {
      this.loading = true;
      try {
        const res = await axios.get('http://localhost:9090/order/list', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            username: this.username
          }
        });

        if (res.data.code === '200') {
          this.orderList = res.data.data.list;
          this.total = res.data.data.total;
        } else {
          this.$message.error('获取订单列表失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      } finally {
        this.loading = false;
      }
    },
    // 订单搜索
    searchOrders() {
      this.currentPage = 1;
      this.loadOrders();
    },
    // 重置订单搜索
    resetOrderSearch() {
      this.username = '';
      this.currentPage = 1;
      this.loadOrders();
    },
    
    // 加载评论列表
    async loadComments() {
      this.loading = true;
      try {
        const res = await axios.get('http://localhost:9090/message/admin/list', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            username: this.searchCommentUsername
          }
        });

        if (res.data.code === '200') {
          this.commentList = res.data.data.list;
          this.total = res.data.data.total;
        } else {
          this.$message.error('获取评论列表失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      } finally {
        this.loading = false;
      }
    },
    
    // 评论搜索
    searchComments() {
      this.currentPage = 1;
      this.loadComments();
    },
    
    // 重置评论搜索
    resetCommentSearch() {
      this.searchCommentUsername = '';
      this.currentPage = 1;
      this.loadComments();
    },
    
    // 删除评论
    async deleteComment(comment) {
      try {
        // 提示确认删除
        await this.$confirm('确定要删除这个评论吗？', '删除确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        // 调用后端API删除评论
        const res = await axios.delete(`http://localhost:9090/message/admin/delete/${comment.id}`);

        if (res.data.code === '200') {
          // 显示删除成功提示
          this.$message.success('删除成功');
          
          // 从本地评论列表中移除
          this.commentList = this.commentList.filter(item => item.id !== comment.id);
          this.total--;
        } else {
          this.$message.error('删除失败');
        }
      } catch (e) {
        if (e === 'cancel') return; // 用户取消删除操作
        console.error(e);
        this.$message.error('网络请求错误');
      }
    },
    // 发货操作
    async shipOrder(order) {
      try {
        // 调用后端API更新订单状态
        const res = await axios.put(`http://localhost:9090/order/admin/updateStatus`, {
          id: order.id,
          status: 'COMPLETED'
        });

        if (res.data.code === '200') {
          // 显示发货成功提示
          this.$message.success('发货成功');
          
          // 更新本地订单列表中的状态
          const index = this.orderList.findIndex(item => item.id === order.id);
          if (index !== -1) {
            this.orderList.splice(index, 1, { ...order, status: 'COMPLETED' });
          }
        } else {
          this.$message.error('发货失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      }
    },
    // 删除订单
    async deleteOrder(order) {
      try {
        // 提示确认删除
        await this.$confirm('确定要删除这个订单吗？', '删除确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        // 调用后端API删除订单
        const res = await axios.delete(`http://localhost:9090/order/admin/delete/${order.id}`);

        if (res.data.code === '200') {
          // 显示删除成功提示
          this.$message.success('删除成功');
          
          // 从本地订单列表中移除
          this.orderList = this.orderList.filter(item => item.id !== order.id);
          this.total--;
        } else {
          this.$message.error('删除失败');
        }
      } catch (e) {
        if (e === 'cancel') return; // 用户取消删除操作
        console.error(e);
        this.$message.error('网络请求错误');
      }
    },

    // --- 用户请求逻辑 ---
    async loadUsers() {
      this.loading = true;
      try {
        const res = await axios.get('http://localhost:9090/user/all', {
          params: {
            username: this.searchUsername,
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        });

        if (res.data.code === '200') {
          this.userList = res.data.data.list;
          this.total = res.data.data.total;
        } else {
          this.$message.error(res.data.msg || '加载失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      } finally {
        this.loading = false;
      }
    },

    async editUser(row) {
      // 先通过ID查询最新的用户信息
      try {
        const res = await axios.get(`http://localhost:9090/user/${row.id}`);
        if (res.data.code === '200') {
          this.userForm = res.data.data;
          this.isAddingUser = false;
          this.userDialogVisible = true;
        } else {
          this.$message.error('获取用户信息失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      }
    },
    formatDate(str) {
      if(!str) return 'Loading...'
      const date = new Date(str)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    addUser() {
      // 新增用户
      this.userForm = {
        // role 字段会在API中自动设置为user
      };
      this.isAddingUser = true;
      this.userDialogVisible = true;
    },
    deleteUser(row) {
      this.$confirm('确定删除该用户吗？', 'WARNING', {
        confirmButtonText: 'CONFIRM',
        cancelButtonText: 'CANCEL',
        type: 'warning',
        customClass: 'tnt-message-box'
      }).then(() => {
        // 确认删除后发起请求
        axios.delete(`http://localhost:9090/user/delete/${row.id}`)
            .then(res => {
              if (res.data.code === '200') {
                this.$message.success('已删除');
                this.loadUsers();
              } else {
                this.$message.error('删除失败');
              }
            })
            .catch(err => {
              console.error(err);
              this.$message.error('删除请求出错');
            });
      }).catch(() => {
        // 捕获“取消”操作，防止抛出 "cancel" 错误
        this.$message.info('已取消删除');
      });
    },
    saveUser() {
      // 根据是否是新增用户选择不同的API
      const url = this.isAddingUser ? 'http://localhost:9090/user/register' : 'http://localhost:9090/user/admin/update';
      const method = this.isAddingUser ? 'post' : 'put';
      
      axios[method](url, this.userForm)
          .then(res => {
            if (res.data.code === '200') {
              this.$message.success(this.isAddingUser ? '添加成功' : '保存成功');
              this.userDialogVisible = false;
              this.loadUsers();
            } else {
              this.$message.error(res.data.msg || (this.isAddingUser ? '添加失败' : '保存失败'));
            }
          })
          .catch(e => {
            console.error(e);
            this.$message.error(this.isAddingUser ? '添加请求出错' : '保存请求出错');
          });
    },

    // --- 商品逻辑 ---
    async loadProducts(searchName = this.searchProductName) {
      this.loading = true;
      try {
        const res = await axios.get('http://localhost:9090/product/list', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            name: searchName
          }
        });

        if (res.data.code === '200') {
          this.productList = res.data.data.list;
          this.total = res.data.data.total;
        } else {
          this.$message.error('获取商品列表失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      } finally {
        this.loading = false;
      }
    },
    addProduct() {
      this.productDialogTitle = 'New Product';
      this.productForm = { name: '', img: '', price: 0, stock: 0, category: '', description: '' };
      this.productDialogVisible = true;
    },
    async editProduct(row) {
      // 先通过ID查询最新的商品信息
      try {
        const res = await axios.get(`http://localhost:9090/product/${row.id}`);
        if (res.data.code === '200') {
          this.productDialogTitle = 'Edit Product';
          this.productForm = res.data.data;
          this.productDialogVisible = true;
        } else {
          this.$message.error('获取商品信息失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('网络请求错误');
      }
    },
    deleteProduct(row) {
      this.$confirm('确定删除该商品吗？', 'WARNING', {
        confirmButtonText: 'CONFIRM',
        cancelButtonText: 'CANCEL',
        type: 'warning',
        customClass: 'tnt-message-box'
      }).then(() => {
        axios.delete(`http://localhost:9090/product/delete/${row.id}`)
            .then(res => {
              if (res.data.code === '200') {
                this.$message.success('已删除');
                this.loadProducts();
              } else {
                this.$message.error('删除失败');
              }
            })
            .catch(e => {
              console.error(e);
              this.$message.error('该商品已经加入其他用户购物车,禁止删除');
            });
      }).catch(() => {
        // 捕获“取消”操作
        this.$message.info('已取消删除');
      });
    },
    saveProduct() {
      const url = this.productForm.id ? 'http://localhost:9090/product/admin/update' : 'http://localhost:9090/product/add';
      const method = this.productForm.id ? 'put' : 'post';
      axios[method](url, this.productForm)
          .then(res => {
            if (res.data.code === '200') {
              this.$message.success('保存成功');
              this.productDialogVisible = false;
              this.loadProducts();
            } else {
              this.$message.error(res.data.msg || '保存失败');
            }
          })
          .catch(e => {
            console.error(e);
            this.$message.error('保存请求出错');
          });
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Anton&family=Archivo+Black&family=Roboto+Mono:wght@400;700&display=swap');

/* --- 全局布局 --- */
.profile-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  font-family: 'Roboto Mono', sans-serif;
  /* 整体背景改为浅黄色 */
  background-color: #FFFBE6;
  overflow: hidden;
}

/* --- 左侧导航栏 --- */
.side-nav {
  width: 280px;
  background: #111; /* 保持深色侧边栏形成对比 */
  color: #FFF;
  display: flex;
  flex-direction: column;
  padding: 30px 0;
  flex-shrink: 0;
  box-shadow: 4px 0 15px rgba(0,0,0,0.1);
  z-index: 10;
}

.user-brief {
  padding: 0 30px;
  margin-bottom: 50px;
  text-align: center;
}

.avatar {
  width: 80px;
  height: 80px;
  background: #FFF;
  color: #000;
  border-radius: 50%;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-family: 'Anton', sans-serif;
  border: 4px solid #FAD02C;
}

.username {
  font-family: 'Anton', sans-serif;
  font-size: 20px;
  letter-spacing: 1px;
  margin-bottom: 5px;
}

.role {
  font-size: 12px;
  color: #FAD02C;
  font-weight: bold;
  letter-spacing: 2px;
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.nav-item {
  padding: 18px 30px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  font-weight: bold;
  font-size: 14px;
  border-left: 5px solid transparent;
}

.nav-item i {
  margin-right: 15px;
  font-size: 18px;
}

.nav-item:hover {
  background: #222;
  color: #FAD02C;
}

.nav-item.active {
  background: #FAD02C;
  color: #000;
  border-left-color: #FFF;
}

.nav-spacer {
  flex: 1;
}

.logout-btn {
  margin-top: auto;
  border-top: 1px solid #333;
  color: #AAA;
}
.logout-btn:hover {
  background: #333;
  color: #FFF;
}

/* --- 右侧主体内容 --- */
.main-content {
  flex: 1;
  /* 内容区背景透明，显示底层浅黄 */
  background: transparent;
  padding: 40px;
  overflow-y: auto;
}

.content-section {
  max-width: 1200px;
  margin: 0 auto;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-title {
  font-family: 'Anton', sans-serif;
  font-size: 42px;
  margin: 0;
  position: relative;
  display: inline-block;
  text-transform: uppercase;
  color: #000;
}

.section-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 5px;
  width: 100%;
  height: 10px;
  background: rgba(250, 208, 44, 0.4);
  z-index: -1;
}

/* --- 搜索栏优化 --- */
.search-panel {
  background: #FFF;
  border: 2px solid #000;
  padding: 20px;
  margin-bottom: 25px;
  display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20px;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
}

.search-item {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 500px;
}

.search-label {
  font-weight: bold;
  margin-right: 15px;
  font-size: 14px;
  white-space: nowrap;
}

.tnt-input {
  width: 100%;
}

.search-actions {
  display: flex;
  gap: 15px;
}

/* --- 表格样式 --- */
.table-container {
  background: #FFF;
  border: 2px solid #000;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
  padding: 20px;
}

.role-tag {
  padding: 4px 8px;
  font-weight: bold;
  font-size: 12px;
  text-transform: uppercase;
  display: inline-block;
}
.role-tag.admin {
  background: #000;
  color: #FAD02C;
}
.role-tag.user {
  background: #F0F0F0;
  color: #555;
  border: 1px solid #CCC;
}

.img-wrapper {
  width: 60px;
  height: 60px;
  border: 2px solid #EEE;
  overflow: hidden;
  background: #FFF;
}
.img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* --- 按钮样式 --- */
.tnt-btn {
  border: 2px solid #000;
  background: #FFF;
  color: #000;
  padding: 10px 24px;
  font-weight: bold;
  font-family: 'Roboto Mono', sans-serif;
  cursor: pointer;
  transition: all 0.1s;
  text-transform: uppercase;
  outline: none;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tnt-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 4px 4px 0 rgba(0,0,0,0.2);
}
.tnt-btn:active {
  transform: translate(0, 0);
  box-shadow: none;
}
.tnt-btn.primary {
  background: #FAD02C;
}

.tnt-btn-sm {
  border: 1px solid #000;
  background: #FFF;
  color: #000;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  margin-right: 8px;
  text-transform: uppercase;
  transition: all 0.1s;
}
.tnt-btn-sm:hover {
  background: #000;
  color: #FFF;
}
.tnt-btn-sm.danger {
  color: #D32F2F;
  border-color: #D32F2F;
}
.tnt-btn-sm.danger:hover {
  background: #D32F2F;
  color: #FFF;
}

/* --- Element UI 样式覆盖 --- */
::v-deep .el-table th {
  background-color: #FFFBE6 !important; /* 表头浅黄色 */
  color: #000;
  font-family: 'Anton', sans-serif;
  text-transform: uppercase;
  font-weight: normal;
  border-bottom: 2px solid #000;
  padding: 12px 0;
}
::v-deep .el-table td {
  border-bottom: 1px solid #EEE;
  padding: 12px 0;
  font-weight: 500;
}

/* Input 覆盖 */
::v-deep .el-input__inner {
  border-radius: 0;
  border: 2px solid #DDD;
  font-family: 'Roboto Mono', monospace;
  height: 40px;
  line-height: 40px;
}
::v-deep .el-input__inner:focus {
  border-color: #000;
}

/* 订单状态标签 */
.status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}
.status-tag.pending {
  background-color: #FFF3CD;
  color: #856404;
  border: 1px solid #FFEEBA;
}
.status-tag.completed {
  background-color: #D4EDDA;
  color: #155724;
  border: 1px solid #C3E6CB;
}

/* 分页覆盖 */
::v-deep .tnt-pagination .el-pager li {
  background: #FFF;
  border: 1px solid #DDD;
  font-weight: bold;
  border-radius: 0;
}
::v-deep .tnt-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: #FAD02C;
  color: #000;
  border-color: #000;
}

/* 关于界面样式 */
.about-content {
  font-size: 15px;
  line-height: 1.8;
  padding: 20px;
  background: #FFF;
  border: 2px solid #000;
  margin-top: 20px;
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
  background: #FAFAFA;
}
.tech-stack h4 {
  margin-top: 0;
  font-family: 'Anton';
}
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
  text-align: center;
}

/* 弹窗覆盖 */
::v-deep .tnt-dialog .el-dialog__header {
  background: #FFFBE6;
  padding: 20px;
  border-bottom: 2px solid #000;
}
::v-deep .tnt-dialog .el-dialog__title {
  color: #000;
  font-family: 'Anton', sans-serif;
  font-size: 24px;
}
::v-deep .tnt-dialog .el-dialog__close {
  color: #000;
  font-weight: bold;
}
::v-deep .tnt-dialog .el-dialog__body {
  padding: 30px;
  background: #FFF;
}
::v-deep .tnt-dialog {
  border-radius: 0;
  border: 3px solid #000;
  box-shadow: 10px 10px 0 rgba(0,0,0,0.2);
}
</style>