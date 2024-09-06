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

import sonia.scm.repository.api.DiffCommandBuilder;

import java.io.IOException;

/**
 *
 * @since 1.17
 */
public interface DiffCommand
{

  /**
   * Implementations of this method have to ensure, that all resources are released when the stream ends.
   * This implementation is used for streaming results, where there are no more requests with the same
   * context will be expected, whatsoever.
   *
   * <b>If</b> this closes resources that will prevent further commands from execution, you have to override
   * {@link #getDiffResultInternal(DiffCommandRequest)} that will return the same as this functions, but
   * must not release these resources so that subsequent commands will still function.
   */
  DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) throws IOException;

  /**
   * Override this if {@link #getDiffResult(DiffCommandRequest)} releases resources that will prevent other
   * commands from execution, so that these resources are not released with this function.
   *
   * Defaults to a simple call to {@link #getDiffResult(DiffCommandRequest)}.
   *
   * @since 2.36.0
   */
  default DiffCommandBuilder.OutputStreamConsumer getDiffResultInternal(DiffCommandRequest request) throws IOException {
    return getDiffResult(request);
  }
}
