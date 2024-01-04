package com.example.complexity.benchmark.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DockerJavaAPITest {

  private static final String PROJECT_ROOT = System.getProperty("user.dir") + "/";
  private static final String JAR_FILENAME = "benchmark-1.0-SNAPSHOT-jmh.jar";
  private static final String JAR_FILEPATH = PROJECT_ROOT + JAR_FILENAME;
  private static final String IMAGE_TAG = "complexity:test";
  private static final String DOCKER_SOCKET_PATH = "unix:///var/run/docker.sock";
  private static final String TEST_STATIC_DIRPATH = "src/test/resources/static/";
  private static final String HOST_MOUNT = PROJECT_ROOT + TEST_STATIC_DIRPATH;
  private static final String DOCKERFILE_PATH = TEST_STATIC_DIRPATH + "Dockerfile";
  private static final String DOCKER_MOUNT = "/complexity/";
  // for BOTH! use forward... script to set up and tear down tcp -> socket forwarding
  //  private static final String DOCKER_SOCKET_PATH = "tcp://localhost:2375";
  private DockerClient dockerClient;
  private CreateContainerResponse container;
  private String imageId;
  Set<File> filesToBeDeleted;

  @BeforeAll
  public static void init() {
    // TODO run the forward_tcp_to_dockerd_socker.sh
  }

  @AfterAll
  public static void destroy() {
    // TODO stop forwarding
  }

  @BeforeEach
  public void setUp() {
    dockerClient = getDockerClient();
    filesToBeDeleted = new HashSet<>();
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

    filesToBeDeleted.forEach(File::delete);
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
                 () -> execAndGetImageId(TEST_STATIC_DIRPATH + "emptyDockerfile", IMAGE_TAG));
  }

  @Test
  public void throws_when_Dockerfile_is_missing() {
    assertThrows(IllegalArgumentException.class,
                 () -> execAndGetImageId(TEST_STATIC_DIRPATH + "missingDockerfile", IMAGE_TAG));
  }

  @Test
  public void throws_when_Dockerfile_is_corrupted() {
    assertThrows(BadRequestException.class,
                 () -> execAndGetImageId(TEST_STATIC_DIRPATH + "corruptDockerfile", IMAGE_TAG));
  }

  @Test
  public void builds_image_from_a_local_Dockerfile_and_finds_it_among_images() {
    imageId = execAndGetImageId(DOCKERFILE_PATH, IMAGE_TAG);
    List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(IMAGE_TAG).exec();

    assertNotNull(imageId);
    assertFalse(images.isEmpty());
  }

  @Test
  public void builds_simple_image_with_API() {
    container = dockerClient.createContainerCmd("amazoncorretto:17")
        .withCmd("java", "-version")
        .exec();

    dockerClient.startContainerCmd(container.getId()).exec();

    Integer statusCode = dockerClient.waitContainerCmd(container.getId())
        .exec(new WaitContainerResultCallback())
        .awaitStatusCode();
    assertEquals(0, statusCode, "Container did not exit successfully");
  }

  @Test
  public void container_reads_and_writes_to_a_file_on_host() throws IOException {
    Bind inputBind = new Bind(HOST_MOUNT, new Volume(DOCKER_MOUNT));
    HostConfig hostConfig = new HostConfig().withBinds(inputBind);
    String writeFilename = "readwrite.test";
    String readFilename = "read.test";
    File readFilepath = new File(HOST_MOUNT, readFilename);
    Files.writeString(readFilepath.toPath(), "test");
    filesToBeDeleted.add(readFilepath);

    container = dockerClient.createContainerCmd("amazoncorretto:17")
        .withCmd("/bin/sh", "-c",
                 "cat " + DOCKER_MOUNT + readFilename + " > " + DOCKER_MOUNT + writeFilename)
        .withHostConfig(hostConfig)
        .exec();

    dockerClient.startContainerCmd(container.getId()).exec();

    Integer statusCode = dockerClient.waitContainerCmd(container.getId())
        .exec(new WaitContainerResultCallback())
        .awaitStatusCode();

    File writeFilepath = new File(HOST_MOUNT, writeFilename);
    filesToBeDeleted.add(writeFilepath);
    String output = Files.readString(writeFilepath.toPath());

    assertTrue(writeFilepath.exists(), "Output file doesn't exist");
    assertFalse(output.isEmpty(), "Output file is empty");
    assertEquals(0, statusCode, "Container did not exit successfully");
  }

  @Test
  public void container_writes_to_a_file_on_host() throws IOException {
    Bind inputBind = new Bind(HOST_MOUNT,
                              new Volume(DOCKER_MOUNT));
    HostConfig hostConfig = new HostConfig().withBinds(inputBind);
    String outputFilename = "write.test";

    container = dockerClient.createContainerCmd("amazoncorretto:17")
        .withCmd("touch", DOCKER_MOUNT + outputFilename)
        .withCmd("/bin/sh", "-c", "echo success > " + DOCKER_MOUNT + outputFilename)
        .withHostConfig(hostConfig)
        .exec();

    dockerClient.startContainerCmd(container.getId()).exec();

    File outputFilepath = new File("build/libs/" + outputFilename);
    filesToBeDeleted.add(outputFilepath);

    String output = Files.readString(outputFilepath.toPath());
    Integer statusCode = dockerClient.waitContainerCmd(container.getId())
        .exec(new WaitContainerResultCallback())
        .awaitStatusCode();

    assertTrue(outputFilepath.exists(), "Output file doesn't exist");
    assertFalse(output.isEmpty(), "Output file is empty");
    assertEquals(0, statusCode, "Container did not exit successfully");
  }

  @Test
  public void container_runs_benchmark_jar_and_writes_results_to_local_files() throws IOException {
    Bind inputBind = new Bind("/Users/kalamita/coding/projects/cOmplexity/build/libs/",
                              new Volume("/complexity"));

    HostConfig hostConfig = new HostConfig().withBinds(inputBind);

    String resultsJsonFilename = "jmh-results.json";
    String resultsTxtFilename = "jmh-results.txt";

    container = dockerClient.createContainerCmd("amazoncorretto:17")
        .withHostConfig(hostConfig)
        .withCmd("java", "-jar", DOCKER_MOUNT + JAR_FILENAME, "-tu=ns", "-bm=ss",
                 "-rf=json", "-rff=" + DOCKER_MOUNT + resultsJsonFilename,
                 "-o=" + DOCKER_MOUNT + resultsTxtFilename)
        .exec();

    dockerClient.startContainerCmd(container.getId()).exec();

    Integer statusCode = dockerClient.waitContainerCmd(container.getId())
        .exec(new WaitContainerResultCallback())
        .awaitStatusCode();
    File resultsJsonFilepath = new File("build/libs/" + resultsJsonFilename);
    File resultsTxtFilepath = new File("build/libs/" + resultsTxtFilename);
    filesToBeDeleted.add(resultsTxtFilepath);
    filesToBeDeleted.add(resultsJsonFilepath);
    String resultsTxt = Files.readString(resultsJsonFilepath.toPath());
    String resultsJson = Files.readString(resultsTxtFilepath.toPath());

    assertFalse(resultsTxt.isEmpty());
    assertFalse(resultsJson.isEmpty());
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
    //        dockerClient.startContainerCmd(container.getId()).exec(); //actually start the container!
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
        //        .connectionTimeout(Duration.ofSeconds(30))
        //        .responseTimeout(Duration.ofSeconds(45))
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
