package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;

class DockerServiceTest {

  private static final String TEST_DIRNAME = "/tmp/complexity/test/";
  private static File thisClassDirpath;
  private final BenchmarkRequestDTO requestDTO = new BenchmarkRequestDTO("imports",
                                                                         "loads",
                                                                         "setUp",
                                                                         "body");
  DockerService dockerService = new DockerServiceImpl();
  private Benchmark benchmark;

  @BeforeAll
  static void beforeAll() {
    String thisClassPath = TEST_DIRNAME + DockerServiceTest.class.getSimpleName();
    thisClassDirpath = new File(thisClassPath);
    thisClassDirpath.mkdirs();
    assert thisClassDirpath.exists();
  }
}
