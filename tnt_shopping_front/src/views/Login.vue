<template>
  <div class="page-wrapper">
    <!-- 背景装饰字 (极淡水印) -->
    <div class="bg-decoration">TNT SPACE</div>

    <div class="auth-box">
      <!-- 左侧：视觉冲击区 (黄底 + 纯黑巨字) -->
      <div class="visual-side">
        <div class="brand-block">
          <div class="logo-circle">TNT</div>
          <h1 class="hero-text">
            <div>TEENS</div>
            <div>IN</div>
            <div>TIMES</div>
          </h1>
          <div class="divider"></div>
          <div class="slogan-box">
            <p class="cn">破天下 · 定风云</p>
            <p class="en">OFFICIAL MERCHANDISE</p>
          </div>
        </div>
        <!-- 底部装饰条形码 -->
        <div class="barcode">NO. 1123-TNT-OFFICIAL</div>
      </div>

      <!-- 右侧：表单区 (白底) -->
      <div class="form-side">
        <div class="form-header">
          <div class="tag">TNT SPACE</div>
          <h2>{{ isRegister ? 'CREATE ACCOUNT' : 'WELCOME BACK' }}</h2>
          <p class="subtitle">{{ isRegister ? '加入会员，开启应援之旅' : '登录账号，查看最新周边' }}</p>
        </div>

        <!-- 登录表单 -->
        <el-form
            v-if="!isRegister"
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="heavy-form"
            size="large"
            hide-required-asterisk
        >
          <el-form-item prop="username">
            <div class="label">ACCOUNT / 账号</div>
            <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <div class="label">PASSWORD / 密码</div>
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 登录按钮：默认黑底黄字 -->
          <button class="tnt-btn solid" @click.prevent="handleLogin">
            <span v-if="!loading">登录 / LOGIN</span>
            <span v-else>LOADING...</span>
          </button>
        </el-form>

        <!-- 注册表单 -->
        <el-form
            v-else
            ref="regFormRef"
            :model="regForm"
            :rules="regRules"
            class="heavy-form"
            size="large"
            hide-required-asterisk
        >
          <el-form-item prop="username">
            <div class="label">SET ACCOUNT / 设置账号</div>
            <el-input
                v-model="regForm.username"
                placeholder="设置用户名"
                :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <div class="label">PASSWORD / 设置密码</div>
            <el-input
                v-model="regForm.password"
                type="password"
                placeholder="设置密码"
                show-password
                :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item prop="confirmPass">
            <div class="label">CONFIRM / 确认密码</div>
            <el-input
                v-model="regForm.confirmPass"
                type="password"
                placeholder="再次输入密码"
                show-password
                :prefix-icon="CircleCheck"
            />
          </el-form-item>

          <!-- 注册按钮：默认白底黑框 (线框) -->
          <button class="tnt-btn outline" @click.prevent="handleRegister">
            <span v-if="!loading">注册 / REGISTER</span>
            <span v-else>PROCESSING...</span>
          </button>
        </el-form>

        <div class="footer-switch">
          <span @click="toggleMode">
            {{ isRegister ? '已有账号？返回登录' : '没有账号？立即注册' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const isRegister = ref(false)
const loading = ref(false)
const loginFormRef = ref(null)
const regFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirmPass: '' })

// 基础校验规则
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 密码一致性校验
const validatePass2 = (rule, value, callback) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== regForm.password) callback(new Error('两次密码不一致'))
  else callback()
}

const regRules = {
  ...rules,
  confirmPass: [{ validator: validatePass2, trigger: 'blur' }]
}

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  loginFormRef.value?.resetFields()
  regFormRef.value?.resetFields()
}

// 登录逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await proxy.$request.post('/user/login', loginForm)

        if (res.data.code === '200') {
          ElMessage.success('登录成功 / Welcome')
          localStorage.setItem('user', JSON.stringify(res.data.data))

          setTimeout(() => {
            router.replace('/home')
          }, 500)
        } else {
          ElMessage.error(res.data.msg || '登录失败')
        }
      } catch (error) {
        ElMessage.error('无法连接到服务器')
      } finally {
        loading.value = false
      }
    }
  })
}

// 注册逻辑
const handleRegister = async () => {
  if (!regFormRef.value) return
  await regFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await proxy.$request.post('/user/register', {
          username: regForm.username,
          password: regForm.password
        })
        if (res.data.code === '200') {
          ElMessage.success('注册成功，请登录')
          toggleMode()
        } else {
          ElMessage.error(res.data.msg)
        }
      } catch (error) {
        ElMessage.error('无法连接到服务器')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 引入谷歌字体 */
@import url('https://fonts.googleapis.com/css2?family=Anton&family=Inter:wght@400;700;900&display=swap');

/* --- 核心配色 --- */
:root {
  --bg-color: #FFFDF0;       /* 浅米黄背景 */
  --tnt-yellow: #FAD02C;     /* 爆米花黄 */
  --tnt-black: #000000;      /* 纯黑 */
  --tnt-white: #FFFFFF;
}

.page-wrapper {
  min-height: 100vh;
  background-color: var(--bg-color);
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Inter', sans-serif;
  position: relative;
  overflow: hidden;
  padding: 20px; /* 增加内边距，防止小屏幕贴边 */
}

.bg-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-10deg);
  font-size: 25vw;
  font-weight: 900;
  color: rgba(0, 0, 0, 0.03);
  white-space: nowrap;
  pointer-events: none;
  font-family: 'Anton', sans-serif;
}

.auth-box {
  width: 960px;
  min-height: 580px; /* 改为最小高度，允许内容撑开 */
  background: var(--tnt-white);
  box-shadow: 0 40px 80px rgba(0,0,0,0.15);
  display: flex;
  border: 3px solid var(--tnt-black); /* 粗黑边框 */
  z-index: 10;
  max-width: 100%; /* 防止小于960px屏幕溢出 */
}

/* --- 左侧视觉区 --- */
.visual-side {
  width: 45%;
  background-color: var(--tnt-yellow); /* 黄底 */
  color: var(--tnt-black);             /* 黑字 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.logo-circle {
  width: 50px;
  height: 50px;
  border: 3px solid var(--tnt-black);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  margin-bottom: 30px;
}

.hero-text {
  font-family: 'Anton', sans-serif;
  font-size: 80px;
  line-height: 0.85;
  margin: 0;
}

.hero-text div {
  /* 确保文字紧凑堆叠 */
  display: block;
}

.divider {
  width: 60px;
  height: 8px;
  background: var(--tnt-black);
  margin: 30px 0;
}

.slogan-box .cn {
  font-size: 24px;
  font-weight: 900;
  margin: 0 0 5px 0;
  letter-spacing: 2px;
}

.slogan-box .en {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  opacity: 0.8;
}

.barcode {
  position: absolute;
  bottom: 30px;
  left: 60px;
  font-family: monospace;
  font-weight: bold;
  opacity: 0.6;
}

/* --- 右侧表单区 --- */
.form-side {
  flex: 1;
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background-color: var(--tnt-white);
}

.form-header {
  margin-bottom: 40px;
}

.tag {
  display: inline-block;
  background: var(--tnt-black);
  color: var(--tnt-yellow);
  padding: 4px 10px;
  font-weight: bold;
  font-size: 12px;
  margin-bottom: 15px;
}

.form-header h2 {
  font-family: 'Anton', sans-serif;
  font-size: 40px;
  margin: 0 0 10px 0;
  color: var(--tnt-black);
  letter-spacing: 1px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

/* 表单控件样式 */
.heavy-form .label {
  font-size: 12px;
  font-weight: 800;
  color: var(--tnt-black);
  margin-bottom: 8px;
}

.heavy-form :deep(.el-input__wrapper) {
  background-color: #F8F8F8;
  border-radius: 0; /* 直角 */
  box-shadow: none !important;
  border: 2px solid #EEE;
  padding: 10px;
  transition: all 0.2s;
}

.heavy-form :deep(.el-input__wrapper.is-focus) {
  background-color: #FFF;
  border-color: var(--tnt-black);
}

.heavy-form :deep(.el-input__inner) {
  font-weight: bold;
  color: var(--tnt-black);
}

/* --- 核心按钮样式修复 --- */
.tnt-btn {
  width: 100%;
  height: 56px;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 1px;
  cursor: pointer;
  margin-top: 30px;
  border: 3px solid var(--tnt-black); /* 统一边框 */

  /* 关键修复：只对位置和阴影做动画，不对颜色做动画，防止闪烁/透明 */
  transition: transform 0.1s ease, box-shadow 0.1s ease;
}

/* 1. 登录按钮 (实心): 黑底黄字 -> 悬浮变 黄底黑字 */
.tnt-btn.solid {
  background-color: var(--tnt-black);
  color: var(--tnt-yellow);
}

.tnt-btn.solid:hover {
  background-color: var(--tnt-yellow);
  color: var(--tnt-black);
  /* 悬浮时的硬阴影 */
  box-shadow: 6px 6px 0px var(--tnt-black);
  transform: translate(-3px, -3px);
}

/* 2. 注册按钮 (线框): 白底黑字 -> 悬浮变 黑底白字 (或黑底黄字) */
.tnt-btn.outline {
  background-color: transparent;
  color: var(--tnt-black);
}

.tnt-btn.outline:hover {
  /* 确保悬浮时填充黑色背景，文字变黄，对比极高 */
  background-color: var(--tnt-black);
  color: var(--tnt-yellow);
  /* 阴影改为黄色，形成反差 */
  box-shadow: 6px 6px 0px var(--tnt-yellow);
  transform: translate(-3px, -3px);
}

/* 按钮禁用状态 */
.tnt-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.footer-switch {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
}

.footer-switch span {
  cursor: pointer;
  font-weight: bold;
  color: #888;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.footer-switch span:hover {
  color: var(--tnt-black);
  border-bottom-color: var(--tnt-yellow);
}
</style>