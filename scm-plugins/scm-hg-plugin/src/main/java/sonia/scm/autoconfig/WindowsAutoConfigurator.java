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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgVerifier;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WindowsAutoConfigurator implements AutoConfigurator {

  private static final Logger LOG = LoggerFactory.getLogger(WindowsAutoConfigurator.class);

  @VisibleForTesting
  static final String REGISTRY_KEY_TORTOISE_HG = "HKEY_CURRENT_USER\\Software\\TortoiseHg";
  @VisibleForTesting
  static final String REGISTRY_KEY_MERCURIAL = "HKEY_CURRENT_USER\\Software\\Mercurial\\InstallDir";

  private static final String[] REGISTRY_KEYS = {REGISTRY_KEY_TORTOISE_HG, REGISTRY_KEY_MERCURIAL};

  @VisibleForTesting
  static final String BINARY_HG_EXE = "hg.exe";
  @VisibleForTesting
  static final String BINARY_HG_BAT = "hg.bat";

  private static final String[] BINARIES = {BINARY_HG_EXE, BINARY_HG_BAT};

  private final HgVerifier verifier;
  private final WindowsRegistry registry;
  private final Map<String, String> env;

  WindowsAutoConfigurator(HgVerifier verifier, WindowsRegistry registry, Map<String, String> env) {
    this.verifier = verifier;
    this.registry = registry;
    this.env = env;
  }

  @Override
  public void configure(HgConfig config) {
    Set<String> fsPaths = new LinkedHashSet<>(pathFromEnv());
    resolveRegistryKeys(fsPaths);

    Optional<String> hg = findInPath(fsPaths);
    if (hg.isPresent()) {
      String hgBinary = hg.get();
      LOG.info("found hg at {}", hgBinary);
      config.setHgBinary(hgBinary);
    } else {
      LOG.warn("could not find valid mercurial installation");
    }
  }

  private void resolveRegistryKeys(Set<String> fsPaths) {
    for (String registryKey : REGISTRY_KEYS) {
      Optional<String> registryValue = registry.get(registryKey);
      if (registryValue.isPresent()) {
        String directory = registryValue.get();
        LOG.trace("resolved registry key {} to directory {}", registryKey, directory);
        fsPaths.add(directory);
      } else {
        LOG.trace("could not find value for registry key {}", registryKey);
      }
    }
  }

  private Collection<String> pathFromEnv() {
    String path = env.getOrDefault("PATH", "");
    return Splitter.on(File.pathSeparator).splitToList(path);
  }

  private Optional<String> findInPath(Set<String> fsPaths) {
    for (String directory : fsPaths) {
      Optional<String> binaryPath = findInDirectory(directory);
      if (binaryPath.isPresent()) {
        return binaryPath;
      }
    }
    return Optional.empty();
  }

  private Optional<String> findInDirectory(String directory) {
    LOG.trace("check directory {} for mercurial installations", directory);
    for (String binary : BINARIES) {
      Path hg = Paths.get(directory, binary);
      if (verifier.isValid(hg)) {
        return Optional.of(hg.toAbsolutePath().toString());
      }
    }
    return Optional.empty();
  }

}
