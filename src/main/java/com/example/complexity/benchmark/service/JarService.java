package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.exceptions.JarServiceException;
import java.io.File;

public interface JarService {

  File createJar(Benchmark benchmark) throws JarServiceException;
}
