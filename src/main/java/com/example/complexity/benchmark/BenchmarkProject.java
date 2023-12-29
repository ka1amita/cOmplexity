package com.example.complexity.benchmark;

import com.example.complexity.benchmark.exceptions.BenchmarkProjectException;
import java.io.File;

public class BenchmarkProject {

  private File projectRoot;

  public BenchmarkProject(File projectRoot) {
    checkAvailability(projectRoot);
    this.projectRoot = projectRoot;
  }

  private void checkAvailability(File projectRoot) {
    if (projectRoot.exists()) {
      throw new BenchmarkProjectException(
          String.format("project root path %s already exists", projectRoot.getPath()));
    }
  }

  public void add(Experiment experiment) {
  }

  public void run() {
    compile();
    dockerize();
    evaluate();
    close();
  }

  private void compile() {
  }

  private void dockerize() {
  }

  private void evaluate() {
  }

  private void close() {
    // TODO autoclosable and use try with resources
  }
}
