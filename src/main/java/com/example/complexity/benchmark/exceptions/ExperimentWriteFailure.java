package com.example.complexity.benchmark.exceptions;

import com.example.complexity.benchmark.Experiment;

public class ExperimentWriteFailure extends ExperimentException {

  public ExperimentWriteFailure(Throwable cause) {
    super(cause);
  }
}
