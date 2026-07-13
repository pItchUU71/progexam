package com.example.demo.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.conf.FacadeIT;
import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class ImageControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ImageSubmissionRepository repository;

  @Test
  void post_and_get_images() {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add("email", "test@example.com");
    body.add("file", new ByteArrayResource("fake-image-content".getBytes()) {
      @Override
      public String getFilename() {
        return "test.png";
      }
    });

    var headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
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
