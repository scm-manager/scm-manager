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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.BrowserResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public interface BrowseCommand
{

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   */
  BrowserResult getBrowserResult(BrowseCommandRequest request) throws IOException;

  default <T> void sort(List<T> entries, Function<T, Boolean> isDirectory, Function<T, String> nameOf) {
    entries.sort((e1, e2) -> {
      if (isDirectory.apply(e1).equals(isDirectory.apply(e2))) {
        return nameOf.apply(e1).compareTo(nameOf.apply(e2));
      } else if (isDirectory.apply(e1)) {
        return -1;
      } else {
        return 1;
      }
    });
  }
}
