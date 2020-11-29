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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PosixAutoConfigurator implements AutoConfigurator {

  // TODO check if mercurial works

  private static final Logger LOG = LoggerFactory.getLogger(PosixAutoConfigurator.class);

  private static final List<String> ADDITIONAL_PATH = ImmutableList.of(
    "/usr/bin",
    "/usr/local/bin",
    "/opt/local/bin"
  );

  private final Set<String> fsPaths;

  PosixAutoConfigurator(Map<String, String> env) {
    this(env, ADDITIONAL_PATH);
  }

  PosixAutoConfigurator(Map<String, String> env, List<String> additionalPaths) {
    String path = env.getOrDefault("PATH", "");
    fsPaths = new LinkedHashSet<>();
    fsPaths.addAll(Splitter.on(File.pathSeparator).splitToList(path));
    fsPaths.addAll(additionalPaths);
  }

  @Override
  public HgConfig configure() {
    Optional<Path> hg = findInPath();
    if (hg.isPresent()) {
      return configure(hg.get());
    }
    return new HgConfig();
  }

  private Optional<Path> findInPath() {
    for (String directory : fsPaths) {
      Path binaryPath = Paths.get(directory, "hg");
      if (Files.exists(binaryPath)) {
        return Optional.of(binaryPath);
      }
    }
    return Optional.empty();
  }

  @Override
  public HgConfig configure(Path hg) {
    HgConfig config = new HgConfig();
    if (Files.exists(hg)) {
      configureWithExistingHg(hg, config);
    } else {
      LOG.warn("{} does not exists", hg);
    }
    return config;
  }

  private void configureWithExistingHg(Path hg, HgConfig config) {
    config.setHgBinary(hg.toAbsolutePath().toString());
  }

}
