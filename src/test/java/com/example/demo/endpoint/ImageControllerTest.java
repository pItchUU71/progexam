package com.example.demo.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.conf.FacadeIT;
import com.example.demo.endpoint.rest.controller.ImageRequest;
import com.example.demo.repository.model.ImageSubmission;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

class ImageControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @MockBean private S3Client s3Client;
  @MockBean private SesClient sesClient;

  @Test
  void post_and_get_images() {
    var pngBase64 = Base64.getEncoder().encodeToString("fake-png-content".getBytes());
    var body = new ImageRequest(
        "test@example.com",
        "data:image/png;base64," + pngBase64,
        "test.png");

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var requestEntity = new HttpEntity<>(body, headers);

    var postResponse = restTemplate.exchange(
        "/images",
        HttpMethod.POST,
        requestEntity,
        String.class);

    assertEquals(HttpStatus.OK, postResponse.getStatusCode());
    assertEquals("OK", postResponse.getBody());

    var getResponse = restTemplate.getForEntity("/images", ImageSubmission[].class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertNotNull(getResponse.getBody());
  }
}
