package com.example.complexity.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.ProjectDirectoryAlreadyExists;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class BenchmarkProjectTest {

  private static final String TEST_ROOT_PATH = "/tmp/complexity/test/";
  private static File thisClassDirname;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_ROOT_PATH + BenchmarkProjectTest.class.getSimpleName();
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
  }

  @Test
  public void throws_if_project_root_already_exists() throws IOException {
    File exists = new File(thisClassDirname, "exists");
    exists.createNewFile();
    assert exists.exists();

    String expectedMessage = "project root path %s already exists";
    Throwable e = assertThrows(ProjectDirectoryAlreadyExists.class, () -> new BenchmarkProject(exists));
    assertEquals(String.format(expectedMessage, exists.getAbsolutePath()), e.getMessage());
  }

  @Test
  public void creates_new_project_if_project_root_doesnt_exists() {
    File doesntExist = new File(thisClassDirname, "doesntExist");
    assert !doesntExist.exists();

    new BenchmarkProject(doesntExist);
  }

  @Test
  public void creates_project_directory_including_experiment_file_when_run() throws ExperimentWriteFailure {
    File doesntExist = new File(thisClassDirname, "doesntExist");
    assert !doesntExist.exists();

    BenchmarkProject project = new BenchmarkProject(doesntExist);
    project.setExperiment(new Experiment(new BenchmarkRequestDTO("imports",
                                                                 "loads",
                                                                 "setUp",
                                                                 "body")));
    project.run();

    assertTrue(doesntExist.exists());
  }
}
