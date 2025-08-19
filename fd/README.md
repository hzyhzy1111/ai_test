# 图片上传系统

基于 Vue 3 + TypeScript + Vite 构建的现代化图片上传系统，支持拖拽上传、批量上传、实时预览等功能。

## 功能特性

- 🖼️ **图片预览**: 选择图片后立即显示预览
- 📁 **拖拽上传**: 支持拖拽文件到上传区域
- 📦 **批量上传**: 支持同时选择多个图片文件
- 📊 **上传进度**: 实时显示上传进度
- 🎨 **现代化UI**: 美观的用户界面设计
- 📱 **响应式设计**: 支持移动端和桌面端
- 🔒 **文件验证**: 只允许上传图片格式文件
- 📏 **大小限制**: 支持文件大小限制

## 技术栈

### 前端
- Vue 3 (Composition API)
- TypeScript
- Vite
- CSS3 (Grid, Flexbox)

### 后端 (示例)
- Node.js
- Express.js
- Multer (文件上传中间件)
- CORS

## 快速开始

### 1. 安装前端依赖

```bash
npm install
# 或者
pnpm install
```

### 2. 启动前端开发服务器

```bash
npm run dev
# 或者
pnpm dev
```

前端将在 `http://localhost:5173` 启动

### 3. 启动后端服务器 (可选)

如果你想要完整的功能，需要启动后端服务器：

```bash
# 安装后端依赖
cd server
npm install

# 启动后端服务器
npm start
```

后端将在 `http://localhost:3000` 启动

## 项目结构

```
demo2/
├── src/
│   ├── components/
│   │   └── ImageUpload.vue      # 图片上传组件
│   ├── App.vue                  # 主应用组件
│   ├── main.ts                  # 应用入口
│   └── style.css               # 全局样式
├── server-example.js           # 后端服务器示例
├── server-package.json         # 后端依赖配置
├── package.json                # 前端依赖配置
└── README.md                   # 项目说明
```

## 后端接口

### 上传单个图片
- **POST** `/api/upload`
- **Content-Type**: `multipart/form-data`
- **参数**: `image` (文件)

### 批量上传图片
- **POST** `/api/upload-multiple`
- **Content-Type**: `multipart/form-data`
- **参数**: `images` (文件数组)

### 获取文件列表
- **GET** `/api/files`
- **返回**: 已上传文件列表

### 删除文件
- **DELETE** `/api/files/:filename`
- **参数**: `filename` (文件名)

## 配置说明

### 前端配置

在 `src/components/ImageUpload.vue` 中可以修改：

- 上传接口地址 (默认: `/api/upload`)
- 文件类型限制
- 文件大小限制
- 上传进度显示

### 后端配置

在 `server-example.js` 中可以修改：

- 服务器端口 (默认: 3000)
- 上传目录 (默认: `uploads/`)
- 文件大小限制 (默认: 5MB)
- 文件数量限制 (默认: 10个)

## 开发说明

### 自定义后端接口

如果你有自己的后端服务，只需要修改 `ImageUpload.vue` 中的 `uploadFile` 函数：

```typescript
const uploadFile = async (file: File): Promise<{ success: boolean; message: string }> => {
  try {
    const formData = new FormData()
    formData.append('image', file)

    // 修改为你的后端接口地址
    const response = await fetch('你的后端接口地址', {
      method: 'POST',
      body: formData
    })

    // 根据你的后端响应格式修改
    if (response.ok) {
      return {
        success: true,
        message: `文件 ${file.name} 上传成功`
      }
    } else {
      return {
        success: false,
        message: `文件 ${file.name} 上传失败`
      }
    }
  } catch (error) {
    return {
      success: false,
      message: `文件 ${file.name} 上传失败: 网络错误`
    }
  }
}
```

### 添加新功能

1. **图片压缩**: 可以在上传前添加图片压缩功能
2. **图片裁剪**: 可以添加图片裁剪功能
3. **云存储**: 可以集成阿里云OSS、腾讯云COS等云存储服务
4. **图片处理**: 可以添加水印、滤镜等图片处理功能

## 部署

### 前端部署

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### 后端部署

```bash
# 安装生产依赖
npm install --production

# 启动服务器
npm start
```

## 许可证

MIT License
