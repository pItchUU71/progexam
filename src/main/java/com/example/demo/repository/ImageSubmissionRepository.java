package com.example.demo.repository;

import com.example.demo.repository.model.ImageSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageSubmissionRepository extends JpaRepository<ImageSubmission, String> {

  @Override
  List<ImageSubmission> findAll();
}
