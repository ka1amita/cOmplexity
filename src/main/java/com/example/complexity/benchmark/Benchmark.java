package com.example.complexity.benchmark;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Benchmark {


  private BenchmarkRequestDTO requestDTO;
  private File projectRootpath;

  public Benchmark(BenchmarkRequestDTO requestDTO) {
    this.requestDTO = requestDTO;
  }

  public File getProjectRootpath() {
    return projectRootpath;
  }

  public BenchmarkRequestDTO getRequestDTO() {
    return requestDTO;
  }

  public void run() throws ExperimentWriteFailure, GradleTemplateWriteFailure {
    setUpGradle();
    createJar();
    dockerize();
    evaluate();
    close();
  }

  public void setProjectRootpath(File projectRootpath) {

    this.projectRootpath = projectRootpath;
  }

  private void setUpGradle() {
    // TODO try https://www.baeldung.com/run-shell-command-in-java
    // TODO try https://stackoverflow.com/questions/49876189/how-to-run-a-gradle-task-from-a-java-code
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("gradle", "wrapper");
    builder.directory(projectRootpath);
    builder.redirectErrorStream(true);
    try {
      Process process = builder.start();
      BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while (true) {
        line = r.readLine();
        if (line == null) {
          break;
        }
        System.out.println(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void dockerize() {
  }

  private void evaluate() {
  }

  private void close() {
    // TODO autoclosable and use try with resources
  }

  private void createJar() {
    // ./gradlew jmhJar --info
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("./gradlew", "jmhJar", "--info");
    builder.directory(projectRootpath);
    builder.redirectErrorStream(true);
    try {
      Process process = builder.start();
      BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while (true) {
        line = r.readLine();
        if (line == null) {
          break;
        }
        System.out.println(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
