package com.example.complexity.benchmark.service;

import com.example.complexity.benchmark.dto.Benchmark;
import com.example.complexity.benchmark.Jar;
import com.example.complexity.benchmark.exceptions.GradlewCommandFailure;
import com.example.complexity.benchmark.exceptions.JarCommandFailure;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class JarServiceImpl implements JarService {

  @Override
  public File createJar(Benchmark benchmark) throws JarCommandFailure, GradlewCommandFailure {
    return new Jar(benchmark).getJarFilepath();
  }
}
