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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultRepositoryExportingCheckTest {

  private static final Repository EXPORTING_REPOSITORY = new Repository("exporting_hog", "git", "hitchhiker", "hog");

  private final DefaultRepositoryExportingCheck check = new DefaultRepositoryExportingCheck();

  @Test
  void shouldBeReadOnlyIfBeingExported() {
    check.withExportingLock(EXPORTING_REPOSITORY, () -> {
      boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
      assertThat(readOnly).isTrue();
      return null;
    });
  }

  @Test
  void shouldBeReadOnlyIfBeingExportedMultipleTimes() {
    check.withExportingLock(EXPORTING_REPOSITORY, () -> {
      check.withExportingLock(EXPORTING_REPOSITORY, () -> {
        boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
        assertThat(readOnly).isTrue();
        return null;
      });
      boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
      assertThat(readOnly).isTrue();
      return null;
    });
  }

  @Test
  void shouldNotBeReadOnlyIfNotBeingExported() {
    boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
    assertThat(readOnly).isFalse();
  }
}
