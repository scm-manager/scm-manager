/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.autoconfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgGlobalConfig;
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
  static final String REGISTRY_KEY_TORTOISE_HG = "HKEY_LOCAL_MACHINE\\Software\\TortoiseHg";
  @VisibleForTesting
  static final String REGISTRY_KEY_MERCURIAL = "HKEY_LOCAL_MACHINE\\Software\\Mercurial\\InstallDir";

  private static final String[] REGISTRY_KEYS = {REGISTRY_KEY_TORTOISE_HG, REGISTRY_KEY_MERCURIAL};

  @VisibleForTesting
  static final String BINARY_HG_EXE = "hg.exe";
  @VisibleForTesting
  static final String BINARY_HG_BAT = "hg.bat";

  private static final String[] BINARIES = {BINARY_HG_EXE, BINARY_HG_BAT};

  @VisibleForTesting
  static final String ENV_PATH = "Path";

  private final HgVerifier verifier;
  private final WindowsRegistry registry;
  private final Map<String, String> env;

  WindowsAutoConfigurator(HgVerifier verifier, WindowsRegistry registry, Map<String, String> env) {
    this.verifier = verifier;
    this.registry = registry;
    this.env = env;
  }

  @Override
  public void configure(HgGlobalConfig config) {
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
    String path = env.getOrDefault(ENV_PATH, "");
    LOG.trace("try to find hg in PATH {}", path);
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
