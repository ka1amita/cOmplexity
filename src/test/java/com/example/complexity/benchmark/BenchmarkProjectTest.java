package com.example.complexity.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.BenchmarkProjectException;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class BenchmarkProjectTest {

  private static final String TEST_ROOT_PATH = "/tmp/complexity/test/";
  private static File thisClassRoot;

  private BenchmarkRequestDTO benchmarkRequestDTO;
  private BenchmarkProject project;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_ROOT_PATH + BenchmarkProjectTest.class.getSimpleName();
    thisClassRoot = new File(thisClassPath);
    thisClassRoot.mkdirs();
    assert thisClassRoot.exists();
  }

  @AfterAll
  static void afterAll() {
    assert FileSystemUtils.deleteRecursively(thisClassRoot);
  }

  @BeforeEach
  void setUp() {
    benchmarkRequestDTO = new BenchmarkRequestDTO("", "", "", "");
  }

  @Test
  public void throws_if_project_root_already_exists() throws IOException {
    File exists = new File(thisClassRoot, "exists");
    exists.createNewFile();
    assert exists.exists();

    String expectedMessage = "project root path %s already exists";
    Throwable e = assertThrows(BenchmarkProjectException.class, () -> new BenchmarkProject(exists));
    assertEquals(String.format(expectedMessage, exists.getAbsolutePath()), e.getMessage());
  }

  @Test
  public void creates_new_project_if_project_root_doesnt_exists() throws IOException {
    File doesntExist = new File(thisClassRoot, "doesntExist");
    assert !doesntExist.exists();
    BenchmarkProject project = new BenchmarkProject(doesntExist);
  }
}
