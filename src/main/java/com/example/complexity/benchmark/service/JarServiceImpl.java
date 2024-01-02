package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.Benchmark;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.stereotype.Service;

@Service
public class JarServiceImpl implements JarService {

  @Override
  public File createJar(Benchmark benchmark) {
    File rootpath = benchmark.getProjectRootpath();
    setUpGradle(rootpath);
    create(rootpath);
    // TODO
    return new File("");
  }

  private void create(File rootpath) {
    // ./gradlew jmhJar --info
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("./gradlew", "jmhJar", "--info");
    builder.directory(rootpath);
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

  private void setUpGradle(File rootpath) {
    // TODO try https://www.baeldung.com/run-shell-command-in-java
    // TODO try https://stackoverflow.com/questions/49876189/how-to-run-a-gradle-task-from-a-java-code
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("gradle", "wrapper");
    builder.directory(rootpath);
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
