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
    
package sonia.scm.repository.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.client.spi.RemoveCommand;
import sonia.scm.util.Util;

import java.io.IOException;

/**
 * @since 1.18
 */
public final class RemoveCommandBuilder {

  private static final Logger logger = LoggerFactory.getLogger(RemoveCommandBuilder.class);

  private final RemoveCommand command;

  RemoveCommandBuilder(RemoveCommand command) {
    this.command = command;
  }

  public RemoveCommandBuilder remove(String... paths) throws IOException {
    for (String p : paths) {
      remove(p);
    }
    return this;
  }

  private void remove(String path) throws IOException {
    if (Util.isNotEmpty(path)) {
      logger.debug("add path {}", path);
      command.remove(path);
    }
  }
}
