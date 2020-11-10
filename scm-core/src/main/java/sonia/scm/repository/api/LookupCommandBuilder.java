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

package sonia.scm.repository.api;

import sonia.scm.repository.spi.LookupCommand;
import sonia.scm.repository.spi.LookupCommandRequest;

import java.util.Optional;

/**
 * The lookup command executes a lookup for additional repository information.
 *
 * @since 2.10.0
 */
public class LookupCommandBuilder {

  private final LookupCommand lookupCommand;
  private final LookupCommandRequest request = new LookupCommandRequest();

  public LookupCommandBuilder(LookupCommand lookupCommand) {
    this.lookupCommand = lookupCommand;
  }

  public <T> Optional<T> lookup(Class<T> type, String... args) {
    request.setType(type);
    request.setArgs(args);
    return lookupCommand.lookup(request);
  }
}
