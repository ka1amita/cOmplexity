package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.Benchmark;
import java.io.File;

public interface JarService {

  File createJar(Benchmark benchmark);
}
