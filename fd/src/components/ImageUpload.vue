<template>
  <div class="image-upload-container">
    <!-- 文件选择区域 -->
    <div class="upload-area" @click="triggerFileInput" @drop="handleDrop" @dragover.prevent @dragenter.prevent>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        multiple
        @change="handleFileSelect"
        style="display: none"
      />
      <div class="upload-content">
        <div class="upload-icon">📷</div>
        <h2>选择图片进行AI分析</h2>
        <p>点击选择图片或拖拽图片到此处</p>
        <p class="upload-hint">支持 JPG、PNG、GIF、WebP 格式，最大 5MB</p>
      </div>
    </div>

    <!-- 图片预览区域 -->
    <div v-if="selectedFiles.length > 0" class="preview-section">
      <h3>已选择的图片 ({{ selectedFiles.length }})</h3>
      <div class="image-grid">
        <div
          v-for="(file, index) in selectedFiles"
          :key="index"
          class="image-item"
        >
          <img :src="file.preview" :alt="file.name" />
          <div class="image-info">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
          </div>
          <button @click="removeFile(index)" class="remove-btn">×</button>
        </div>
      </div>
    </div>

    <!-- 文本输入区域 -->
    <div v-if="selectedFiles.length > 0" class="question-input-section">
      <h3>请输入您的问题</h3>
      <div class="question-input-container">
        <textarea
          v-model="userQuestion"
          placeholder="请输入您想询问的问题，例如：这张图片里有什么？请详细描述图片内容。"
          class="question-textarea"
          rows="3"
        ></textarea>
        <div class="question-hint">
          <p>💡 提示：您可以询问图片内容、物体识别、场景描述等问题</p>
        </div>
      </div>
    </div>

    <!-- 上传按钮 -->
    <div v-if="selectedFiles.length > 0" class="upload-actions">
      <div class="upload-options">
        <label class="stream-toggle">
          <input type="checkbox" v-model="useStreamMode" />
          <span>流式输出</span>
        </label>
      </div>
      <div class="upload-buttons">
        <button @click="uploadFiles" :disabled="uploading || !userQuestion.trim()" class="upload-btn">
          <span v-if="uploading" class="loading-spinner"></span>
          {{ uploading ? 'AI分析中...' : '开始AI分析' }}
        </button>
        <button @click="clearFiles" class="clear-btn">清空选择</button>
      </div>
    </div>
    


    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <span class="progress-text">{{ uploadProgress }}%</span>
      <div v-if="useStreamMode && streamMessage" class="stream-message">
        {{ streamMessage }}
      </div>
    </div>

    <!-- 上传结果 -->
    <div v-if="uploadResults.length > 0" class="upload-results">
      <h3>AI分析结果</h3>
      <div class="result-list">
        <div
          v-for="(result, index) in uploadResults"
          :key="index"
          class="result-item"
          :class="{ 
            success: result.success, 
            error: !result.success,
            isStreaming: result.isStreaming 
          }"
        >
          <span class="result-icon">
            <span v-if="result.isStreaming">⏳</span>
            <span v-else-if="result.success">✅</span>
            <span v-else>❌</span>
          </span>
          <span class="result-text">{{ result.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { buildApiUrl, API_CONFIG, isValidImageType, isValidFileSize, UPLOAD_CONFIG } from '../config/api'

interface FileItem {
  file: File
  name: string
  size: number
  preview: string
}

interface UploadResult {
  success: boolean
  message: string
  fileName?: string
  isStreaming?: boolean
}

const fileInput = ref<HTMLInputElement>()
const selectedFiles = ref<FileItem[]>([])
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadResults = ref<UploadResult[]>([])
const useStreamMode = ref(false)
const streamMessage = ref('')
const userQuestion = ref('请分析这张图片')

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    addFiles(Array.from(target.files))
  }
}

// 处理拖拽
const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer?.files) {
    addFiles(Array.from(event.dataTransfer.files))
  }
}

// 添加文件
const addFiles = (files: File[]) => {
  files.forEach(file => {
    // 验证文件类型
    if (!isValidImageType(file)) {
      alert(`文件 ${file.name} 不是支持的图片格式`)
      return
    }
    
    // 验证文件大小
    if (!isValidFileSize(file)) {
      alert(`文件 ${file.name} 超过最大大小限制 (${UPLOAD_CONFIG.MAX_FILE_SIZE / 1024 / 1024}MB)`)
      return
    }
    
    // 验证文件数量
    if (selectedFiles.value.length >= UPLOAD_CONFIG.MAX_FILES) {
      alert(`最多只能选择 ${UPLOAD_CONFIG.MAX_FILES} 个文件`)
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      const fileItem: FileItem = {
        file,
        name: file.name,
        size: file.size,
        preview: e.target?.result as string
      }
      selectedFiles.value.push(fileItem)
    }
    reader.readAsDataURL(file)
  })
}

// 删除文件
const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

// 清空所有文件
const clearFiles = () => {
  selectedFiles.value = []
  uploadResults.value = []
  userQuestion.value = '请分析这张图片'
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 上传文件
const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return

  uploading.value = true
  uploadProgress.value = 0
  uploadResults.value = []
  streamMessage.value = ''

  try {
    for (let i = 0; i < selectedFiles.value.length; i++) {
      const fileItem = selectedFiles.value[i]
      
      if (useStreamMode.value) {
        // 使用流式输出
        await uploadFileStream(fileItem.file)
      } else {
        // 使用传统同步方式
        const progressInterval = setInterval(() => {
          if (uploadProgress.value < 90) {
            uploadProgress.value += 10
          }
        }, 100)

        const result = await uploadFile(fileItem.file)
        
        clearInterval(progressInterval)
        uploadProgress.value = 100

        uploadResults.value.push({
          success: result.success,
          message: result.message,
          fileName: fileItem.name
        })
      }

      // 等待一下再上传下一个文件
      await new Promise(resolve => setTimeout(resolve, 500))
    }
  } catch (error) {
    uploadResults.value.push({
      success: false,
      message: '识别过程中发生错误'
    })
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    streamMessage.value = ''
  }
}



// 流式上传单个文件到后端
const uploadFileStream = async (file: File): Promise<void> => {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('question', userQuestion.value.trim() || '请分析这张图片')

    const response = fetch(buildApiUrl('/api/ai/stream-analyze'), {
      method: 'POST',
      body: formData
    })

    response.then(res => {
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`)
      }

      const reader = res.body?.getReader()
      const decoder = new TextDecoder()

      if (!reader) {
        throw new Error('无法读取响应流')
      }

      const readStream = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            resolve()
            return
          }

          const chunk = decoder.decode(value)
          const lines = chunk.split('\n')

          lines.forEach(line => {
            if (line.startsWith('data: ')) {
              try {
                const data = JSON.parse(line.slice(6))
                
                if (data.type === 'progress') {
                  uploadProgress.value = data.progress
                  streamMessage.value = data.message
                } else if (data.type === 'stream') {
                  // 处理流式内容
                  if (!uploadResults.value.length || uploadResults.value[uploadResults.value.length - 1].fileName !== file.name) {
                    // 创建新的结果项
                    uploadResults.value.push({
                      success: true,
                      message: data.content,
                      fileName: file.name,
                      isStreaming: true
                    })
                  } else {
                    // 追加到现有结果
                    const currentResult = uploadResults.value[uploadResults.value.length - 1]
                    currentResult.message += data.content
                  }
                } else if (data.type === 'result') {
                  uploadProgress.value = 100
                  streamMessage.value = data.message
                  // 更新最终结果
                  const currentResult = uploadResults.value.find(r => r.fileName === file.name)
                  if (currentResult) {
                    currentResult.message = data.result
                    currentResult.isStreaming = false
                  }
                } else if (data.type === 'error') {
                  uploadResults.value.push({
                    success: false,
                    message: `图片 ${file.name} 分析失败: ${data.message}`,
                    fileName: file.name
                  })
                }
              } catch (e) {
                console.error('解析流数据失败:', e)
              }
            }
          })

          readStream()
        }).catch(reject)
      }

      readStream()
    }).catch(reject)
  })
}

// 上传单个文件到后端
const uploadFile = async (file: File): Promise<{ success: boolean; message: string }> => {
  try {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('question', userQuestion.value.trim() || '请分析这张图片')

    const response = await fetch(buildApiUrl(API_CONFIG.AI.UPLOAD_AND_ANALYZE), {
      method: 'POST',
      body: formData
    })

    if (response.ok) {
      const result = await response.text()
      return {
        success: true,
        message: `图片 ${file.name} 分析结果: ${result}`
      }
    } else {
      const errorText = await response.text()
      return {
        success: false,
        message: `图片 ${file.name} 分析失败: ${errorText || response.statusText}`
      }
    }
  } catch (error) {
    console.error('分析错误:', error)
    return {
      success: false,
      message: `图片 ${file.name} 分析失败: 网络错误`
    }
  }
}
</script>

<style scoped>
.image-upload-container {
  max-width: 600px;
  width: 100%;
  background: #ffffff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.upload-area {
  border: 3px dashed #667eea;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 30px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.upload-area:hover {
  border-color: #764ba2;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.2);
}

.upload-content {
  color: #333;
}

.upload-icon {
  font-size: 64px;
  margin-bottom: 20px;
  display: block;
}

.upload-content h2 {
  font-size: 1.8rem;
  margin: 0 0 15px 0;
  color: #333;
  font-weight: 600;
}

.upload-content p {
  font-size: 1.1rem;
  color: #666;
  margin: 10px 0;
}

.upload-hint {
  font-size: 0.9rem;
  color: #999;
  margin-top: 15px;
}

.preview-section {
  margin-bottom: 30px;
}

.preview-section h3 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  margin-top: 15px;
}

.image-item {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: white;
  transition: all 0.3s ease;
}

.image-item:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.image-item img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.image-info {
  padding: 12px;
  background: #f8f9fa;
}

.file-name {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.file-size {
  font-size: 0.8rem;
  color: #666;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: rgba(220, 53, 69, 1);
  transform: scale(1.1);
}

.question-input-section {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.question-input-section h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 1.1rem;
  font-weight: 600;
}

.question-input-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  transition: border-color 0.3s ease;
}

.question-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.question-hint {
  font-size: 0.85rem;
  color: #6c757d;
  line-height: 1.4;
}

.question-hint p {
  margin: 0;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 25px;
  align-items: center;
}

.upload-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stream-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666;
  user-select: none;
}

.stream-toggle input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
}

.upload-buttons {
  display: flex;
  gap: 15px;
}



.upload-btn, .clear-btn {
  padding: 12px 30px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.upload-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.clear-btn {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e0e0e0;
}

.clear-btn:hover {
  background: #e9ecef;
  border-color: #ccc;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.progress-text {
  font-weight: 600;
  color: #333;
  min-width: 40px;
}

.stream-message {
  font-size: 0.9rem;
  color: #667eea;
  margin-top: 8px;
  text-align: center;
  font-weight: 500;
}

.upload-results {
  margin-top: 25px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
}

.upload-results h3 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.result-list {
  margin-top: 10px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;
  font-size: 0.95rem;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item.success {
  color: #28a745;
}

.result-item.error {
  color: #dc3545;
}

.result-item.isStreaming {
  position: relative;
}

.result-item.isStreaming::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
  50% {
    opacity: 0.5;
    transform: translateY(-50%) scale(1.2);
  }
  100% {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
}

.result-icon {
  font-size: 18px;
}

.result-text {
  font-weight: 500;
}

@media (max-width: 768px) {
  .image-upload-container {
    padding: 30px 20px;
    margin: 10px;
  }
  
  .upload-area {
    padding: 40px 20px;
  }
  
  .upload-icon {
    font-size: 48px;
  }
  
  .upload-content h2 {
    font-size: 1.5rem;
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
  
  .upload-actions {
    flex-direction: column;
  }
  
  .upload-btn, .clear-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
