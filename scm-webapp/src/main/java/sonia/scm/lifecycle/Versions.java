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

package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class Versions {

  private static final Logger LOG = LoggerFactory.getLogger(Versions.class);

  static final Version MIN_VERSION = Version.parse("1.60");

  private final SCMContextProvider contextProvider;

  @VisibleForTesting
  Versions(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @VisibleForTesting
  boolean isPreviousVersionTooOld() {
    return readVersion().map(v -> v.isOlder(MIN_VERSION)).orElse(false);
  }

  @VisibleForTesting
  void writeNewVersion() {
    Path config = contextProvider.resolve(Paths.get("config"));
    IOUtil.mkdirs(config.toFile());

    String version = contextProvider.getVersion();
    LOG.debug("write new version {} to file", version);
    Path versionFile = config.resolve("version.txt");
    try {
      Files.write(versionFile, version.getBytes());
    } catch (IOException e) {
      throw new IllegalStateException("failed to write version file", e);
    }
  }

  private Optional<Version> readVersion() {
    Path versionFile = contextProvider.resolve(Paths.get("config", "version.txt"));
    if (versionFile.toFile().exists()) {
      return Optional.of(readVersionFromFile(versionFile));
    }
    return Optional.empty();
  }

  private Version readVersionFromFile(Path versionFile) {
    try {
      String versionString = new String(Files.readAllBytes(versionFile), StandardCharsets.UTF_8).trim();
      LOG.debug("read previous version {} from file", versionString);
      return Version.parse(versionString);
    } catch (IOException e) {
      throw new IllegalStateException("failed to read version file", e);
    }
  }

  static boolean isTooOld() {
    return new Versions(SCMContext.getContext()).isPreviousVersionTooOld();
  }

  static void writeNew() {
    new Versions(SCMContext.getContext()).writeNewVersion();
  }

}
