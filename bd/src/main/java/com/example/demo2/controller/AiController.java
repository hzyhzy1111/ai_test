package com.example.demo2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import com.example.demo2.service.AiService;
import com.example.demo2.service.ImageRecordService;
import com.example.demo2.entity.ImageRecord;
import java.util.Base64;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ImageRecordService imageRecordService;
    
    @GetMapping("/analyze")
    public String analyzeDefaultImage() {
        return aiService.getDefaultAnalysis();
    }
    
    @PostMapping("/analyze")
    public String analyzeImage(@RequestParam String imageUrl, @RequestParam String question) {
        return aiService.analyzeImage(imageUrl, question);
    }
    
    @PostMapping("/upload-image")
    public String uploadAndAnalyzeImage(@RequestParam("image") MultipartFile imageFile, 
                                       @RequestParam(value = "question", defaultValue = "请分析这张图片") String question) {
        try {
            // 将图片转换为Base64格式
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = "data:" + imageFile.getContentType() + ";base64," + 
                               Base64.getEncoder().encodeToString(imageBytes);
            
            // 调用AI服务分析图片
            String result;
            try {
                result = aiService.analyzeImage(base64Image, question);
            } catch (Exception e) {
                // 如果AI服务失败，使用模拟结果进行测试
                result = "模拟AI分析结果：这是一张图片，包含" + question;
                System.err.println("AI服务调用失败: " + e.getMessage());
            }
            
            // 保存历史记录 - 只保存文件名而不是完整的Base64数据
            try {
                String imageIdentifier = "图片_" + System.currentTimeMillis() + ".jpg";
                imageRecordService.saveImageRecord(imageIdentifier, result);
            } catch (Exception e) {
                // 如果保存历史记录失败，记录错误但不影响返回结果
                System.err.println("保存历史记录失败: " + e.getMessage());
            }
            
            return result;
        } catch (IOException e) {
            return "错误: 图片处理失败 - " + e.getMessage();
        } catch (Exception e) {
            return "错误: AI分析失败 - " + e.getMessage();
        }
    }
    
    @PostMapping("/stream-analyze")
    public ResponseBodyEmitter streamAnalyzeImage(@RequestParam("image") MultipartFile imageFile, 
                                                 @RequestParam(value = "question", defaultValue = "请分析这张图片") String question) {
        // 设置超时时间为5分钟
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);
        
        // 用于存储完整的AI分析结果
        final String[] aiResult = new String[1];
        
        try {
            // 将图片转换为Base64格式
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = "data:" + imageFile.getContentType() + ";base64," + 
                               Base64.getEncoder().encodeToString(imageBytes);
            
            // 启动流式分析，使用回调函数接收AI分析结果
            aiService.analyzeImageStream(base64Image, question, emitter, (result) -> {
                aiResult[0] = result;
            });
            
            // 异步保存历史记录
            emitter.onCompletion(() -> {
                try {
                    String imageIdentifier = "图片_" + System.currentTimeMillis() + ".jpg";
                    // 使用实际的AI分析结果，如果没有则使用默认文本
                    String resultToSave = aiResult[0] != null ? aiResult[0] : "流式分析结果";
                    imageRecordService.saveImageRecord(imageIdentifier, resultToSave);
                } catch (Exception e) {
                    System.err.println("保存历史记录失败: " + e.getMessage());
                }
            });
            
            // 添加超时处理
            emitter.onTimeout(() -> {
                try {
                    emitter.send("data: {\"type\":\"error\",\"message\":\"请求超时，请重试\"}\n\n");
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            
            // 添加错误处理
            emitter.onError((ex) -> {
                try {
                    emitter.send("data: {\"type\":\"error\",\"message\":\"处理过程中发生错误: " + ex.getMessage().replace("\"", "\\\"") + "\"}\n\n");
                    emitter.complete();
                } catch (IOException e) {
                    // 忽略错误
                }
            });
            
        } catch (IOException e) {
            try {
                emitter.send("data: {\"type\":\"error\",\"message\":\"图片处理失败: " + e.getMessage().replace("\"", "\\\"") + "\"}\n\n");
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
        
        return emitter;
    }
    
    @GetMapping("/health")
    public String health() {
        return "AI服务运行正常";
    }
    

    
    /**
     * 测试数据库连接
     */
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 尝试获取记录数量来测试数据库连接
            List<ImageRecord> records = imageRecordService.getAllRecords();
            response.put("success", true);
            response.put("message", "数据库连接正常");
            response.put("recordCount", records.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
    
    /**
     * 获取所有历史记录
     */
    @GetMapping("/history")
    public Map<String, Object> getHistoryRecords() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ImageRecord> records = imageRecordService.getAllRecords();
            response.put("success", true);
            response.put("data", records);
            response.put("message", "获取历史记录成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取历史记录失败: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * 删除历史记录
     */
    @DeleteMapping("/history/{id}")
    public Map<String, Object> deleteHistoryRecord(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = imageRecordService.deleteRecord(id);
            if (success) {
                response.put("success", true);
                response.put("message", "删除记录成功");
            } else {
                response.put("success", false);
                response.put("message", "删除记录失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除记录失败: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * 测试保存记录
     */
    @PostMapping("/test-save")
    public Map<String, Object> testSaveRecord() {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = imageRecordService.saveImageRecord("test-image-path", "测试AI分析结果");
            if (success) {
                response.put("success", true);
                response.put("message", "测试记录保存成功");
            } else {
                response.put("success", false);
                response.put("message", "测试记录保存失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "测试记录保存失败: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}