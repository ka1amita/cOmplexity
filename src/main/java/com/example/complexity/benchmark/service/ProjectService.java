package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import java.io.File;

public interface ProjectService {

  File createProject(Benchmark benchmark)
      throws GradleTemplateWriteFailure, ExperimentWriteFailure;
}
