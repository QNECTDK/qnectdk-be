package com.qnectdk.domain.profile.service;

import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 로컬 파일시스템 저장 구현 (S3 설정 전 임시).
 * {base-path}에 저장하고 {base-url}/{filename} 형태의 URL을 반환한다.
 */
@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    private final Path baseDir;
    private final String baseUrl;

    public LocalStorageService(
            @Value("${app.storage.local.base-path}") String basePath,
            @Value("${app.storage.local.base-url}") String baseUrl) {
        this.baseDir = Paths.get(basePath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
    }

    @Override
    public String store(MultipartFile file) {
        validateImage(file);
        try {
            Files.createDirectories(baseDir);
            String filename = UUID.randomUUID().toString().replace("-", "")
                    + extractExtension(file.getOriginalFilename());
            file.transferTo(baseDir.resolve(filename));
            return toPublicUrl(filename);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (filename.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(baseDir.resolve(filename).normalize());
        } catch (IOException e) {
            // 정리 실패는 업로드 흐름을 막지 않는다 (best-effort)
            log.warn("이전 이미지 삭제 실패: {}", filename, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";
    }

    private String toPublicUrl(String filename) {
        return baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
    }
}
