package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;

public interface BenchmarkService {

  void createBenchmark(BenchmarkRequestDTO benchmarkRequestDTO)
      throws GradleTemplateWriteFailure, ExperimentWriteFailure;

}
