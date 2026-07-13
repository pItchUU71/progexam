package com.example.demo.endpoint.rest.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageRequest(
    @JsonProperty("email") String email,
    @JsonProperty("file") String file,
    @JsonProperty("fileName") String fileName) {}
