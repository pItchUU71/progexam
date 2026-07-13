package com.example.demo.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Conf {

  @Getter private final String bucketName;
  private final Region region;

  public S3Conf(@Value("poja-bucket") String bucketName, @Value("eu-west-3") Region region) {
    this.bucketName = bucketName;
    this.region = region;
  }

  @Bean
  public S3Client getS3Client() {
    return S3Client.builder().region(region).build();
  }
}
