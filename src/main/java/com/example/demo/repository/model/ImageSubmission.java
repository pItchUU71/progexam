package com.example.demo.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ImageSubmission {
  @Id private String id;
  private String fileName;
  private String email;
  private Instant createdAt;
}
