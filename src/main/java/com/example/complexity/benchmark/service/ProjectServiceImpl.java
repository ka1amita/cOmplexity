package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.Project;
import com.example.complexity.benchmark.exceptions.BenchmarkClassWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectServiceTemplateWriteFailure;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

  @Override
  public File createProject(Benchmark benchmark) throws ProjectServiceTemplateWriteFailure, BenchmarkClassWriteFailure {
    return new Project(benchmark).getRootpath();
  }
}
