<template>
  <div class="login-form">
    <div v-if="!isLoggedIn" class="login-form-content">
      <div class="form-group">
        <label for="username">用户名</label>
        <input
          id="username"
          v-model="username"
          type="text"
          placeholder="请输入用户名"
          class="form-input"
        />
      </div>
      
      <div class="form-group">
        <label for="password">密码</label>
        <input
          id="password"
          v-model="password"
          type="password"
          placeholder="请输入密码"
          class="form-input"
        />
      </div>
      
      <button @click="handleLogin" :disabled="loading" class="login-btn">
        <span v-if="loading" class="loading-spinner"></span>
        {{ loading ? '登录中...' : '登录' }}
      </button>
      
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
    
    <div v-else class="user-info">
      <div class="user-avatar">
        <span>{{ userInfo.username.charAt(0).toUpperCase() }}</span>
      </div>
      <div class="user-details">
        <h4>{{ userInfo.username }}</h4>
        <p>{{ userInfo.email }}</p>
      </div>
      <button @click="handleLogout" class="logout-btn">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { buildApiUrl, API_CONFIG } from '../config/api'

interface UserInfo {
  username: string
  email: string
  token: string
}

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const isLoggedIn = ref(false)
const userInfo = reactive<UserInfo>({
  username: '',
  email: '',
  token: ''
})

// 登录处理
const handleLogin = async () => {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await fetch(buildApiUrl('/api/login'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })

    if (response.ok) {
      const data = await response.json()
      userInfo.username = data.username
      userInfo.email = data.email
      userInfo.token = data.token
      isLoggedIn.value = true
      
      // 保存到本地存储
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      // 清空表单
      username.value = ''
      password.value = ''
    } else {
      const errorData = await response.json()
      error.value = errorData.message || '登录失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 退出登录
const handleLogout = () => {
  isLoggedIn.value = false
  userInfo.username = ''
  userInfo.email = ''
  userInfo.token = ''
  localStorage.removeItem('userInfo')
}

// 检查本地存储的登录状态
const checkLoginStatus = () => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    const parsed = JSON.parse(savedUserInfo)
    userInfo.username = parsed.username
    userInfo.email = parsed.email
    userInfo.token = parsed.token
    isLoggedIn.value = true
  }
}

// 组件挂载时检查登录状态
checkLoginStatus()
</script>

<style scoped>
.login-form {
  width: 100%;
}

.login-form-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.login-btn {
  padding: 10px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #dc3545;
  font-size: 0.8rem;
  text-align: center;
  padding: 8px;
  background: #f8d7da;
  border-radius: 4px;
  border: 1px solid #f5c6cb;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  text-align: center;
}

.user-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
}

.user-details h4 {
  margin: 0;
  font-size: 1rem;
  color: #333;
}

.user-details p {
  margin: 5px 0 0 0;
  font-size: 0.8rem;
  color: #666;
}

.logout-btn {
  padding: 8px 16px;
  background: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #e9ecef;
  border-color: #ccc;
}
</style>
