package com.example.complexity.benchmark.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DockerServiceTest {

  private static final String TEST_DIRNAME = "/tmp/complexity/test/";
  private static final String IMAGE_TAG = "complexity:test";
  private static final String DOCKERFILE_PATH = "src/test/resources/static/Dockerfile";
  private static final String DOCKER_SOCKET_PATH = "unix:///var/run/docker.sock";
  private static final String JAR_FILEPATH =
      "/Users/kalamita/coding/projects/cOmplexity/build/libs/cOmplexity-0.0.1-SNAPSHOT.jar";
  // for BOTH! use forward... script to set up and tear down tcp -> socket forwarding
  //  private static final String DOCKER_SOCKET_PATH = "tcp://localhost:2375";
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
