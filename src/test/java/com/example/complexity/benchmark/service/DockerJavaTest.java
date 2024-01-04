package com.example.complexity.benchmark.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DockerJavaTest {

  private static final String IMAGE_TAG = "complexity:test";
  private static final String DOCKERFILE_PATH = "src/test/resources/static/Dockerfile";
  private static final String DOCKER_SOCKET_PATH = "unix:///var/run/docker.sock";
  // for BOTH! use forward... script to set up and tear down tcp -> socket forwarding
  //  private static final String DOCKER_SOCKET_PATH = "tcp://localhost:2375";
  private DockerClient dockerClient;
  private CreateContainerResponse container;
  private String imageId;

  @BeforeAll
  public static void init() {
    // run the forward_tcp_to_dockerd_socker.sh
  }

  @AfterAll
  public static void destroy() {
    // stop forwarding
  }

  @BeforeEach
  public void setUp() {
    dockerClient = getDockerClient();
  }

  @AfterEach
  public void tearDown() throws IOException {
    if (container != null) {
      dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
    }

    if (imageId != null) {
      dockerClient.removeImageCmd(imageId).withForce(true).exec();
    }

    dockerClient.close(); //autoclosable!
  }

  @Test
  public void ping_docker_client_and_print_version() {
    dockerClient.pingCmd().exec();
    System.out.println(dockerClient.versionCmd().exec());
  }

  @Test
  public void engine_responses_ok_to_ping_request() throws IOException {
    DockerHttpClient httpClient = getDockerHttpClient();

    try (Response response = httpClient.execute(getPingRequest())) {
      assertThat(response.getStatusCode(), equalTo(200));
      assertThat(IOUtils.toString(response.getBody()), equalTo("OK"));
    }
  }

  @Test
  public void throws_when_Dockerfile_is_empty() {
    assertThrows(BadRequestException.class,
                 () -> execAndGetImageId("src/test/resources/static/emptyDockerfile", IMAGE_TAG));
  }

  @Test
  public void throws_when_Dockerfile_is_missing() {
    assertThrows(IllegalArgumentException.class,
                 () -> execAndGetImageId("src/test/resources/static/missingDockerfile", IMAGE_TAG));
  }

  @Test
  public void throws_when_Dockerfile_is_corrupted() {
    assertThrows(BadRequestException.class,
                 () -> execAndGetImageId("src/test/resources/static/corruptDockerfile", IMAGE_TAG));
  }

  @Test
  public void builds_image_from_a_local_Dockerfile_and_finds_it_among_images() {
    imageId = execAndGetImageId(DOCKERFILE_PATH, IMAGE_TAG);
    List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(IMAGE_TAG).exec();

    assertNotNull(imageId);
    assertFalse(images.isEmpty());
  }



  @Test
  public void throws_when_image_is_missing() {
    DockerClient dockerClient = getDockerClient();
    CreateContainerCmd command = dockerClient.createContainerCmd("missing_image");
    assertThrows(NotFoundException.class, command::exec);
  }

  @Test
  public void container_runs_from_image_and_is_found_() {
    CreateContainerCmd command = dockerClient.createContainerCmd(IMAGE_TAG);
    imageId = execAndGetImageId();
    container = command.exec();

    List<Container> containers =
        dockerClient.listContainersCmd().withShowAll(true)
            .withIdFilter(Collections.singleton(container.getId())).exec();

    assertFalse(containers.isEmpty());
    assertFalse(container.getId().isBlank());
    assertTrue(container.getWarnings().length == 0);
  }

  @Test
  public void container_X() {
    CreateContainerCmd command = dockerClient.createContainerCmd(IMAGE_TAG);
    imageId = execAndGetImageId();
    container = command.exec();

    //     TODO TEST/USE:
    //    dockerClient.execStartCmd();
    //    dockerClient.execCreateCmd();
    //        dockerClient.startContainerCmd(container.getId()).exec();
    assertFalse(container.getId().isBlank());
    assertTrue(container.getWarnings().length == 0);
  }

  private DockerHttpClient getDockerHttpClient() {
    DockerClientConfig config = getDockerClientConfig();
    return getDockerHttpClient(config);
  }

  private Ports getPortBindings() {
    ExposedPort tcp80 = ExposedPort.tcp(80);
    Ports portBindings = new Ports();
    portBindings.bind(tcp80, Ports.Binding.bindPort(8080));
    return portBindings;
  }

  private String execAndGetImageId() {
    return execAndGetImageId(DOCKERFILE_PATH, IMAGE_TAG);
  }

  private String execAndGetImageId(String dockerfile, String imageTag) {
    return dockerClient.buildImageCmd(new File(dockerfile))
        .withNoCache(true)
        .withTags(Set.of(imageTag))
        .exec(new BuildImageResultCallback() {
          @Override
          public void onNext(BuildResponseItem item) {
            System.out.println("" + item);
            super.onNext(item);
          }
        })
        .awaitImageId();
  }

  private DockerClient getDockerClient() {
    DockerClientConfig config = getDockerClientConfig();
    DockerHttpClient httpClient = getDockerHttpClient(config);
    return DockerClientImpl.getInstance(config, httpClient);
  }

  private DockerClientConfig getDockerClientConfig() {
    return DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost(DOCKER_SOCKET_PATH)
        .build();
  }

  private DockerHttpClient getDockerHttpClient(DockerClientConfig config) {
    return new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .maxConnections(100)
        .connectionTimeout(Duration.ofSeconds(30))
        .responseTimeout(Duration.ofSeconds(45))
        .build();
  }

  private Request getPingRequest() {
    Request request = Request.builder()
        .method(Request.Method.GET)
        .path("/_ping")
        .build();
    return request;
  }
}
