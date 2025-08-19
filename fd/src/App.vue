<script setup lang="ts">
import ImageUpload from './components/ImageUpload.vue'
import Sidebar from './components/Sidebar.vue'
</script>

<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <Sidebar />
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="content-wrapper">
        <ImageUpload />
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  background: #ffffff;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #fafafa;
  height: 100vh;
  overflow-y: auto;
}

.content-wrapper {
  width: 100%;
  max-width: 800px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .app-container {
    flex-direction: column;
    height: 100vh;
  }
  
  .main-content {
    padding: 10px;
    height: calc(100vh - 200px);
  }
}
</style>
