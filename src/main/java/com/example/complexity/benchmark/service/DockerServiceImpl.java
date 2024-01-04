package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.stereotype.Service;

@Service
public class DockerServiceImpl implements DockerService {

  private static final String IMAGE_NAME = "complexity";

  @Override
  public void runBenchmark(Benchmark benchmark) {
    DockerClient dockerClient = DockerClientBuilder.getInstance().build();


  }
}
