package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.exceptions.BenchmarkClassWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectServiceTemplateWriteFailure;
import java.io.File;

public interface ProjectService {

  File createProject(Benchmark benchmark)
      throws ProjectServiceTemplateWriteFailure, BenchmarkClassWriteFailure;
}
