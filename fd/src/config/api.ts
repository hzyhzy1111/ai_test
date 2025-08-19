// API配置文件
export const API_CONFIG = {
  // 基础URL，根据你的后端服务地址修改
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
  
  // 上传相关接口
  UPLOAD: {
    // 单个文件上传
    SINGLE: '/api/upload',
    // 批量文件上传
    MULTIPLE: '/api/upload-multiple',
    // 获取文件列表
    LIST: '/api/files',
    // 删除文件
    DELETE: '/api/files'
  },
  
  // 用户相关接口
  USER: {
    // 用户登录
    LOGIN: '/api/login',
    // 用户注册
    REGISTER: '/api/register',
    // 获取用户信息
    PROFILE: '/api/user/profile',
    // 退出登录
    LOGOUT: '/api/logout'
  },
  
  // 历史记录相关接口
  HISTORY: {
    // 获取历史记录列表
    LIST: '/api/history',
    // 获取单条历史记录详情
    DETAIL: '/api/history',
    // 删除历史记录
    DELETE: '/api/history'
  }
}

// 构建完整的API URL
export const buildApiUrl = (endpoint: string): string => {
  return `${API_CONFIG.BASE_URL}${endpoint}`
}

// 上传配置
export const UPLOAD_CONFIG = {
  // 最大文件大小 (5MB)
  MAX_FILE_SIZE: 5 * 1024 * 1024,
  // 支持的文件类型
  ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  // 最大文件数量
  MAX_FILES: 10
}

// 文件类型验证
export const isValidImageType = (file: File): boolean => {
  return UPLOAD_CONFIG.ALLOWED_TYPES.includes(file.type)
}

// 文件大小验证
export const isValidFileSize = (file: File): boolean => {
  return file.size <= UPLOAD_CONFIG.MAX_FILE_SIZE
}
