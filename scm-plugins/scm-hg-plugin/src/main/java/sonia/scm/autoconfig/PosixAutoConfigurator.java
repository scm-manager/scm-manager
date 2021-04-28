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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgVerifier;

import java.io.File;
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

  private final HgVerifier verifier;
  private final Set<String> fsPaths;

  PosixAutoConfigurator(HgVerifier verifier, Map<String, String> env) {
    this(verifier, env, ADDITIONAL_PATH);
  }

  @VisibleForTesting
  PosixAutoConfigurator(HgVerifier verifier, Map<String, String> env, List<String> additionalPaths) {
    this.verifier = verifier;
    String path = env.getOrDefault("PATH", "");
    fsPaths = new LinkedHashSet<>();
    fsPaths.addAll(Splitter.on(File.pathSeparator).splitToList(path));
    fsPaths.addAll(additionalPaths);
  }

  @Override
  public void configure(HgGlobalConfig config) {
    Optional<Path> hg = findInPath();
    if (hg.isPresent()) {
      config.setHgBinary(hg.get().toAbsolutePath().toString());
    } else {
      LOG.warn("could not find valid mercurial installation");
    }
  }

  private Optional<Path> findInPath() {
    for (String directory : fsPaths) {
      Path binaryPath = Paths.get(directory, "hg");
      if (verifier.verify(binaryPath) == HgVerifier.HgVerifyStatus.VALID) {
        return Optional.of(binaryPath);
      }
    }
    return Optional.empty();
  }

}
