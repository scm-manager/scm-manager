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
      if (verifier.isValid(binaryPath)) {
        return Optional.of(binaryPath);
      }
    }
    return Optional.empty();
  }

}
