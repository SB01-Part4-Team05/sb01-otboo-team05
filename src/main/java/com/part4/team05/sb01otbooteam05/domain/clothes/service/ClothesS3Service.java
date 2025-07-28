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

  @Value("${clothes.bucket}")
  private String bucketName;

  public String upload(UUID clothesId, MultipartFile file) {
    try {
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      String fileName = clothesId.toString() + "_" + System.currentTimeMillis() + extension;

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

      return s3Client.utilities().getUrl(getUrlRequest).toExternalForm();

    } catch (IOException e) {
      throw new RuntimeException("이미지 업로드에 실패했습니다", e);
    }
  }

  public void delete(String fileUrl) {
    try {
      String fileName = extractFileNameFromUrl(fileUrl);

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

        } catch (NoSuchKeyException e) {
          log.warn("삭제하려는 파일이 S3에 존재하지 않음: fileName={}", fileName);
        }
      }
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패: fileUrl={}", fileUrl, e);
    }
  }


  private String extractFileNameFromUrl(String fileUrl) {
    if (fileUrl == null || !fileUrl.contains(bucketName)) {
      return null;
    }

    try {
      String[] parts = fileUrl.split("/");
      if (parts.length >= 1) {
        return parts[parts.length - 1];
      }
    } catch (Exception e) {
      log.warn("URL에서 파일명 추출 실패: {}", fileUrl, e);
    }

    return null;
  }
}
