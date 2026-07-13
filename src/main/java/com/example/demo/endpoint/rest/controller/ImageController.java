package com.example.demo.endpoint.rest.controller;

import com.example.demo.repository.ImageSubmissionRepository;
import com.example.demo.repository.model.ImageSubmission;
import com.example.demo.service.ImageService;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class ImageController {

  private final ImageService imageService;
  private final ImageSubmissionRepository repository;

  @PostMapping("/images")
  public ResponseEntity<String> uploadImage(
      @RequestParam("email") String email,
      @RequestParam("file") MultipartFile file) {
    try {
      imageService.save(file, email);
    } catch (IOException e) {
      return ResponseEntity.badRequest().body("Invalid file");
    }
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/images")
  public List<ImageSubmission> getAllImages() {
    return repository.findAll();
  }
}
