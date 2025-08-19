<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h1>图片识别系统</h1>
    </div>
    
    <div class="sidebar-content">
      <!-- 登录组件 -->
      <div class="sidebar-section">
        <div class="section-header">
          <h3>用户登录</h3>
        </div>
        <div class="section-content">
          <LoginForm />
        </div>
      </div>
      
      <!-- 历史记录组件 -->
      <div class="sidebar-section">
        <div class="section-header">
          <h3>历史记录</h3>
        </div>
        <div class="section-content">
          <HistoryList />
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import LoginForm from './LoginForm.vue'
import HistoryList from './HistoryList.vue'
</script>

<style scoped>
.sidebar {
  width: 320px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.sidebar-header {
  padding: 30px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.sidebar-header h1 {
  font-size: 1.5rem;
  margin: 0;
  font-weight: 600;
  text-align: center;
}

.sidebar-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 80px);
}

.sidebar-section {
  margin-bottom: 30px;
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e9ecef;
}

.section-header {
  background: #ffffff;
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
}

.section-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.section-content {
  padding: 20px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .sidebar-header {
    padding: 20px;
  }
  
  .sidebar-content {
    padding: 15px;
    height: calc(200px - 80px);
  }
}
</style>
