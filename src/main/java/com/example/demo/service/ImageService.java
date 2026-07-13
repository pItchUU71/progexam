package com.example.demo.service;

import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import jakarta.mail.internet.InternetAddress;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class ImageService {

  private final ImageSubmissionRepository repository;
  private final S3Conf s3Conf;
  private final S3Client s3Client;
  private final Mailer mailer;
  private final Tika tika;
  private final ImageService self;

  private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

  public ImageService(
      ImageSubmissionRepository repository,
      S3Conf s3Conf,
      S3Client s3Client,
      Mailer mailer,
      Tika tika,
      @Lazy @Autowired ImageService self) {
    this.repository = repository;
    this.s3Conf = s3Conf;
    this.s3Client = s3Client;
    this.mailer = mailer;
    this.tika = tika;
    this.self = self;
  }

  public String save(MultipartFile file, String email) throws IOException {
    var mimeType = tika.detect(file.getInputStream());
    if (!ALLOWED_TYPES.contains(mimeType)) {
      throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
    }

    var id = UUID.randomUUID().toString();
    var submission = new ImageSubmission();
    submission.setId(id);
    submission.setFileName(file.getOriginalFilename());
    submission.setEmail(email);
    submission.setCreatedAt(Instant.now());
    repository.save(submission);

    self.processImage(id, file.getBytes(), file.getOriginalFilename(), email);

    return id;
  }

  @Async
  public void processImage(String id, byte[] imageBytes, String fileName, String email) {
    try {
      var original = ImageIO.read(new ByteArrayInputStream(imageBytes));
      var bwImage = toBlackAndWhite(original);

      var baos = new ByteArrayOutputStream();
      var formatName = fileName.endsWith(".png") ? "png" : "jpg";
      ImageIO.write(bwImage, formatName, baos);

      var s3Key = "bw-" + id + "." + formatName;
      uploadToS3(s3Key, baos.toByteArray());

      var s3Url =
          s3Client
              .utilities()
              .getUrl(builder -> builder.bucket(s3Conf.getBucketName()).key(s3Key));
      sendEmailWithLink(email, s3Url.toString());

    } catch (Exception e) {
      log.error("Failed to process image {}", id, e);
    }
  }

  private BufferedImage toBlackAndWhite(BufferedImage original) {
    var bw =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    bw.getGraphics().drawImage(original, 0, 0, null);
    return bw;
  }

  private void uploadToS3(String key, byte[] bytes) {
    s3Client.putObject(
        PutObjectRequest.builder().bucket(s3Conf.getBucketName()).key(key).build(),
        RequestBody.fromBytes(bytes));
  }

  @SneakyThrows
  private void sendEmailWithLink(String to, String s3Link) {
    var internetAddress = new InternetAddress(to);
    var email =
        new Email(
            internetAddress,
            List.of(),
            List.of(),
            "Your black & white image is ready",
            "<a href=\"" + s3Link + "\">Download your black & white image</a>",
            List.of());
    mailer.accept(email);
  }
}
