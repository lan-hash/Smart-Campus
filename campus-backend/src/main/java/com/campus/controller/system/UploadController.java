package com.campus.controller.system;

import com.aliyun.oss.OSS;
import com.campus.common.config.OssConfig;
import com.campus.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    public UploadController(OSS ossClient, OssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    /**
     * 上传图片到阿里云 OSS
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 1. 获取文件扩展名 (commons-io 的 FilenameUtils)
            String originalName = file.getOriginalFilename();
            String ext = FilenameUtils.getExtension(originalName);
            if (ext == null || ext.isEmpty()) {
                ext = "jpg";
            }

            // 2. 生成 OSS 中的对象键: campus/2026/07/25/uuid.jpg
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = "campus/" + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

            // 3. 上传到 OSS
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            }

            // 4. 返回可访问的完整 URL
            String url = ossConfig.getUrlPrefix() + objectName;
            return Result.success(url);

        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}