package com.example.complexity.benchmark.service;

import static java.io.File.separator;

import com.example.complexity.benchmark.BenchmarkProject;
import com.example.complexity.benchmark.Experiment;
import com.example.complexity.benchmark.GradleTemplate;
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

  private static final String DEFAULT_PROJECT_ROOT_DIRPATH =
      separator + "tmp" + separator + "complexity" + separator;

  @Override
  public void createBenchmark(BenchmarkRequestDTO benchmarkRequestDTO) {
    Experiment experiment = initExperiment(benchmarkRequestDTO);
    BenchmarkProject project = initProject();
    setGradleTemplateToProject(new GradleTemplate(), project);
    setExperimentToProject(experiment, project);
    runProject(project);
  }

  private void setGradleTemplateToProject(GradleTemplate gradleTemplate, BenchmarkProject project) {
    project.setGradleTemplate(gradleTemplate);
  }

  private BenchmarkProject initProject() {
    File projectRoot = new File(DEFAULT_PROJECT_ROOT_DIRPATH);
    return new BenchmarkProject(projectRoot);
  }

  private void setExperimentToProject(Experiment experiment, BenchmarkProject project) {
    project.setExperiment(experiment);
  }

  private void runProject(BenchmarkProject project) {
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
