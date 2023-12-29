package com.example.complexity.benchmark;

import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import java.io.File;

public class BenchmarkProject {

  private final File projectRootDirname;

  private Experiment experiment;

  public BenchmarkProject(File projectRootDirname) {
    checkAvailability(projectRootDirname);
    this.projectRootDirname = projectRootDirname;
  }

  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  private void checkAvailability(File dirname) throws ProjectDirectoryAlreadyExists {
    if (dirname.exists()) {
      throw new ProjectDirectoryAlreadyExists(
          String.format("project root path %s already exists", dirname.getPath()));
    }
  }

  public void run() throws ExperimentWriteFailure {
    write();
    compile();
    dockerize();
    evaluate();
    close();
  }

  private void write() throws ExperimentWriteFailure {
    projectRootDirname.mkdirs();
    experiment.writeExperimentClassBodyToFile(projectRootDirname);
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
