package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ImageSubmitted;
import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import com.example.demo.service.ImageService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class ImageController {

  private final ImageService imageService;
  private final ImageSubmissionRepository repository;
  private final EventProducer eventProducer;

  @PostMapping("/images")
  public ResponseEntity<String> uploadImage(@RequestBody ImageRequest request) {
    try {
      var result = imageService.save(request);
      var event =
          ImageSubmitted.builder()
              .id(result.id())
              .email(result.email())
              .format(result.format())
              .build();
      eventProducer.accept(List.of(event));
      return ResponseEntity.ok("OK");
    } catch (Exception e) {
      log.error("POST /images failed", e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @GetMapping("/images")
  public List<ImageSubmission> getAllImages() {
    return repository.findAll();
  }
}
