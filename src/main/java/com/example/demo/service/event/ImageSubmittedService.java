package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.ImageSubmitted;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.service.S3Conf;
import jakarta.mail.internet.InternetAddress;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@AllArgsConstructor
@Slf4j
public class ImageSubmittedService implements Consumer<ImageSubmitted> {

  private final S3Client s3Client;
  private final S3Conf s3Conf;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ImageSubmitted event) {
    var id = event.getId();
    var format = event.getFormat();
    var originalKey = "original-" + id + "." + format;

    ResponseInputStream<?> originalObj =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(s3Conf.getBucketName()).key(originalKey).build());

    var original = ImageIO.read(originalObj);
    var bwImage = toBlackAndWhite(original);

    var baos = new ByteArrayOutputStream();
    ImageIO.write(bwImage, format, baos);

    var bwKey = "bw-" + id + "." + format;
    s3Client.putObject(
        PutObjectRequest.builder().bucket(s3Conf.getBucketName()).key(bwKey).build(),
        RequestBody.fromBytes(baos.toByteArray()));

    var bwUrl =
        s3Client
            .utilities()
            .getUrl(builder -> builder.bucket(s3Conf.getBucketName()).key(bwKey));

    sendEmail(event.getEmail(), bwUrl.toString());
  }

  private BufferedImage toBlackAndWhite(BufferedImage original) {
    var bw =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    bw.getGraphics().drawImage(original, 0, 0, null);
    return bw;
  }

  @SneakyThrows
  private void sendEmail(String to, String s3Link) {
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
