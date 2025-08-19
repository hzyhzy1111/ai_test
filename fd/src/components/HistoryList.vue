<template>
  <div class="history-list">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="historyList.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <p>暂无历史记录</p>
    </div>
    
    <div v-else class="history-content">
      <div class="history-header">
        <span>共 {{ historyList.length }} 条记录</span>
        <button @click="refreshHistory" class="refresh-btn">刷新</button>
      </div>
      
      <div class="history-items">
        <div
          v-for="(item, index) in historyList"
          :key="index"
          class="history-item"
          @click="viewDetail(item)"
        >
          <div class="item-image">
            <img :src="item.imageUrl" :alt="item.filename" />
          </div>
          <div class="item-info">
            <h4>{{ item.filename }}</h4>
            <p class="item-time">{{ formatTime(item.createTime) }}</p>
            <p class="item-status" :class="item.status">
              {{ getStatusText(item.status) }}
            </p>
          </div>
          <div class="item-actions">
            <button @click.stop="deleteHistory(item.id)" class="delete-btn">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { buildApiUrl } from '../config/api'

interface HistoryItem {
  id: string
  filename: string
  imageUrl: string
  createTime: string
  status: 'success' | 'failed' | 'processing'
  result?: string
}

const historyList = ref<HistoryItem[]>([])
const loading = ref(false)

// 获取历史记录
const fetchHistory = async () => {
  loading.value = true
  try {
    const response = await fetch(buildApiUrl('/api/history'), {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getUserToken()}`
      }
    })

    if (response.ok) {
      const data = await response.json()
      historyList.value = data.data || []
    } else {
      console.error('获取历史记录失败')
    }
  } catch (error) {
    console.error('网络错误:', error)
  } finally {
    loading.value = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  fetchHistory()
}

// 删除历史记录
const deleteHistory = async (id: string) => {
  if (!confirm('确定要删除这条记录吗？')) return

  try {
    const response = await fetch(buildApiUrl(`/api/history/${id}`), {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${getUserToken()}`
      }
    })

    if (response.ok) {
      // 从列表中移除
      historyList.value = historyList.value.filter(item => item.id !== id)
    } else {
      alert('删除失败')
    }
  } catch (error) {
    console.error('删除错误:', error)
    alert('删除失败')
  }
}

// 查看详情
const viewDetail = (item: HistoryItem) => {
  // 这里可以打开详情弹窗或跳转到详情页面
  console.log('查看详情:', item)
}

// 格式化时间
const formatTime = (timeStr: string): string => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString()
}

// 获取状态文本
const getStatusText = (status: string): string => {
  const statusMap = {
    success: '识别成功',
    failed: '识别失败',
    processing: '识别中'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

// 获取用户token
const getUserToken = (): string => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const parsed = JSON.parse(userInfo)
    return parsed.token || ''
  }
  return ''
}

// 组件挂载时获取历史记录
onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-list {
  width: 100%;
  max-height: 300px;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #666;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.8rem;
  color: #666;
}

.refresh-btn {
  padding: 4px 8px;
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #e9ecef;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.item-image {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h4 {
  margin: 0 0 5px 0;
  font-size: 0.85rem;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  margin: 0 0 3px 0;
  font-size: 0.7rem;
  color: #999;
}

.item-status {
  margin: 0;
  font-size: 0.7rem;
  font-weight: 500;
}

.item-status.success {
  color: #28a745;
}

.item-status.failed {
  color: #dc3545;
}

.item-status.processing {
  color: #ffc107;
}

.item-actions {
  flex-shrink: 0;
}

.delete-btn {
  padding: 4px 8px;
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: #f1b0b7;
  border-color: #ed969e;
}

/* 滚动条样式 */
.history-list::-webkit-scrollbar {
  width: 4px;
}

.history-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.history-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
