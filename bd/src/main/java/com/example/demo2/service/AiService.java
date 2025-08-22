package com.example.demo2.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;

import java.io.IOException;

@Service
public class AiService {
    
    public String analyzeImage(String imageUrl, String question) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("text", "You are a helpful assistant."))).build();
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("image", imageUrl),
                            Collections.singletonMap("text", question))).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen-vl-max-latest")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .build();
            MultiModalConversationResult result = conv.call(param);
            Object textContent = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            return textContent != null ? textContent.toString() : "无法获取分析结果";
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            return "错误: " + e.getMessage();
        }
    }
    
    public void analyzeImageStream(String imageUrl, String question, ResponseBodyEmitter emitter, Consumer<String> resultCallback) {
        System.out.println("开始流式分析，图片URL长度: " + (imageUrl != null ? imageUrl.length() : 0));
        
        CompletableFuture.runAsync(() -> {
            try {
                // 阶段1: 开始分析
                System.out.println("发送阶段1消息");
                emitter.send("data: {\"type\":\"progress\",\"stage\":\"start\",\"message\":\"开始分析图片...\",\"progress\":10}\n\n");
                
                // 阶段2: 图片处理
                System.out.println("发送阶段2消息");
                emitter.send("data: {\"type\":\"progress\",\"stage\":\"processing\",\"message\":\"正在处理图片...\",\"progress\":30}\n\n");
                
                // 阶段3: AI分析 - 使用真正的流式输出
                System.out.println("发送阶段3消息");
                emitter.send("data: {\"type\":\"progress\",\"stage\":\"analyzing\",\"message\":\"AI正在分析图片内容...\",\"progress\":60}\n\n");
                
                // 使用千问大模型的流式输出
                System.out.println("开始调用AI分析");
                streamQwenResponse(imageUrl, question, emitter, resultCallback);
                
            } catch (Exception e) {
                System.err.println("流式分析过程中发生错误: " + e.getMessage());
                e.printStackTrace();
                try {
                    emitter.send("data: {\"type\":\"error\",\"message\":\"分析失败: " + e.getMessage().replace("\"", "\\\"") + "\"}\n\n");
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
    }
    
    private void streamQwenResponse(String imageUrl, String question, ResponseBodyEmitter emitter, Consumer<String> resultCallback) {
        try {
            // 先发送一个测试消息，确保流式输出机制工作
            emitter.send("data: {\"type\":\"stream\",\"content\":\"正在连接AI服务...\"}\n\n");
            Thread.sleep(500);
            
            // 直接使用多模态对话API进行图片分析
            String fullResult = analyzeImage(imageUrl, question);
            
            // 检查结果是否为空或错误
            if (fullResult == null || fullResult.isEmpty() || fullResult.startsWith("错误:")) {
                emitter.send("data: {\"type\":\"error\",\"message\":\"AI分析失败: " + fullResult.replace("\"", "\\\"") + "\"}\n\n");
                emitter.complete();
                return;
            }
            
            // 发送分析开始消息
            emitter.send("data: {\"type\":\"stream\",\"content\":\"AI分析结果：\"}\n\n");
            Thread.sleep(300);
            
            // 模拟流式输出效果 - 将结果分段发送
            String[] sentences = fullResult.split("(?<=[。！？；])");
            StringBuilder streamedContent = new StringBuilder();
            
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    streamedContent.append(sentence);
                    
                    // 发送流式数据
                    try {
                        emitter.send("data: {\"type\":\"stream\",\"content\":\"" + 
                                   sentence.replace("\"", "\\\"").replace("\n", "\\n") + "\"}\n\n");
                        
                        // 模拟真实的流式延迟
                        Thread.sleep(200 + (int)(Math.random() * 300));
                        
                    } catch (IOException e) {
                        // 如果发送失败，可能是客户端断开连接
                        System.err.println("流式数据发送失败: " + e.getMessage());
                        return;
                    }
                }
            }
            
            // 发送完成信号，包含完整的AI分析结果
            emitter.send("data: {\"type\":\"result\",\"stage\":\"complete\",\"message\":\"分析完成\",\"progress\":100,\"result\":\"" + 
                       fullResult.replace("\"", "\\\"").replace("\n", "\\n") + "\"}\n\n");
            
            // 调用回调函数，传递完整的AI分析结果
            if (resultCallback != null) {
                resultCallback.accept(fullResult);
            }
            
            emitter.complete();
            
        } catch (Exception e) {
            try {
                emitter.send("data: {\"type\":\"error\",\"message\":\"流式输出失败: " + e.getMessage().replace("\"", "\\\"") + "\"}\n\n");
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }
    
    public String getDefaultAnalysis() {
        return analyzeImage(
            "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg",
            "图中描绘的是什么景象?"
        );
    }
}