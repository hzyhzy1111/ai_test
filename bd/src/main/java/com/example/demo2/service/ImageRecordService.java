package com.example.demo2.service;

import com.example.demo2.entity.ImageRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class ImageRecordService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<ImageRecord> rowMapper = new RowMapper<ImageRecord>() {
        @Override
        public ImageRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            ImageRecord record = new ImageRecord();
            record.setId(rs.getInt("id"));
            record.setImagePath(rs.getString("image_path"));
            record.setResultText(rs.getString("result_text"));
            record.setCreatedAt(rs.getTimestamp("created_at"));
            return record;
        }
    };
    
    /**
     * 保存图片记录
     */
    public boolean saveImageRecord(String imagePath, String resultText) {
        String sql = "INSERT INTO image_records (image_path, result_text, created_at) VALUES (?, ?, ?)";
        int result = jdbcTemplate.update(sql, imagePath, resultText, new Date());
        return result > 0;
    }
    
    /**
     * 获取所有历史记录
     */
    public List<ImageRecord> getAllRecords() {
        String sql = "SELECT * FROM image_records ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }
    
    /**
     * 根据ID获取记录
     */
    public ImageRecord getRecordById(Integer id) {
        String sql = "SELECT * FROM image_records WHERE id = ?";
        List<ImageRecord> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * 删除记录
     */
    public boolean deleteRecord(Integer id) {
        String sql = "DELETE FROM image_records WHERE id = ?";
        int result = jdbcTemplate.update(sql, id);
        return result > 0;
    }
}
