package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.faceit.library.service.exception.S3Exception;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private static final String BOOKS_FOLDER = "books/";
    private final S3Client s3Client;
    @Value("${aws.bucket}")
    private String bucketName;

    public void putObject(String fileKey, byte[] file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(BOOKS_FOLDER + fileKey)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));
        } catch (Exception e) {
            throw new S3Exception("Can't put object to S3 bucket: " + bucketName + " fileKey: " + fileKey);
        }
    }

    public byte[] getObject(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(BOOKS_FOLDER + fileKey)
                    .build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            return response.readAllBytes();
        } catch (Exception e) {
            throw new S3Exception("Can't get object from S3 bucket: " + bucketName + " fileKey: " + fileKey);
        }
    }

    public void deleteObject(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(BOOKS_FOLDER + fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new S3Exception("Can't delete object in S3 bucket: " + bucketName + " fileKey: " + fileKey);
        }
    }
}
