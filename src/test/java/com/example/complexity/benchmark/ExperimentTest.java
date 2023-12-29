package com.example.complexity.benchmark;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class ExperimentTest {

  private static final String TEST_DIRPATH = "/tmp/complexity/test/";
  private static File thisClassDirname;
  private final String imports = "import java.util.Arrays;";
  private final String loadsDeclarations = "public int[] load;";
  private final String setUpBody = """
      load = new int[scope];
      for (int i = 0; i < load.length; i++) {
        load[i] = (int) (Math.random() * scope);
      }""";
  private final String benchmarkedMethodBody = """
      Arrays.sort(load);
      return load;""";
  private final String expected = """
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
  private BenchmarkRequestDTO benchmarkRequestDTO;
  private Experiment experiment;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_DIRPATH + ExperimentTest.class.getSimpleName() + separator;
    thisClassDirname = new File(thisClassPath);
    thisClassDirname.mkdirs();
    assert thisClassDirname.exists();
  }

  @AfterAll
  static void afterAll() {
    assert FileSystemUtils.deleteRecursively(thisClassDirname);
  }

  @BeforeEach
  void setUp() {
    benchmarkRequestDTO = new BenchmarkRequestDTO(imports,
                                                  loadsDeclarations,
                                                  setUpBody,
                                                  benchmarkedMethodBody);
    experiment = new Experiment(benchmarkRequestDTO);

  }
  @Test
  public void sanity() {
    assertTrue(thisClassDirname.exists());
  }

  @Test
  public void throws_when_given_non_existing_parent_path() {
    File projectRoot = new File(thisClassDirname, "project");
    assert !projectRoot.exists();

    Throwable e = assertThrows(ExperimentWriteFailure.class,
                               () -> experiment.writeExperimentClassBodyToFile(projectRoot));

    String expectedMessage = "java.io.FileNotFoundException: %s (No such file or directory)";
    File expectedFile = new File(projectRoot, Experiment.FILENAME);
    assertEquals(String.format(expectedMessage, expectedFile.getPath()), e.getMessage());
  }

  @Test
  public void writes_experiment_class_file_when_given_parent_path()
      throws ExperimentWriteFailure, IOException {
    File projectRoot = new File(thisClassDirname, "project");
    projectRoot.mkdirs();

    experiment.writeExperimentClassBodyToFile(projectRoot);

    File experimentFile = new File(projectRoot, Experiment.FILENAME);
    assertTrue(experimentFile.exists());

    String actual = Files.readString(Paths.get(experimentFile.toURI()), Charset.defaultCharset());
    assertEquals(expected, actual);
  }
}
