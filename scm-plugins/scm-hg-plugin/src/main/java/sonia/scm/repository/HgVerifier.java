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

package sonia.scm.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HgVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(HgVerifier.class);
  private final VersionResolver versionResolver;

  public HgVerifier() {
    this.versionResolver = defaultVersionResolver();
  }

  HgVerifier(VersionResolver versionResolver) {
    this.versionResolver = versionResolver;
  }

  public boolean isValid(HgGlobalConfig config) {
    return verify(config) == HgVerifyStatus.VALID;
  }

  public boolean isValid(Path path) {
    return verify(path) == HgVerifyStatus.VALID;
  }

  public HgVerifyStatus verify(HgGlobalConfig config) {
    return verify(config.getHgBinary());
  }

  public HgVerifyStatus verify(String hg) {
    return verify(Paths.get(hg));
  }

  public HgVerifyStatus verify(Path hg) {
    LOG.trace("check if hg binary {} is valid", hg);
    if (!Files.isRegularFile(hg)) {
      LOG.warn("{} is not a regular file", hg);
      return HgVerifyStatus.NOT_REGULAR_FILE;
    }

    if (!Files.isExecutable(hg)) {
      LOG.warn("{} is not executable", hg);
      return HgVerifyStatus.NOT_EXECUTABLE;
    }

    try {
      String version = versionResolver.resolveVersion(hg);
      return isVersionValid(hg, version);
    } catch (IOException ex) {
      LOG.warn("failed to resolve version of {}: ", hg, ex);
      return HgVerifyStatus.COULD_NOT_RESOLVE_VERSION;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("failed to resolve version of {}: ", hg, ex);
      return HgVerifyStatus.COULD_NOT_RESOLVE_VERSION;
    }
  }

  private HgVerifyStatus isVersionValid(Path hg, String version) {
    String[] parts = version.split("\\.");
    if (parts.length < 2) {
      LOG.warn("{} returned invalid version: {}", hg, version);
      return HgVerifyStatus.INVALID_VERSION;
    }
    try {
      int major = Integer.parseInt(parts[0]);
      if (major < 4) {
        LOG.warn("{} is too old, we need at least mercurial 4.x", hg);
        return HgVerifyStatus.VERSION_TOO_OLD;
      }
    } catch (NumberFormatException ex) {
      LOG.warn("{} returned invalid version {}", hg, version);
      return HgVerifyStatus.INVALID_VERSION;
    }
    return HgVerifyStatus.VALID;
  }

  private VersionResolver defaultVersionResolver() {
    return hg -> {
      SimpleCommand command = new SimpleCommand(hg.toString(), "version", "--template", "{ver}");
      SimpleCommandResult result = command.execute();
      if (!result.isSuccessfull()) {
        throw new IOException("failed to get version from hg");
      }
      return result.getOutput().trim();
    };
  }

  @FunctionalInterface
  interface VersionResolver {
    String resolveVersion(Path hg) throws IOException, InterruptedException;
  }

  public enum HgVerifyStatus {
    VALID("hg binary is valid"),
    NOT_REGULAR_FILE("hg binary is not a regular file"),
    NOT_EXECUTABLE("hg binary is not executable"),
    INVALID_VERSION("hg binary returned invalid version"),
    VERSION_TOO_OLD("hg binary version is too old, we need at least 4.x"),
    COULD_NOT_RESOLVE_VERSION("failed to resolve version of hg binary");

    private final String description;

    HgVerifyStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
