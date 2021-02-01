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

  private static final Repository NORMAL_REPOSITORY = new Repository("hog", "git", "hitchhiker", "hog");
  private static final Repository EXPORTING_REPOSITORY = new Repository("exporting_hog", "git", "hitchhiker", "hog");

  static {
    EXPORTING_REPOSITORY.setExporting(true);
  }

  private final DefaultRepositoryExportingCheck check = new DefaultRepositoryExportingCheck();

  @BeforeEach
  void prepareExportingRepository() {
    DefaultRepositoryExportingCheck.setAsExporting(EXPORTING_REPOSITORY.getId());
  }

  @AfterEach
  void removeExportingRepository() {
    DefaultRepositoryExportingCheck.removeFromExporting(EXPORTING_REPOSITORY.getId());
  }

  @Test
  void shouldBeReadOnlyIfBeingExported() {
    boolean readOnly = check.isReadOnly(EXPORTING_REPOSITORY);
    assertThat(readOnly).isTrue();
  }

  @Test
  void shouldNotBeReadOnlyIfNotBeingExported() {
    boolean readOnly = check.isReadOnly(NORMAL_REPOSITORY);
    assertThat(readOnly).isFalse();
  }

  @Test
  void shouldNotBeReadOnlyAfterExportIsFinished() {
    DefaultRepositoryExportingCheck.removeFromExporting(EXPORTING_REPOSITORY.getId());
    boolean readOnly = check.isReadOnly(EXPORTING_REPOSITORY);
    assertThat(readOnly).isFalse();
  }
}
