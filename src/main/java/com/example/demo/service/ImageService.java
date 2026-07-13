package com.example.demo.service;

import com.example.demo.endpoint.rest.controller.ImageRequest;
import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@AllArgsConstructor
public class ImageService {

  private final ImageSubmissionRepository repository;
  private final S3Conf s3Conf;
  private final S3Client s3Client;

  private static final Pattern DATA_URI_PATTERN =
      Pattern.compile("^data:image/(png|jpeg);base64,(.+)$");

  public record SaveResult(String id, String email, String format) {}

  public SaveResult save(ImageRequest request) {
    var matcher = DATA_URI_PATTERN.matcher(request.file());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Only JPEG and PNG images are allowed (data URI format)");
    }

    var format = matcher.group(1);
    var base64Data = matcher.group(2);
    var imageBytes = Base64.getDecoder().decode(base64Data);
    var fileName = request.fileName() != null ? request.fileName() : "image." + format;

    var id = UUID.randomUUID().toString();
    var submission = new ImageSubmission();
    submission.setId(id);
    submission.setFileName(fileName);
    submission.setEmail(request.email());
    submission.setCreatedAt(Instant.now());
    repository.save(submission);

    var s3Key = "original-" + id + "." + format;
    s3Client.putObject(
        PutObjectRequest.builder().bucket(s3Conf.getBucketName()).key(s3Key).build(),
        RequestBody.fromBytes(imageBytes));

    return new SaveResult(id, request.email(), format);
  }
}
