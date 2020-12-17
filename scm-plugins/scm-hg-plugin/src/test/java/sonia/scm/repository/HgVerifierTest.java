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

import com.google.common.base.Splitter;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.util.SystemUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class HgVerifierTest {

  @Nested
  class WithDefaultVersionResolver {

    private final HgVerifier verifier = new HgVerifier();

    @Test
    void shouldReturnFalseIfFileDoesNotExists(@TempDir Path directory) {
      Path hg = directory.resolve("hg");

      boolean isValid = verify(hg);

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseIfFileIsADirectory(@TempDir Path directory) throws IOException {
      Path hg = directory.resolve("hg");
      Files.createDirectories(hg);

      boolean isValid = verify(hg);

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseIfFileIsNotExecutable(@TempDir Path directory) throws IOException {
      Path hg = directory.resolve("hg");
      Files.createFile(hg);

      boolean isValid = verify(hg);

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueForIfMercurialIsAvailable() {
      Path hg = findHg();

      // skip test if we could not find mercurial
      Assumptions.assumeTrue(hg != null, "skip test, because we could not find mercurial on path");

      boolean isValid = verify(hg);

      assertThat(isValid).isTrue();
    }

    private Path findHg() {
      String pathEnv = System.getenv("PATH");
      if (Strings.isNullOrEmpty(pathEnv)) {
        return null;
      }

      for (String path : Splitter.on(File.pathSeparator).splitToList(pathEnv)) {
        Path hg = Paths.get(path, SystemUtil.isWindows() ? "hg.exe" : "hg");
        if (Files.exists(hg)) {
          return hg;
        }
      }
      return null;
    }

    private boolean verify(Path hg) {
      HgConfig config = new HgConfig();
      config.setHgBinary(hg.toString());
      return verifier.isValid(config);
    }

  }

  @ParameterizedTest
  @ValueSource(strings = { "3-2-1", "x.y.z", "3.2.0" })
  void shouldReturnFalseForInvalidVersions(String version, @TempDir Path directory) throws IOException {
    HgVerifier verifier = new HgVerifier(hg -> version);

    Path hg = createHg(directory);

    boolean isValid = verifier.isValid(hg);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldReturnFalseOnIOException(@TempDir Path directory) throws IOException {
    HgVerifier verifier = new HgVerifier(hg -> {
      throw new IOException("failed");
    });

    Path hg = createHg(directory);

    boolean isValid = verifier.isValid(hg);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldReturnTrue(@TempDir Path directory) throws IOException {
    HgVerifier verifier = new HgVerifier(hg -> "4.2.0");

    Path hg = createHg(directory);

    boolean isValid = verifier.isValid(hg);

    assertThat(isValid).isTrue();
  }

  @Nonnull
  private Path createHg(Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Files.createFile(hg);

    // skip test if we could not set executable flag
    Assumptions.assumeTrue(hg.toFile().setExecutable(true));
    return hg;
  }

}
