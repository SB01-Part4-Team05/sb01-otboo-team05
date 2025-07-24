package com.part4.team05.sb01otbooteam05.domain.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 amazonS3;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  /**
   * 프로필 이미지를 S3에 업로드하고 URL을 반환
   */
  public String uploadProfileImage(UUID userId, MultipartFile file) {
    try {
      // 파일 확장자 추출
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      // S3에 저장할 파일명 생성 (profile/userId~~)
      String fileName = "profile/" + userId.toString() + "_" + System.currentTimeMillis() + extension;

      // 메타데이터 설정
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType());
      metadata.setContentLength(file.getSize());

      // S3에 파일 업로드
      PutObjectRequest putObjectRequest = new PutObjectRequest(
          bucketName,
          fileName,
          file.getInputStream(),
          metadata
      );

      amazonS3.putObject(putObjectRequest);

      // 업로드된 파일의 URL 반환
      String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();

      log.info("S3 프로필 이미지 업로드 성공: userId={}, fileName={}, url={}",
          userId, fileName, fileUrl);

      return fileUrl;

    } catch (IOException e) {
      log.error("S3 프로필 이미지 업로드 실패: userId={}", userId, e);
      throw new RuntimeException("프로필 이미지 업로드에 실패했습니다", e);
    }
  }

  /**
   * S3에서 파일 삭제 (기존 프로필 이미지 삭제용)
   */
  public void deleteFile(String fileUrl) {
    try {
      // URL에서 파일명 추출
      String fileName = extractFileNameFromUrl(fileUrl);

      if (fileName != null && amazonS3.doesObjectExist(bucketName, fileName)) {
        amazonS3.deleteObject(bucketName, fileName);
        log.info("S3 파일 삭제 성공: fileName={}", fileName);
      }
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패: fileUrl={}", fileUrl, e);
      // 삭제 실패해도 전체 프로세스를 중단시키지 않음
    }
  }

  /**
   * S3 URL에서 파일명 추출
   */
  private String extractFileNameFromUrl(String fileUrl) {
    if (fileUrl == null || !fileUrl.contains(bucketName)) {
      return null;
    }

    try {
      // https://bucket-name.s3.region.amazonaws.com/profile/filename 형태에서 파일명 추출
      String[] parts = fileUrl.split("/");
      if (parts.length >= 2) {
        // profile/filename 형태로 반환
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
      }
    } catch (Exception e) {
      log.warn("URL에서 파일명 추출 실패: {}", fileUrl, e);
    }

    return null;
  }
}
