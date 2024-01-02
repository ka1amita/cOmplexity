package com.example.complexity.benchmark;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import java.io.File;

public class Benchmark {


  private BenchmarkRequestDTO requestDTO;
  private File projectRootpath;
  private File jarFilepath;

  public Benchmark(BenchmarkRequestDTO requestDTO) {
    this.requestDTO = requestDTO;
  }

  public File getProjectRootpath() {
    return projectRootpath;
  }

  public BenchmarkRequestDTO getRequestDTO() {
    return requestDTO;
  }

  public void setProjectRootpath(File projectRootpath) {
    this.projectRootpath = projectRootpath;
  }

  public void setJarFilepath(File jarFilepath) {
    this.jarFilepath = jarFilepath;
  }
}
