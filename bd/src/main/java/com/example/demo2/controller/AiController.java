package com.example.demo2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo2.service.AiService;
import com.example.demo2.service.ImageRecordService;
import com.example.demo2.entity.ImageRecord;
import java.util.Base64;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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