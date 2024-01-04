package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.github.dockerjava.api.exception.DockerException;
import java.io.IOException;

public interface DockerService {

  void runBenchmark(Benchmark benchmark)
      throws DockerException, InterruptedException, IOException;
}
