/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.autoconfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.MoreFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PosixAutoConfigurator implements AutoConfigurator {

  private static final Logger LOG = LoggerFactory.getLogger(PosixAutoConfigurator.class);

  private static final List<String> ADDITIONAL_PATH = ImmutableList.of(
    "/usr/bin",
    "/usr/local/bin",
    "/opt/local/bin"
  );

  private final Set<String> fsPaths;

  private Executor executor = (Path binary, String... args) -> {
    ProcessBuilder builder = new ProcessBuilder(
      Lists.asList(binary.toString(), args).toArray(new String[0])
    );
    Process process = builder.start();
    int rc = process.waitFor();
    if (rc != 0) {
      throw new IOException(binary.toString() + " failed with return code " + rc);
    }
    return process.getInputStream();
  };

  PosixAutoConfigurator(Map<String, String> env) {
    this(env, ADDITIONAL_PATH);
  }

  PosixAutoConfigurator(Map<String, String> env, List<String> additionalPaths) {
    String path = env.getOrDefault("PATH", "");
    fsPaths = new LinkedHashSet<>();
    fsPaths.addAll(Splitter.on(File.pathSeparator).splitToList(path));
    fsPaths.addAll(additionalPaths);
  }

  @VisibleForTesting
  void setExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public HgConfig configure() {
    Optional<Path> hg = findInPath("hg");
    if (hg.isPresent()) {
      return configure(hg.get());
    }
    return new HgConfig();
  }

  private Optional<Path> findInPath(String binary) {
    for (String directory : fsPaths) {
      Path binaryPath = Paths.get(directory, binary);
      if (Files.exists(binaryPath)) {
        return Optional.of(binaryPath);
      }
    }
    return Optional.empty();
  }


  private Optional<Path> findModulePath(Path hg) {
    if (!Files.isExecutable(hg)) {
      LOG.warn("{} is not executable", hg);
      return Optional.empty();
    }
    try {
      InputStream debuginstall = executor.execute(hg, "debuginstall");
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(debuginstall))) {
        while (reader.ready()) {
          String line = reader.readLine();
          if (line.contains("installed modules")) {
            int start = line.indexOf("(");
            int end = line.indexOf(")");
            Path modulePath = Paths.get(line.substring(start + 1, end));
            if (Files.exists(modulePath)) {
              // installed modules contains the path to the mercurial module,
              // but we need the parent for the python path
              return Optional.of(modulePath.getParent());
            } else {
              LOG.warn("could not find module path at {}", modulePath);
            }
          }
        }
      }
    } catch (IOException ex) {
      LOG.warn("failed to parse debuginstall of {}", hg);
    } catch (InterruptedException e) {
      LOG.warn("interrupted during debuginstall parsing of {}", hg);
      Thread.currentThread().interrupt();
    }
    return Optional.empty();
  }

  @Override
  public HgConfig configure(Path hg) {
    HgConfig config = new HgConfig();
    try {
      if (Files.exists(hg)) {
        configureWithExistingHg(hg, config);
      } else {
        LOG.warn("{} does not exists", hg);
      }
    } catch (IOException e) {
      LOG.warn("failed to read first line of {}", hg);
    }
    return config;
  }

  private void configureWithExistingHg(Path hg, HgConfig config) throws IOException {
    config.setHgBinary(hg.toAbsolutePath().toString());
    Optional<Path> pythonFromShebang = findPythonFromShebang(hg);
    if (pythonFromShebang.isPresent()) {
      config.setPythonBinary(pythonFromShebang.get().toAbsolutePath().toString());
    } else {
      LOG.warn("could not find python from shebang, searching for python in path");
      Optional<Path> python = findInPath("python");
      if (!python.isPresent()) {
        LOG.warn("could not find python in path, searching for python3 instead");
        python = findInPath("python3");
      }
      if (python.isPresent()) {
        config.setPythonBinary(python.get().toAbsolutePath().toString());
      } else {
        LOG.warn("could not find python in path");
      }
    }

    Optional<Path> modulePath = findModulePath(hg);
    if (modulePath.isPresent()) {
      config.setPythonPath(modulePath.get().toAbsolutePath().toString());
    } else {
      LOG.warn("could not find module path");
    }

  }

  private Optional<Path> findPythonFromShebang(Path hg) throws IOException {
    String shebang = MoreFiles.asCharSource(hg, StandardCharsets.UTF_8).readFirstLine();
    if (shebang != null && shebang.startsWith("#!")) {
      String substring = shebang.substring(2);
      String[] parts = substring.split("\\s+");
      if (parts.length > 1) {
        return findInPath(parts[1]);
      } else {
        Path python = Paths.get(parts[0]);
        if (Files.exists(python)) {
          return Optional.of(python);
        } else {
          LOG.warn("python binary from shebang {} does not exists", python);
        }
      }
    } else {
      LOG.warn("first line does not look like a shebang: {}", shebang);
    }
    return Optional.empty();
  }

  @FunctionalInterface
  interface Executor {
    InputStream execute(Path binary, String... args) throws IOException, InterruptedException;
  }
}
