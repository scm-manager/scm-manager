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

package sonia.scm.store;

import com.google.common.annotations.Beta;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The {@link Exporter} is used to export a single store entry to an {@link OutputStream}.
 * <p><b>This interface is not yet finalized and might change in the upcoming versions.</b></p>
 *
 * @since 2.13.0
 */
@Beta
public interface Exporter {
  /**
   * Returns the {@link OutputStream} that should be used to export a single store entry with the given name.
   *
   * @param name The name of the exported store entry.
   * @param size The size of the exported store entry (the size of the bytes that will be written to the output stream).
   * @return The output stream the raw data of the store entry must be written to.
   */
  OutputStream put(String name, long size) throws IOException;
}
