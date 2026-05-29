package com.campus.controller.common;

import com.campus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器 - 公共接口（无需登录）
 */
@Slf4j
@Tag(name = "文件上传控制器", description = "公共文件上传接口，支持图片等文件")
@RestController
@RequestMapping("/api/common")
public class FileUploadController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Operation(summary = "上传图片", description = "公共图片上传接口，返回图片访问URL")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择图片");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件名格式不正确");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        List<String> allowedSuffixes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
        if (!allowedSuffixes.contains(suffix)) {
            return Result.error("仅支持 jpg/png/gif/webp 格式图片");
        }

        String realPath = getRealPath();
        File dir = new File(realPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("创建目录 {}: {}", realPath, created);
        }

        String fileName = UUID.randomUUID() + suffix;

        try {
            File targetFile = new File(realPath + fileName);
            file.transferTo(targetFile);

            String imageUrl = "http://localhost:8080/image/" + fileName;
            log.info("图片上传成功: {}, 绝对路径: {}", imageUrl, targetFile.getAbsolutePath());
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取图片存储的真实路径
     * 开发环境：项目根目录/image/
     * 生产环境：jar包所在目录/image/
     */
    private String getRealPath() {
        // 获取当前运行的绝对路径
        String userDir = System.getProperty("user.dir");
        log.info("当前运行目录: {}", userDir);

        // 如果配置的是相对路径，拼接运行目录
        if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
            String relativePath = uploadPath.substring(2); // 去掉 ./
            return userDir + File.separator + relativePath;
        }

        // 如果是绝对路径，直接使用
        return uploadPath;
    }
}