package com.example.complexity.benchmark;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.BenchmarkClassWriteFailure;
import jakarta.validation.ValidationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.springframework.validation.annotation.Validated;

@Validated
public class BenchmarkClass {

  static final String FILENAME = "src/jmh/java/org/example/BenchmarkClass.java";
  private static final String CONTENT_TEMPLATE = """
      package org.example;
      
      %1$s
      import org.openjdk.jmh.annotations.Benchmark;
      import org.openjdk.jmh.annotations.Level;
      import org.openjdk.jmh.annotations.Param;
      import org.openjdk.jmh.annotations.Scope;
      import org.openjdk.jmh.annotations.Setup;
      import org.openjdk.jmh.annotations.State;
      import org.openjdk.jmh.infra.Blackhole;

      @State(Scope.Benchmark)
      public class BenchmarkClass {

        @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256"})
        public int scope;

      %2$s

        @Setup(Level.Invocation)
        public void setUp() {
      %3$s
        }

        @Benchmark
        public void benchmark(Blackhole blackhole) {
          blackhole.consume(benchmarkedMethod());
        }

        private Object benchmarkedMethod() {
      %4$s
        }
      }
      """;
  private final String imports;
  private final String loadsDeclarations;
  private final String setUpBody;
  private final String benchmarkedMethodBody;
  private String experimentClassContent;
  private File filepath;

  public BenchmarkClass(BenchmarkRequestDTO benchmarkRequest) {
    imports = benchmarkRequest.getImports();
    loadsDeclarations = benchmarkRequest.getLoadsDeclarations();
    setUpBody = benchmarkRequest.getSetUp();
    benchmarkedMethodBody = benchmarkRequest.getBenchmarkedMethodBody();
  }

  private void createExperimentClassContent() {
    // TODO for other options check https://stackoverflow.com/questions/2286648/named-placeholders-in-string-formatting
    if (benchmarkedMethodBody.isBlank()) {
      throw new ValidationException("benchmarked method body must not be empty");
    }
    experimentClassContent = String.format(CONTENT_TEMPLATE,
                                           imports,
                                           loadsDeclarations,
                                           setUpBody,
                                           benchmarkedMethodBody);
  }

  public void writeExperimentClassBodyToFile(File parent) throws BenchmarkClassWriteFailure {
    composeFilepath(parent);
    createExperimentClassContent();
    write();
  }

  private void composeFilepath(File parent) {
    filepath = new File(parent, FILENAME);
  }

  private void write() throws BenchmarkClassWriteFailure {
    filepath.getParentFile().mkdirs();
    try (final OutputStream destination = new FileOutputStream(filepath);
        final Writer writer = new OutputStreamWriter(destination, UTF_8)) {
      writer.write(experimentClassContent); // flushes before autoclose()
    } catch (IOException e) {
      throw new BenchmarkClassWriteFailure(e);
    }
  }
}
