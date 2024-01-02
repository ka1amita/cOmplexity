package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.Benchmark;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

  @Override
  public File createProject(Benchmark benchmark) throws GradleTemplateWriteFailure, ExperimentWriteFailure {
    return new Project(benchmark).getRootpath();
  }
}
