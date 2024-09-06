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

package sonia.scm.repository.spi;

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.repository.Feature;

import java.io.File;
import java.io.IOException;

public interface ModifyCommand {

  String execute(ModifyCommandRequest request);

  /**
   * Implementations should use the {@link ModifyWorkerHelper} for this.
   */
  interface Worker {
    void delete(String toBeDeleted, boolean recursive) throws IOException;

    void create(String toBeCreated, File file, boolean overwrite) throws IOException;

    void modify(String path, File file) throws IOException;

    /**
     * @since 2.28.0
     */
    void move(String path, String newPath, boolean overwrite) throws IOException;
  }
}
