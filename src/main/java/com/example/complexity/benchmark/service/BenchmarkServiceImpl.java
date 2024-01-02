package com.example.complexity.benchmark.service;

import static java.io.File.separator;

import com.example.complexity.benchmark.Benchmark;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * directs all the high level benchmarking steps from querying new Benchmark to returning response
 * to controller
 */
@Service
public class BenchmarkServiceImpl implements BenchmarkService {

  static private final File DEFAULT_PROJECT_ROOTPATH = new File(
      separator + "tmp" + separator + "complexity" + separator);

  private final ProjectService projectService;
  private final JarService jarService;

  @Autowired
  public BenchmarkServiceImpl(ProjectService projectService, JarService jarService) {
    this.projectService = projectService;
    this.jarService = jarService;
  }

  @Override
  public void createBenchmark(BenchmarkRequestDTO benchmarkRequestDTO)
      throws GradleTemplateWriteFailure, ExperimentWriteFailure {
    Benchmark benchmark = new Benchmark(benchmarkRequestDTO);

    // set benchmarkID

    benchmark.setProjectRootpath(DEFAULT_PROJECT_ROOTPATH);
    projectService.createProject(benchmark);

    File jarFilepath = jarService.createJar(benchmark);
    benchmark.setJarFilepath(jarFilepath);

    //    dockerize()
    //    evaluate()
    //    close()
    //    runProject(benchmark);
    // TODO autoclosable and use try with resources
    // TODO return ErrorDTO to client and recover
  }
}
