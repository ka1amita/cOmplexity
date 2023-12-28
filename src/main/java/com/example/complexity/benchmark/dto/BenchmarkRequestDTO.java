package com.example.complexity.benchmark.dto;

public class BenchmarkRequestDTO {
  private final String imports;
  private final String loadsDeclarations;
  private final String setUpBody;
  private final String benchmarkedMethodBody;

  public BenchmarkRequestDTO(String imports, String loadsDeclarations, String setUpBody,
      String benchmarkedMethodBody) {
    this.imports = imports;
    this.loadsDeclarations = loadsDeclarations;
    this.setUpBody = setUpBody;
    this.benchmarkedMethodBody = benchmarkedMethodBody;
  }

  public String getImports() {
    return imports;
  }

  public String getLoadsDeclarations() {
    return loadsDeclarations;
  }

  public String getSetUp() {
    return setUpBody;
  }

  public String getBenchmarkedMethodBody() {
    return benchmarkedMethodBody;
  }
}
