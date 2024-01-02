package com.example.complexity.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.BenchmarkClassWriteFailure;
import com.example.complexity.benchmark.exceptions.JarServiceException;
import com.example.complexity.benchmark.exceptions.ProjectServiceTemplateWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import com.example.complexity.benchmark.service.JarService;
import com.example.complexity.benchmark.service.JarServiceImpl;
import com.example.complexity.benchmark.service.ProjectService;
import com.example.complexity.benchmark.service.ProjectServiceImpl;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class BenchmarkTest {

  private final BenchmarkRequestDTO requestDTO = new BenchmarkRequestDTO("imports",
                                                                         "loads",
                                                                         "setUp",
                                                                         "body");
  private static final String TEST_DIRNAME = "/tmp/complexity/test/";
  private static File thisClassDirpath;
  ProjectService projectService = new ProjectServiceImpl();
  JarService jarService = new JarServiceImpl();
  private Benchmark benchmark;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_DIRNAME + BenchmarkTest.class.getSimpleName();
    thisClassDirpath = new File(thisClassPath);
    thisClassDirpath.mkdirs();
    assert thisClassDirpath.exists();
  }

  @AfterAll
  static void afterAll() {
        assert FileSystemUtils.deleteRecursively(thisClassDirpath);
  }

  @BeforeEach
  void setUp() {
    benchmark = new Benchmark(requestDTO);
  }

  @Test
  public void throws_if_project_root_already_exists() throws IOException {
    File exists = new File(thisClassDirpath, "exists");
    exists.createNewFile();
    assert exists.exists();

    benchmark.setProjectRootpath(exists);

    Throwable e = assertThrows(ProjectDirectoryAlreadyExists.class,
                               () -> projectService.createProject(benchmark));

    String expectedMessage = "project root path %s already exists";
    assertEquals(String.format(expectedMessage, exists.getAbsolutePath()), e.getMessage());
  }

  @Test
  public void creates_new_project_if_project_root_doesnt_exists()
      throws ProjectServiceTemplateWriteFailure, BenchmarkClassWriteFailure {
    File doesntExist = new File(thisClassDirpath, "doesntExist");
    assert !doesntExist.exists();

    benchmark.setProjectRootpath(doesntExist);
    projectService.createProject(benchmark);
  }

  @Test
  public void writes_project_directories()
      throws BenchmarkClassWriteFailure, ProjectServiceTemplateWriteFailure {
    File projectRoot = new File(thisClassDirpath, "projectRoot");
    assert !projectRoot.exists();
    benchmark.setProjectRootpath(projectRoot);

    projectService.createProject(benchmark);

    assertTrue(projectRoot.exists());
    assertTrue(new File(projectRoot, "src/jmh/java/org/example/BenchmarkClass.java").exists());
    assertTrue(new File(projectRoot, "src/jmh/java/org/example/BenchmarkRunner.java").exists());
    assertTrue(new File(projectRoot, "src/main/java/org/example/Main.java").exists());
    assertTrue(new File(projectRoot, "build.gradle").exists());
    assertTrue(new File(projectRoot, "gradlew").exists());
  }

  @Test
  public void creates_wrapper_and_experiment_jar()
      throws BenchmarkClassWriteFailure, ProjectServiceTemplateWriteFailure, JarServiceException {
    File projectRoot = new File(thisClassDirpath, "experiment");
    assert !projectRoot.exists();

    String imports = "import java.util.Arrays;";
    String loadsDeclarations = "public int[] load;";
    String setUpBody = """
        load = new int[scope];
        for (int i = 0; i < load.length; i++) {
          load[i] = (int) (Math.random() * scope);
        }""";
    String benchmarkedMethodBody = """
        Arrays.sort(load);
        return load;""";
    Benchmark benchmark = new Benchmark(new BenchmarkRequestDTO(imports,
                                                                loadsDeclarations,
                                                                setUpBody,
                                                                benchmarkedMethodBody));
    benchmark.setProjectRootpath(projectRoot);

    projectService.createProject(benchmark);
    jarService.createJar(benchmark);
    File dotGradle = new File(projectRoot, ".gradle/");
    File gradle = new File(projectRoot, "gradle/");
    File gradlew = new File(projectRoot, "gradlew");
    File jar = new File(projectRoot, "build/libs/experiment-1.0-SNAPSHOT-jmh.jar");
    assertTrue(dotGradle.exists());
    assertTrue(gradle.exists());
    assertTrue(gradlew.exists());
    assertTrue(jar.exists());
  }
}
