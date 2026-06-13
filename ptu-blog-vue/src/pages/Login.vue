<template>
  <div class="login-page">
    <!-- 表单卡片容器 -->
    <div class="form-card">
      <!-- ===== 标题区 ===== -->
      <div class="form-header">
        <h1 class="form-title">{{ isLogin ? '登录' : '注册' }}</h1>
        <span class="form-switch" @click="toggleMode">
          {{ isLogin ? '新用户注册' : '已有账号？去登录' }}
        </span>
      </div>

      <!-- ===== 登录表单 ===== -->
      <div v-if="isLogin" class="form-body">
        <!-- 用户名 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': loginErrors.username }"
            type="text"
            v-model="loginForm.username"
            placeholder="用户名"
            @blur="validateLoginField('username')"
          />
          <span v-if="loginErrors.username" class="field-error">{{ loginErrors.username }}</span>
        </div>

        <!-- 密码 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': loginErrors.password }"
            type="password"
            v-model="loginForm.password"
            placeholder="密码"
            @blur="validateLoginField('password')"
            @keyup.enter="handleLogin"
          />
          <span class="forgot-link" @click="showForgotHint">忘记密码？</span>
          <span v-if="loginErrors.password" class="field-error">{{ loginErrors.password }}</span>
        </div>

        <!-- 登录按钮 -->
        <button
          class="submit-btn"
          :disabled="!loginFormValid"
          :class="{ 'submit-btn--disabled': !loginFormValid }"
          @click="handleLogin"
        >
          登录
        </button>

        <!-- 服务端错误提示 -->
        <p v-if="loginServerError" class="server-error">{{ loginServerError }}</p>
      </div>

      <!-- ===== 注册表单 ===== -->
      <div v-else class="form-body">
        <!-- 用户名 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': registerErrors.username }"
            type="text"
            v-model="registerForm.userName"
            placeholder="用户名"
            @blur="validateRegisterField('username')"
          />
          <span v-if="registerErrors.username" class="field-error">{{ registerErrors.username }}</span>
        </div>

        <!-- 昵称 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': registerErrors.nickName }"
            type="text"
            v-model="registerForm.nickName"
            placeholder="昵称"
            @blur="validateRegisterField('nickName')"
          />
          <span v-if="registerErrors.nickName" class="field-error">{{ registerErrors.nickName }}</span>
        </div>

        <!-- 邮箱 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': registerErrors.email }"
            type="email"
            v-model="registerForm.email"
            placeholder="邮箱"
            @blur="validateRegisterField('email')"
          />
          <span v-if="registerErrors.email" class="field-error">{{ registerErrors.email }}</span>
        </div>

        <!-- 密码 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': registerErrors.password }"
            type="password"
            v-model="registerForm.password"
            placeholder="密码（6-12位字母、数字、下划线）"
            @blur="validateRegisterField('password')"
          />
          <span v-if="registerErrors.password" class="field-error">{{ registerErrors.password }}</span>
        </div>

        <!-- 确认密码 -->
        <div class="field-group">
          <input
            class="field-input"
            :class="{ 'field-input--error': registerErrors.confirmPassword }"
            type="password"
            v-model="registerForm.confirmPassword"
            placeholder="确认密码"
            @blur="validateRegisterField('confirmPassword')"
            @keyup.enter="handleRegister"
          />
          <span v-if="registerErrors.confirmPassword" class="field-error">{{ registerErrors.confirmPassword }}</span>
        </div>

        <!-- 注册按钮 -->
        <button
          class="submit-btn"
          :disabled="!registerFormValid"
          :class="{ 'submit-btn--disabled': !registerFormValid }"
          @click="handleRegister"
        >
          完成注册
        </button>

        <!-- 服务端错误提示 -->
        <p v-if="registerServerError" class="server-error">{{ registerServerError }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { userLogin, userRegister } from '../api/user.js'
import { setToken } from '../utils/auth.js'

export default {
  name: 'Login',

  data() {
    return {
      // 当前模式：true=登录 false=注册
      isLogin: true,

      // 登录表单
      loginForm: {
        username: '',
        password: ''
      },
      loginErrors: {
        username: '',
        password: ''
      },
      loginServerError: '',

      // 注册表单
      registerForm: {
        userName: '',
        nickName: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      registerErrors: {
        username: '',
        nickName: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      registerServerError: '',

      submitting: false
    }
  },

  computed: {
    // 登录按钮是否可点击：两个字段都非空
    loginFormValid() {
      return this.loginForm.username.trim() && this.loginForm.password.trim()
    },
    // 注册按钮是否可点击：五个字段都非空且无校验错误
    registerFormValid() {
      const f = this.registerForm
      const e = this.registerErrors
      return (
        f.userName.trim() &&
        f.nickName.trim() &&
        f.email.trim() &&
        f.password.trim() &&
        f.confirmPassword.trim() &&
        !e.username && !e.nickName && !e.email && !e.password && !e.confirmPassword
      )
    }
  },

  methods: {
    // ===== 模式切换 =====
    toggleMode() {
      this.isLogin = !this.isLogin
      this.loginServerError = ''
      this.registerServerError = ''
      // 同步路由 query 参数，兼容旧逻辑
      const query = this.isLogin ? { login: '1' } : { login: '0' }
      this.$router.replace({ path: '/Login', query })
    },

    // ===== 登录字段校验 =====
    validateLoginField(field) {
      this.loginServerError = ''
      if (field === 'username') {
        this.loginErrors.username = this.loginForm.username.trim() ? '' : '请输入用户名'
      }
      if (field === 'password') {
        this.loginErrors.password = this.loginForm.password.trim() ? '' : '请输入密码'
      }
    },

    // ===== 注册字段校验 =====
    validateRegisterField(field) {
      this.registerServerError = ''
      const f = this.registerForm
      switch (field) {
        case 'username':
          this.registerErrors.username = f.userName.trim() ? '' : '请输入用户名'
          break
        case 'nickName':
          this.registerErrors.nickName = f.nickName.trim() ? '' : '请输入昵称'
          break
        case 'email': {
          if (!f.email.trim()) {
            this.registerErrors.email = '请输入邮箱'
          } else if (!/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/.test(f.email)) {
            this.registerErrors.email = '邮箱格式不正确'
          } else {
            this.registerErrors.email = ''
          }
          break
        }
        case 'password': {
          const pwd = f.password
          if (!pwd) {
            this.registerErrors.password = '请输入密码'
          } else if (!/^(\w){6,12}$/.test(pwd)) {
            this.registerErrors.password = '密码需6-12位字母、数字、下划线'
          } else {
            this.registerErrors.password = ''
          }
          // 密码变化时同步校验确认密码
          if (f.confirmPassword && pwd !== f.confirmPassword) {
            this.registerErrors.confirmPassword = '两次密码不一致'
          } else if (f.confirmPassword) {
            this.registerErrors.confirmPassword = ''
          }
          break
        }
        case 'confirmPassword': {
          if (!f.confirmPassword) {
            this.registerErrors.confirmPassword = '请再次输入密码'
          } else if (f.confirmPassword !== f.password) {
            this.registerErrors.confirmPassword = '两次密码不一致'
          } else {
            this.registerErrors.confirmPassword = ''
          }
          break
        }
      }
    },

    // ===== 登录提交 =====
    handleLogin() {
      if (this.submitting || !this.loginFormValid) return
      // 最终校验
      this.validateLoginField('username')
      this.validateLoginField('password')
      if (this.loginErrors.username || this.loginErrors.password) return

      this.submitting = true
      this.loginServerError = ''

      userLogin(this.loginForm.username, this.loginForm.password)
        .then((response) => {
          setToken(response.token)
          localStorage.setItem('userInfo', JSON.stringify(response.userInfo))
          const logUrl = localStorage.getItem('logUrl')
          this.$router.push({ path: logUrl || '/' })
        })
        .catch(() => {
          this.loginServerError = '用户名或密码错误'
          this.submitting = false
        })
    },

    // ===== 注册提交 =====
    handleRegister() {
      if (this.submitting || !this.registerFormValid) return
      // 全部字段最终校验
      const fields = ['username', 'nickName', 'email', 'password', 'confirmPassword']
      fields.forEach((f) => this.validateRegisterField(f))

      const hasError = Object.values(this.registerErrors).some((e) => e)
      if (hasError) return

      this.submitting = true
      this.registerServerError = ''

      const f = this.registerForm
      userRegister(f.userName, f.nickName, f.email, f.password)
        .then(() => {
          this.submitting = false
          // 注册成功，切换到登录
          this.isLogin = true
          this.loginServerError = ''
          this.registerServerError = ''
          // 清空登录表单（避免残留旧数据）
          this.loginForm.username = ''
          this.loginForm.password = ''
          // 同步路由
          this.$router.replace({ path: '/Login', query: { login: '1' } })
        })
        .catch((error) => {
          this.submitting = false
          const data = error.response && error.response.data
          if (data && data.msg) {
            this.registerServerError = data.msg
          } else {
            this.registerServerError = '注册失败，请稍后重试'
          }
        })
    },

    // ===== 忘记密码 =====
    showForgotHint() {
      this.$message({
        type: 'info',
        message: '功能开发中，敬请期待'
      })
    },

    // ===== 路由初始化 =====
    routeChange() {
      const loginParam = this.$route.query.login
      this.isLogin = loginParam === undefined ? true : parseInt(loginParam) === 1
      // 清空表单
      this.loginForm = { username: '', password: '' }
      this.loginErrors = { username: '', password: '' }
      this.loginServerError = ''
      this.registerForm = { userName: '', nickName: '', email: '', password: '', confirmPassword: '' }
      this.registerErrors = { username: '', nickName: '', email: '', password: '', confirmPassword: '' }
      this.registerServerError = ''
    }
  },

  watch: {
    '$route': 'routeChange'
  },

  created() {
    this.routeChange()
  }
}
</script>

<style scoped>
/* ----- 页面全屏容器：使用 login.jpg 底板 ----- */
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/static/img/login.jpg') center / cover no-repeat;
  font-family: 'Inter', 'Noto Sans SC', '思源黑体', 'Source Han Sans CN', -apple-system, BlinkMacSystemFont,
    'Segoe UI', sans-serif;
  overflow: hidden;
}

/* ----- 表单卡片容器（哑光米白卡纸质感） ----- */
.form-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  margin: 0 16px;
  /* 毛玻璃质感：半透白 + 模糊背景 */
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 12px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04);
  padding: 36px 32px;
}

/* ----- 标题区 ----- */
.form-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
}
.form-title {
  font-size: 26px;
  font-weight: 600;
  color: #333333;
  margin: 0;
  /* 轻微字间距，提升标题呼吸感 */
  letter-spacing: 1px;
}
.form-switch {
  font-size: 13px;
  font-weight: 400;
  color: #90d4f7;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s ease;
}
.form-switch:hover {
  color: #6bb5d8;
}

/* ----- 表单字段组 ----- */
.form-body {
  display: flex;
  flex-direction: column;
}
.field-group {
  position: relative;
  margin-bottom: 18px;
}
.field-group:last-of-type {
  margin-bottom: 0;
}

/* ----- 输入框（统一牛皮纸体系） ----- */
.field-input {
  display: block;
  width: 100%;
  height: 42px;
  padding: 0 14px;
  font-size: 15px;
  color: #333333;
  background: rgba(255, 255, 255, 0.5);
  border: 1.5px solid rgba(0, 0, 0, 0.08);
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
  font-family: inherit;
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease;
}
/* 占位符颜色 */
.field-input::placeholder {
  color: #999999;
  font-size: 14px;
}
/* 聚焦态：边框切换主色浅蓝，微微内光 */
.field-input:focus {
  border-color: #90d4f7;
  box-shadow:
    inset 0 0 0 1px rgba(144, 212, 247, 0.2),
    0 0 0 3px rgba(144, 212, 247, 0.1);
}
/* 错误态：边框浅红 */
.field-input--error {
  border-color: #e8a0a0;
}
.field-input--error:focus {
  border-color: #e8a0a0;
  box-shadow:
    inset 0 0 0 1px rgba(232, 160, 160, 0.15),
    0 0 0 3px rgba(232, 160, 160, 0.08);
}

/* ----- 行内校验错误提示（输入框下方浅红小字） ----- */
.field-error {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: #d47878;
  line-height: 1.3;
}

/* ----- 忘记密码链接（密码框右下角） ----- */
.forgot-link {
  position: absolute;
  right: 2px;
  bottom: -20px;
  font-size: 13px;
  color: #999999;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s ease;
}
.forgot-link:hover {
  color: #90d4f7;
}

/* ----- 提交按钮 ----- */
.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 28px;
  font-size: 15px;
  font-weight: 500;
  color: #ffffff;
  background: #90d4f7;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: 3px;
  /* 极细微纸张凹凸阴影，拒绝生硬直角硬阴影 */
  box-shadow:
    0 1px 2px rgba(160, 150, 130, 0.15),
    0 2px 6px rgba(144, 212, 247, 0.12);
  transition:
    background 0.2s ease,
    transform 0.1s ease,
    box-shadow 0.2s ease;
}
/* hover：蓝色轻微加深 */
.submit-btn:hover {
  background: #7dc8ed;
  box-shadow:
    0 2px 4px rgba(160, 150, 130, 0.2),
    0 4px 10px rgba(144, 212, 247, 0.18);
}
/* active：极微小下压缩放 */
.submit-btn:active {
  transform: scale(0.98);
}
/* 禁用态：浅米灰，不可点击 */
.submit-btn--disabled {
  background: #ddd9d0;
  color: #b0aaa0;
  cursor: not-allowed;
  box-shadow: none;
}
.submit-btn--disabled:hover {
  background: #ddd9d0;
  box-shadow: none;
}

/* ----- 服务端错误提示（按钮下方） ----- */
.server-error {
  margin-top: 14px;
  font-size: 13px;
  color: #d47878;
  text-align: center;
}

/* ============================================================
   响应式适配：屏幕宽度 < 500px 时缩小内边距
   ============================================================ */
@media screen and (max-width: 500px) {
  .form-card {
    max-width: 100%;
    margin: 0 12px;
    padding: 28px 20px;
  }
  .form-title {
    font-size: 22px;
  }
  .submit-btn {
    margin-top: 22px;
  }
}
</style>
