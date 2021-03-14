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
    return isValid(config.getHgBinary());
  }

  public boolean isValid(String hg) {
    return isValid(Paths.get(hg));
  }

  public boolean isValid(Path hg) {
    LOG.trace("check if hg binary {} is valid", hg);
    if (!Files.isRegularFile(hg)) {
      LOG.warn("{} is not a regular file", hg);
      return false;
    }

    if (!Files.isExecutable(hg)) {
      LOG.warn("{} is not executable", hg);
      return false;
    }

    try {
      String version = versionResolver.resolveVersion(hg);
      return isVersionValid(hg, version);
    } catch (IOException ex) {
      LOG.warn("failed to resolve version of {}: ", hg, ex);
      return false;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("failed to resolve version of {}: ", hg, ex);
      return false;
    }
  }

  private boolean isVersionValid(Path hg, String version) {
    String[] parts = version.split("\\.");
    if (parts.length < 2) {
      LOG.warn("{} returned invalid version: {}", hg, version);
      return false;
    }
    try {
      int major = Integer.parseInt(parts[0]);
      if (major < 4) {
        LOG.warn("{} is too old, we need at least mercurial 4.x", hg);
        return false;
      }
    } catch (NumberFormatException ex) {
      LOG.warn("{} returned invalid version {}", hg, version);
      return false;
    }
    return true;
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

}
