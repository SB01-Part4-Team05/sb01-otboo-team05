package com.part4.team05.sb01otbooteam05.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

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

      // PutObjectRequest 생성 (v2 방식)
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      // S3에 파일 업로드 (v2 방식)
      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      // 업로드된 파일의 URL 생성 (v2 방식)
      GetUrlRequest getUrlRequest = GetUrlRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .build();

      String fileUrl = s3Client.utilities().getUrl(getUrlRequest).toExternalForm();

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

      if (fileName != null) {
        // 파일 존재 여부 확인 (v2 방식)
        try {
          HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
              .bucket(bucketName)
              .key(fileName)
              .build();

          s3Client.headObject(headObjectRequest);

          // 파일 삭제 (v2 방식)
          DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
              .bucket(bucketName)
              .key(fileName)
              .build();

          s3Client.deleteObject(deleteObjectRequest);
          log.info("S3 파일 삭제 성공: fileName={}", fileName);

        } catch (NoSuchKeyException e) {
          log.warn("삭제하려는 파일이 S3에 존재하지 않음: fileName={}", fileName);
        }
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
