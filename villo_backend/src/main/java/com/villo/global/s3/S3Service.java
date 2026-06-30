package com.villo.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    // Presigned URL 발급 (업로드용, 5분 유효)
    public PresignedUrlResponse generatePresignedUrl(String originalFileName) {
        String key = generateKey(originalFileName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String uploadUrl = presignedRequest.url().toString();
        String imageUrl = endpoint + "/" + bucket + "/" + key;

        return new PresignedUrlResponse(uploadUrl, imageUrl);
    }

    // 고유한 파일 경로 생성
    private String generateKey(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return "todo-images/" + UUID.randomUUID() + extension;
    }

    public record PresignedUrlResponse(String uploadUrl, String imageUrl) {}
}
