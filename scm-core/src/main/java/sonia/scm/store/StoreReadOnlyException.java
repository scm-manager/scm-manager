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

package sonia.scm.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class StoreReadOnlyException extends ExceptionWithContext {

  private static final Logger LOG = LoggerFactory.getLogger(StoreReadOnlyException.class);

  public static final String CODE = "3FSIYtBJw1";

  public StoreReadOnlyException(String location) {
    super(noContext(), String.format("Store is read only, could not write location %s", location));
    LOG.error(getMessage());
  }

  public StoreReadOnlyException(Object object) {
    super(noContext(), String.format("Store is read only, could not write object of type %s: %s", object.getClass(), object));
    LOG.error(getMessage());
  }

  public StoreReadOnlyException() {
    super(noContext(), "Store is read only, could not delete store");
    LOG.error(getMessage());
  }

    @Override
    public String getCode () {
      return CODE;
    }
  }
