package com.example.complexity.benchmark;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.exceptions.GradlewCommandFailure;
import com.example.complexity.benchmark.exceptions.JarCommandFailure;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Jar {

  static final String FILENAME = "build/libs/experiment-1.0-SNAPSHOT-jmh.jar";
  private final ProcessBuilder builder = new ProcessBuilder();
  private final File rootpath;
  private final File jarFilepath;

  public Jar(Benchmark benchmark) throws JarCommandFailure, GradlewCommandFailure {
    this.rootpath = benchmark.getProjectRootpath();
    this.jarFilepath = new File(rootpath, FILENAME);

    configureBuilder();
    initGradlew();
    createJar();
  }

  private void configureBuilder() {
    builder.directory(rootpath);
    builder.redirectErrorStream(true);
  }

  private void initGradlew() throws GradlewCommandFailure {
    // TODO try https://www.baeldung.com/run-shell-command-in-java
    // TODO try https://stackoverflow.com/questions/49876189/how-to-run-a-gradle-task-from-a-java-code
    try {
      processCommand("gradle", "wrapper");
    } catch (IOException e) {
      throw new GradlewCommandFailure(e);
    }
  }

  private void createJar() throws JarCommandFailure {
    try {
      processCommand("./gradlew", "jmhJar", "--info");
    } catch (IOException e) {
      throw new JarCommandFailure(e);
    }
  }

  private void processCommand(String... command) throws IOException {
    builder.command(command);
    final Process process = builder.start();

    try (final InputStream source = process.getInputStream();
        final InputStreamReader reader = new InputStreamReader(source);
        final BufferedReader buffer = new BufferedReader(reader)) {

      while (true) {
        String line = buffer.readLine();
        if (line == null) {
          break;
        }
        // TODO log
        System.out.println(line);
      }
    }
  }

  public File getJarFilepath() {
    return jarFilepath;
  }
}
