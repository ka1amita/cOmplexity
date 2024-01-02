package com.example.complexity.benchmark;

import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class BenchmarkProject {

  private final File projectRootDirpath;

  private GradleTemplate gradleTemplate;

  private Experiment experiment;

  public BenchmarkProject(File projectRootDirpath) {
    checkAvailability(projectRootDirpath);
    this.projectRootDirpath = projectRootDirpath;
  }

  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  public void setGradleTemplate(GradleTemplate gradleTemplate) {
    this.gradleTemplate = gradleTemplate;
  }

  private void checkAvailability(File dirname) throws ProjectDirectoryAlreadyExists {
    if (dirname.exists()) {
      throw new ProjectDirectoryAlreadyExists(
          String.format("project root path %s already exists", dirname.getPath()));
    }
  }

  public void run() throws ExperimentWriteFailure, GradleTemplateWriteFailure {
    writeProjectFiles();
    setUpGradle();
    createJar();
    dockerize();
    evaluate();
    close();
  }

  private void writeProjectFiles() throws ExperimentWriteFailure, GradleTemplateWriteFailure {
    projectRootDirpath.mkdirs();
    gradleTemplate.writeGradleFileContentsToFiles(projectRootDirpath);
    experiment.writeExperimentClassBodyToFile(projectRootDirpath);
  }

  private void setUpGradle() {
    // TODO try https://www.baeldung.com/run-shell-command-in-java
    // TODO try https://stackoverflow.com/questions/49876189/how-to-run-a-gradle-task-from-a-java-code
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("gradle", "wrapper");
    builder.directory(projectRootDirpath);
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

  private void createJar() {
    // ./gradlew jmhJar --info
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("./gradlew", "jmhJar", "--info");
    builder.directory(projectRootDirpath);
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
}
