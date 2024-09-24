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
