package com.example.demo.endpoint.rest.controller;

import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import com.example.demo.service.ImageService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@Slf4j
public class ImageController {

  private final ImageService imageService;
  private final ImageSubmissionRepository repository;

  @PostMapping("/images")
  public ResponseEntity<String> uploadImage(
      @RequestParam("email") String email,
      @RequestParam("file") MultipartFile file) {
    try {
      imageService.save(file, email);
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
