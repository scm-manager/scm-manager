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
