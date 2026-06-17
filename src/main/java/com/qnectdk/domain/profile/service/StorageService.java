package com.qnectdk.domain.profile.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 저장 추상화. 현재는 로컬 파일 구현, 추후 S3 구현으로 교체 가능.
 */
public interface StorageService {

    /**
     * 파일을 저장하고 공개 접근 URL을 반환한다.
     */
    String store(MultipartFile file);

    /**
     * 이전에 store가 반환한 URL의 파일을 삭제한다(이미지 교체 시 정리용). best-effort.
     */
    void delete(String url);
}
