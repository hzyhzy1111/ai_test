package com.example.demo2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo2.service.AiService;
import java.util.Base64;
import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
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
            return aiService.analyzeImage(base64Image, question);
        } catch (IOException e) {
            return "错误: 图片处理失败 - " + e.getMessage();
        }
    }
    
    @GetMapping("/health")
    public String health() {
        return "AI服务运行正常";
    }
}