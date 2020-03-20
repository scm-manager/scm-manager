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
