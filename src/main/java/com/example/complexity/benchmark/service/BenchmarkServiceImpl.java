package com.example.complexity.benchmark.service;

import static java.io.File.separator;

import com.example.complexity.benchmark.Benchmark;
import com.example.complexity.benchmark.Experiment;
import com.example.complexity.benchmark.dto.BenchmarkRequestDTO;
import com.example.complexity.benchmark.exceptions.ExperimentWriteFailure;
import com.example.complexity.benchmark.exceptions.GradleTemplateWriteFailure;
import java.io.File;
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

  public BenchmarkServiceImpl(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public void createBenchmark(BenchmarkRequestDTO benchmarkRequestDTO)
      throws GradleTemplateWriteFailure, ExperimentWriteFailure {
    Benchmark benchmark = new Benchmark(benchmarkRequestDTO);

    // set benchmarkID

    benchmark.setProjectRootpath(DEFAULT_PROJECT_ROOTPATH);
    projectService.createProject(benchmark);

    runProject(benchmark);
  }

  private void runProject(Benchmark project) {
    try {
      project.run();
    } catch (ExperimentWriteFailure | GradleTemplateWriteFailure e) {
      throw new RuntimeException(e);
      // TODO return ErrorDTO to client and recover
    }
  }

  private Experiment initExperiment(BenchmarkRequestDTO benchmarkRequestDTO) {
    return new Experiment(benchmarkRequestDTO);
  }
}
