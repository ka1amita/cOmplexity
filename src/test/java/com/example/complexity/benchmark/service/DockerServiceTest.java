package com.example.complexity.benchmark.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.github.dockerjava.api.DockerClient;
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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @BeforeEach
  public void setUp() {
    benchmark = new Benchmark(requestDTO);
    benchmark.setJarFilepath(new File(
        "/Users/kalamita/coding/projects/cOmplexity/build/libs/cOmplexity-0.0.1-SNAPSHOT.jar"));
  }


  @Test
  public void check_docker_client() throws IOException {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        // for BOTH! use forward... script to set up and tear down tcp -> socket forwarding
//        .withDockerHost("tcp://localhost:2375")
        .withDockerHost("unix:///var/run/docker.sock")
        .build();

    System.out.println(config.getDockerHost());

    DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .maxConnections(100)
        .connectionTimeout(Duration.ofSeconds(30))
        .responseTimeout(Duration.ofSeconds(45))
        .build();


    DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    dockerClient.pingCmd().exec();


    Request request = Request.builder()
        .method(Request.Method.GET)
        .path("/_ping")
        .build();

    try (Response response = httpClient.execute(request)) {
      assertThat(response.getStatusCode(), equalTo(200));
      assertThat(IOUtils.toString(response.getBody()), equalTo("OK"));
    }
  }


  @Test
  public void runs_container() {
    //    dockerService.runBenchmark(benchmark);

  }


}