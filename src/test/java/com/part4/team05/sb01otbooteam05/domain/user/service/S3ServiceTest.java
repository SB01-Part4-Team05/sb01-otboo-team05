package com.part4.team05.sb01otbooteam05.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3 서비스 핵심 테스트")
class S3ServiceTest {

  @Mock private S3Client s3Client;
  @Mock private S3Utilities s3Utilities;
  @InjectMocks private S3Service s3Service;

  private final String TEST_BUCKET_NAME = "test-bucket";
  private final UUID TEST_USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(s3Service, "bucketName", TEST_BUCKET_NAME);
  }

  @Test
  @DisplayName("프로필 이미지 업로드 성공")
  void uploadProfileImage_Success() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("profile.jpg");
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getSize()).thenReturn(1024L);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

    String expectedUrl = "https://" + TEST_BUCKET_NAME + ".s3.amazonaws.com/profile/test.jpg";

    when(s3Client.utilities()).thenReturn(s3Utilities);
    // URL 객체로 반환
    try {
      when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new java.net.URL(expectedUrl));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    String result = s3Service.uploadProfileImage(TEST_USER_ID, file);

    assertThat(result).isEqualTo(expectedUrl);
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verify(s3Client).utilities();
    verify(s3Utilities).getUrl(any(GetUrlRequest.class));
  }

  @Test
  @DisplayName("프로필 이미지 업로드 실패 - IOException")
  void uploadProfileImage_IOException() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("profile.jpg");
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getSize()).thenReturn(1024L);
    when(file.getInputStream()).thenThrow(new IOException("파일 읽기 실패"));

    assertThatThrownBy(() -> s3Service.uploadProfileImage(TEST_USER_ID, file))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("프로필 이미지 업로드에 실패했습니다");
  }

  @Test
  @DisplayName("파일 삭제 성공")
  void deleteFile_Success() {
    String fileUrl = "https://" + TEST_BUCKET_NAME + ".s3.amazonaws.com/profile/test.jpg";

    HeadObjectResponse headObjectResponse = HeadObjectResponse.builder().build();
    when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

    DeleteObjectResponse deleteObjectResponse = DeleteObjectResponse.builder().build();
    when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(deleteObjectResponse);

    s3Service.deleteFile(fileUrl);

    verify(s3Client).headObject(any(HeadObjectRequest.class));
    verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  @DisplayName("파일 삭제 - 파일 존재하지 않음")
  void deleteFile_FileNotExists() {
    String fileUrl = "https://" + TEST_BUCKET_NAME + ".s3.amazonaws.com/profile/test.jpg";

    when(s3Client.headObject(any(HeadObjectRequest.class)))
        .thenThrow(NoSuchKeyException.builder().build());

    assertThatCode(() -> s3Service.deleteFile(fileUrl))
        .doesNotThrowAnyException();

    verify(s3Client).headObject(any(HeadObjectRequest.class));
    verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
  }
}
