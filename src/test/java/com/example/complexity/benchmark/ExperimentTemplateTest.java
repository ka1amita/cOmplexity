package com.example.complexity.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import org.junit.jupiter.api.Test;

class ExperimentTemplateTest {

  private String imports = "import java.util.Arrays;";
  private String loadsDeclarations = "public int[] load;";
  private String setUpBody = """
      load = new int[scope];
      for (int i = 0; i < load.length; i++) {
        load[i] = (int) (Math.random() * scope);
      }
      """;
  private String benchmarkedMethodBody = """
      Arrays.sort(load);
      return load;
      """;

  @Test
  public void sanity() {
    new ExperimentTemplate(new BenchmarkRequestDTO("", "", "", ""));
  }

  @Test
  public void creates_experiment_class_body_when_given_benchmarkRequestDTO_with_all_definitions() {
    imports = "import java.util.Arrays;";
    loadsDeclarations = "public int[] load;";
    setUpBody = """
        load = new int[scope];
        for (int i = 0; i < load.length; i++) {
          load[i] = (int) (Math.random() * scope);
        }""";
    benchmarkedMethodBody = """
        Arrays.sort(load);
        return load;""";

    BenchmarkRequestDTO benchmarkRequestDTO = new BenchmarkRequestDTO(imports,
                                                                      loadsDeclarations,
                                                                      setUpBody,
                                                                      benchmarkedMethodBody);
    ExperimentTemplate experimentTemplate = new ExperimentTemplate(benchmarkRequestDTO);
    experimentTemplate.createExperimentClassFile();
    String actual = experimentTemplate.getExperimentClassBody();
    String expected = """
        import java.util.Arrays;
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
                
        public int[] load;
                
          @Setup(Level.Invocation)
          public void setUp() {
        load = new int[scope];
        for (int i = 0; i < load.length; i++) {
          load[i] = (int) (Math.random() * scope);
        }
          }
                
          @Benchmark
          public void benchmark(Blackhole blackhole) {
            blackhole.consume(benchmarkedMethod());
          }
                
          private Object benchmarkedMethod() {
        Arrays.sort(load);
        return load;
          }
        }
        """;

    assertEquals(expected, actual);
  }

  @Test
  public void throws_when_given_benchmarkRequestDto_with_empty_benchmarked_method_body() {
    imports = "import java.util.Arrays;";
    loadsDeclarations = "public int[] load;";
    setUpBody = """
        load = new int[scope];
        for (int i = 0; i < load.length; i++) {
          load[i] = (int) (Math.random() * scope);
        }""";
    benchmarkedMethodBody = "";
    BenchmarkRequestDTO benchmarkRequestDTO = new BenchmarkRequestDTO(imports,
                                                                      loadsDeclarations,
                                                                      setUpBody,
                                                                      benchmarkedMethodBody);
    ExperimentTemplate experimentTemplate = new ExperimentTemplate(benchmarkRequestDTO);

    assertThrows(Exception.class, experimentTemplate::createExperimentClassFile);
  }
}
