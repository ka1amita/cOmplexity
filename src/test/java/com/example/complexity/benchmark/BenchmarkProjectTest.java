package com.example.complexity.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class BenchmarkProjectTest {

  private static final String TEST_DIRNAME = "/tmp/complexity/test/";
  private static File thisClassDirpath;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_DIRNAME + BenchmarkProjectTest.class.getSimpleName();
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
  }

  @Test
  public void throws_if_project_root_already_exists() throws IOException {
    File exists = new File(thisClassDirpath, "exists");
    exists.createNewFile();
    assert exists.exists();

    String expectedMessage = "project root path %s already exists";
    Throwable e = assertThrows(ProjectDirectoryAlreadyExists.class, () -> new BenchmarkProject(exists));
    assertEquals(String.format(expectedMessage, exists.getAbsolutePath()), e.getMessage());
  }

  @Test
  public void creates_new_project_if_project_root_doesnt_exists() {
    File doesntExist = new File(thisClassDirpath, "doesntExist");
    assert !doesntExist.exists();

    new BenchmarkProject(doesntExist);
  }

  @Test
  public void creates_project_directory_when_run()
      throws ExperimentWriteFailure, GradleTemplateWriteFailure {
    File projectRoot = new File(thisClassDirpath, "projectRoot");
    assert !projectRoot.exists();
    BenchmarkProject project = new BenchmarkProject(projectRoot);
    project.setGradleTemplate(new GradleTemplate());
    project.setExperiment(new Experiment(new BenchmarkRequestDTO("imports",
                                                                 "loads",
                                                                 "setUp",
                                                                 "body")));

    project.run();

    assertTrue(projectRoot.exists());
    assertTrue(new File(projectRoot, "src/jmh/java/org/example/Experiment.java").exists());
    assertTrue(new File(projectRoot, "src/jmh/java/org/example/BenchmarkRunner.java").exists());
    assertTrue(new File(projectRoot, "src/main/java/org/example/Main.java").exists());
    assertTrue(new File(projectRoot, "build.gradle").exists());
    assertTrue(new File(projectRoot, "gradlew").exists());
  }

  @Test
  public void creates_wrapper_and_experiment_jar()
      throws ExperimentWriteFailure, GradleTemplateWriteFailure {
    File projectRoot = new File(thisClassDirpath, "experiment");
    assert !projectRoot.exists();

    BenchmarkProject project = new BenchmarkProject(projectRoot);
    project.setGradleTemplate(new GradleTemplate());
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

    project.setExperiment(new Experiment(new BenchmarkRequestDTO(imports,
                                                                 loadsDeclarations,
                                                                 setUpBody,
                                                                 benchmarkedMethodBody)));
    project.run();

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
