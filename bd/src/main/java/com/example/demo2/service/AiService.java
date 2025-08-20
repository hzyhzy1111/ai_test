package com.example.demo2.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Collections;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;

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
    
    public String getDefaultAnalysis() {
        return analyzeImage(
            "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg",
            "图中描绘的是什么景象?"
        );
    }
}