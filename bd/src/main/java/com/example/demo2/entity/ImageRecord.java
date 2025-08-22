package com.example.demo2.entity;

import java.util.Date;

public class ImageRecord {
    
    private Integer id;
    
    private String imagePath;
    
    private String resultText;
    
    private Date createdAt;
    
    // 构造函数
    public ImageRecord() {}
    
    public ImageRecord(String imagePath, String resultText) {
        this.imagePath = imagePath;
        this.resultText = resultText;
    }
    
    // Getter和Setter方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getResultText() {
        return resultText;
    }
    
    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "ImageRecord{" +
                "id=" + id +
                ", imagePath='" + imagePath + '\'' +
                ", resultText='" + resultText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
