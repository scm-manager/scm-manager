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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Import result of the {@link AdvancedImportHandler}.
 *
 * @since 1.43
 * @deprecated
 */
@EqualsAndHashCode
@ToString
@Getter
@Deprecated
public final class ImportResult {

  /**
   * failed directories
   */
  private List<String> failedDirectories;

  /**
   * successfully imported directories
   */
  private List<String> importedDirectories;

  public ImportResult(List<String> importedDirectories,
                      List<String> failedDirectories) {
    this.importedDirectories = checkNotNull(importedDirectories,
      "list of imported directories is required");
    this.failedDirectories = checkNotNull(failedDirectories,
      "list of failed directories is required");
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for {@link ImportResult}.
   */
  public static class Builder {

    /**
     * successfully imported directories
     */
    private final List<String> importedDirectories = Lists.newArrayList();

    /**
     * failed directories
     */
    private final List<String> failedDirectories = Lists.newArrayList();

    /**
     * Adds a failed directory to the import result.
     *
     * @param name name of the directory
     * @return {@code this}
     */
    public Builder addFailedDirectory(String name) {
      this.failedDirectories.add(name);

      return this;
    }

    /**
     * Adds a successfully imported directory to the import result.
     *
     * @param name name of the directory
     * @return {@code this}
     */
    public Builder addImportedDirectory(String name) {
      this.importedDirectories.add(name);

      return this;
    }

    /**
     * Builds the final import result.
     *
     * @return final import result
     */
    public ImportResult build() {
      return new ImportResult(ImmutableList.copyOf(importedDirectories),
        ImmutableList.copyOf(failedDirectories));
    }
  }
}
