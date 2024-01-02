package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.Benchmark;
import com.example.complexity.benchmark.BenchmarkClass;
import com.example.complexity.benchmark.ProjectTemplate;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import java.io.File;

public class Project {
  private final BenchmarkRequestDTO requestDTO;
  private final File rootpath;
  ProjectTemplate template;
  BenchmarkClass benchmarkClass;


  public Project(Benchmark benchmark)
      throws GradleTemplateWriteFailure, ExperimentWriteFailure {
    this.requestDTO = benchmark.getRequestDTO();
    this.rootpath = benchmark.getProjectRootpath();
    setFiles();
    writeFiles();
  }

  public File getRootpath() {
    return rootpath;
  }

  private void validateRootpath(File rootpath) throws ProjectDirectoryAlreadyExists {
    if (rootpath.exists()) {
      throw new ProjectDirectoryAlreadyExists(
          String.format("project root path %s already exists", rootpath.getPath()));
    }
  }

  public void setFiles() {
    template = new ProjectTemplate();
    benchmarkClass = new BenchmarkClass(requestDTO);
  }

  public void writeFiles() throws GradleTemplateWriteFailure, ExperimentWriteFailure {
    validateRootpath(rootpath);
    rootpath.mkdirs();
    template.writeGradleFileContentsToFiles(rootpath);
    benchmarkClass.writeExperimentClassBodyToFile(rootpath);
  }
}
