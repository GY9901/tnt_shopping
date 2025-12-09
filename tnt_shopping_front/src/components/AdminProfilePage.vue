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
          <span>用户管理 / USERS</span>
        </div>
        <div
            class="nav-item"
            :class="{ active: activeMenu === 'product' }"
            @click="handleMenuSelect('product')"
        >
          <i class="el-icon-s-goods"></i>
          <span>商品管理 / PRODUCTS</span>
        </div>

        <div class="nav-spacer"></div>

        <div class="nav-item logout-btn" @click="goHome">
          <i class="el-icon-back"></i>
          <span>返回商城 / BACK</span>
        </div>
      </div>
    </div>

    <!-- 右侧主体内容 -->
    <div class="main-content">

      <!-- 用户管理模块 -->
      <div v-if="activeMenu === 'user'" class="content-section">
        <div class="header-row">
          <h2 class="section-title">USER MANAGEMENT / 用户管理</h2>
        </div>

        <!-- 搜索栏：优化布局，增加间距和样式 -->
        <div class="search-panel">
          <div class="search-item">
            <span class="search-label">USERNAME:</span>
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
                <button class="tnt-btn-sm" @click="editUser(scope.row)">EDIT</button>
                <button class="tnt-btn-sm danger" @click="deleteUser(scope.row)">DEL</button>
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
          <button class="tnt-btn primary" @click="addProduct">
            <i class="el-icon-plus"></i> NEW PRODUCT
          </button>
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
                  <img :src="scope.row.img" alt="prod">
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="NAME"></el-table-column>
            <el-table-column prop="price" label="PRICE" width="120">
              <template v-slot="scope">¥ {{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="STOCK" width="100"></el-table-column>
            <el-table-column label="ACTION" width="200">
              <template v-slot="scope">
                <button class="tnt-btn-sm" @click="editProduct(scope.row)">EDIT</button>
                <button class="tnt-btn-sm danger" @click="deleteProduct(scope.row)">DEL</button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

    </div>

    <!-- 用户编辑弹窗 -->
    <el-dialog
        title="EDIT USER INFO"
        :visible="userDialogVisible"
        @update:visible="userDialogVisible = $event"
        width="400px"
        custom-class="tnt-dialog">
      <el-form :model="userForm" label-width="100px" label-position="left">
        <el-form-item label="USERNAME">
          <el-input v-model="userForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="PASSWORD">
          <el-input v-model="userForm.password" show-password></el-input>
        </el-form-item>
        <el-form-item label="EMAIL">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        <el-form-item label="ROLE">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="ADMIN" value="admin"></el-option>
            <el-option label="USER" value="user"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="tnt-btn-sm" @click="userDialogVisible = false">CANCEL</button>
          <button class="tnt-btn-sm primary" @click="saveUser">SAVE</button>
        </div>
      </template>
    </el-dialog>

    <!-- 商品编辑弹窗 -->
    <el-dialog
        :title="productDialogTitle.toUpperCase()"
        :visible="productDialogVisible"
        @update:visible="productDialogVisible = $event"
        width="500px"
        custom-class="tnt-dialog">
      <el-form :model="productForm" label-width="100px" label-position="left">
        <el-form-item label="NAME">
          <el-input v-model="productForm.name"></el-input>
        </el-form-item>
        <el-form-item label="IMAGE URL">
          <el-input v-model="productForm.img"></el-input>
        </el-form-item>
        <el-form-item label="PRICE">
          <el-input v-model="productForm.price" type="number"></el-input>
        </el-form-item>
        <el-form-item label="STOCK">
          <el-input v-model="productForm.stock" type="number"></el-input>
        </el-form-item>
        <el-form-item label="CATEGORY">
          <el-input v-model="productForm.category"></el-input>
        </el-form-item>
        <el-form-item label="DESC">
          <el-input type="textarea" :rows="3" v-model="productForm.description"></el-input>
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
      pageSize: 10,
      total: 0,

      // 商品数据
      productList: [],

      // 弹窗控制
      userDialogVisible: false,
      productDialogVisible: false,
      productDialogTitle: '新增商品',
      userForm: {},
      productForm: {}
    }
  },
  created() {
    this.loadUsers();
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index;
      if (index === 'user') this.loadUsers();
      if (index === 'product') this.loadProducts();
    },
    goHome() {
      this.$router.push('/');
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.loadUsers();
    },

    // 重置搜索
    resetSearch() {
      this.searchUsername = '';
      this.currentPage = 1;
      this.loadUsers();
    },

    // --- 用户请求逻辑 ---
    async loadUsers() {
      this.loading = true;
      try {
        // 使用 axios 直接发送请求，不需要 proxy 或 getCurrentInstance
        const res = await axios.get('http://localhost:9090/user/all', {
          params: {
            username: this.searchUsername, // 搜索关键词
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        });

        if (res.data.code === '200') {
          // 适配 Spring Data JPA 返回的分页结构
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

    editUser(row) {
      this.userForm = JSON.parse(JSON.stringify(row));
      this.userDialogVisible = true;
    },
    deleteUser(row) {
      this.$confirm('确定删除该用户?', 'WARNING', {
        confirmButtonText: 'CONFIRM',
        cancelButtonText: 'CANCEL',
        type: 'warning',
        customClass: 'tnt-message-box'
      }).then(() => {
        axios.delete(`http://localhost:9090/user/delete/${row.id}`).then(res => {
          if (res.data.code === '0') {
            this.$message.success('已删除 / DELETED');
            this.loadUsers();
          }
        });
      }).catch(() => {
        // 捕获“取消”操作，防止抛出 "cancel" 错误
        this.$message.info('已取消删除');
      });
    },
    saveUser() {
      axios.put('http://localhost:9090/user/admin/update', this.userForm).then(res => {
        if (res.data.code === '0') {
          this.$message.success('保存成功 / SAVED');
          this.userDialogVisible = false;
          this.loadUsers();
        }
      });
    },

    // --- 商品逻辑 ---
    loadProducts() {
      axios.get('http://localhost:9090/product/list').then(res => {
        if (res.data.code === '0') this.productList = res.data.data;
      });
    },
    addProduct() {
      this.productDialogTitle = 'New Product';
      this.productForm = {};
      this.productDialogVisible = true;
    },
    editProduct(row) {
      this.productDialogTitle = 'Edit Product';
      this.productForm = JSON.parse(JSON.stringify(row));
      this.productDialogVisible = true;
    },
    deleteProduct(row) {
      this.$confirm('确定删除该商品吗？/ DELETE THIS PRODUCT?', 'WARNING', {
        confirmButtonText: 'CONFIRM',
        cancelButtonText: 'CANCEL',
        type: 'warning',
        customClass: 'tnt-message-box'
      }).then(() => {
        axios.delete(`http://localhost:9090/product/delete/${row.id}`).then(res => {
          if (res.data.code === '0') {
            this.$message.success('已删除 / DELETED');
            this.loadProducts();
          }
        });
      });
    },
    saveProduct() {
      const url = this.productForm.id ? 'http://localhost:9090/product/update' : 'http://localhost:9090/product/add';
      const method = this.productForm.id ? 'put' : 'post';
      axios[method](url, this.productForm).then(res => {
        if (res.data.code === '0') {
          this.$message.success('保存成功 / SAVED');
          this.productDialogVisible = false;
          this.loadProducts();
        }
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