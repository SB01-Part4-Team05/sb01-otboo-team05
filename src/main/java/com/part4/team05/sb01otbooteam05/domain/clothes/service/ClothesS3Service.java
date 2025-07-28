package com.part4.team05.sb01otbooteam05.domain.clothes.service;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesS3Service {

  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  private static final String CLOTHES_FOLDER = "clothes/";

  public String upload(UUID clothesId, MultipartFile file) {
    log.info("S3 업로드 시도: bucket={}, clothesId={}", bucketName, clothesId);

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      // clothes/ 폴더 안에 저장
      String fileName = CLOTHES_FOLDER + clothesId.toString() + "_" + System.currentTimeMillis() + extension;

      log.info("S3 파일명: {}", fileName);

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      GetUrlRequest getUrlRequest = GetUrlRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .build();

      String url = s3Client.utilities().getUrl(getUrlRequest).toExternalForm();
      log.info("S3 업로드 성공: {}", url);

      return url;

    } catch (IOException e) {
      log.error("S3 업로드 실패: bucket={}, clothesId={}, error={}", bucketName, clothesId, e.getMessage());
      throw new RuntimeException("이미지 업로드에 실패했습니다", e);
    }
  }

  public void delete(String fileUrl) {
    try {
      String fileName = extractFileNameFromUrl(fileUrl);
      log.info("S3 파일 삭제 시도: bucket={}, fileName={}", bucketName, fileName);

      if (fileName != null) {
        try {
          HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
              .bucket(bucketName)
              .key(fileName)
              .build();

          s3Client.headObject(headObjectRequest);

          DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
              .bucket(bucketName)
              .key(fileName)
              .build();

          s3Client.deleteObject(deleteObjectRequest);
          log.info("S3 파일 삭제 성공: {}", fileName);

        } catch (NoSuchKeyException e) {
          log.warn("삭제하려는 파일이 S3에 존재하지 않음: fileName={}", fileName);
        }
      }
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패: fileUrl={}, error={}", fileUrl, e.getMessage());
    }
  }

  private String extractFileNameFromUrl(String fileUrl) {
    if (fileUrl == null || !fileUrl.contains(bucketName)) {
      return null;
    }

    try {
      String bucketPath = bucketName + "/";
      int bucketIndex = fileUrl.indexOf(bucketPath);
      if (bucketIndex != -1) {
        return fileUrl.substring(bucketIndex + bucketPath.length());
      }

      String[] parts = fileUrl.split("/");
      if (parts.length >= 2) {
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
      }
    } catch (Exception e) {
      log.warn("URL에서 파일명 추출 실패: {}", fileUrl, e);
    }

    return null;
  }
}

