package com.example.complexity.benchmark;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import jakarta.validation.ValidationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.springframework.validation.annotation.Validated;

@Validated
public class ExperimentTemplate {

  public static final String ASSEMBLY_PATH =
      System.getProperty("user.dir") + File.separatorChar + "assembly" + File.separatorChar;
  private static final String JAVA_SUFFIX = ".java";
  private static final String FILEPATH = ASSEMBLY_PATH + "Experiment" + JAVA_SUFFIX;
  private static final String CLASS_DEFINITION_TEMPLATE = """
      %1$s
      import org.openjdk.jmh.annotations.Benchmark;
      import org.openjdk.jmh.annotations.Level;
      import org.openjdk.jmh.annotations.Param;
      import org.openjdk.jmh.annotations.Scope;
      import org.openjdk.jmh.annotations.Setup;
      import org.openjdk.jmh.annotations.State;
      import org.openjdk.jmh.infra.Blackhole;

      @State(Scope.Benchmark)
      public class Experiment {

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
  private String experimentClassBody;
  private String filepath;

  public ExperimentTemplate(BenchmarkRequestDTO benchmarkRequest) {
    imports = benchmarkRequest.getImports();
    loadsDeclarations = benchmarkRequest.getLoadsDeclarations();
    setUpBody = benchmarkRequest.getSetUp();
    benchmarkedMethodBody = benchmarkRequest.getBenchmarkedMethodBody();
  }

  public String getExperimentClassBody() {
    return experimentClassBody;
  }

  private void createExperimentClassBody() {
    // TODO for other options check https://stackoverflow.com/questions/2286648/named-placeholders-in-string-formatting
    if (benchmarkedMethodBody.isBlank()) {
      throw new ValidationException("benchmarked method body must not be empty");
    }
    experimentClassBody = String.format(CLASS_DEFINITION_TEMPLATE,
                         imports,
                         loadsDeclarations,
                         setUpBody,
                         benchmarkedMethodBody);
  }

  public void writeExperimentClassToFile() throws IOException {
    createExperimentClassBody();
    setFilepath();
    // TODO create parent
    writeToFile();
  }

  private void setFilepath() {
    filepath = FILEPATH;
  }

  private void writeToFile() throws IOException {
    try (final OutputStream destination = new FileOutputStream(filepath);
        final Writer writer = new OutputStreamWriter(destination, UTF_8)) {
      writer.write(experimentClassBody);
    }
  }
}
