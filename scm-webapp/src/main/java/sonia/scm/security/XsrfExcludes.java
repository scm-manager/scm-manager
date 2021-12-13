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

package sonia.scm.security;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * XsrfExcludes can be used to define request uris which are excluded from xsrf validation.
 * @since 2.28.0
 */
@Singleton
public class XsrfExcludes {

  private final Set<String> excludes = new HashSet<>();

  /**
   * Exclude the given request uri from xsrf validation.
   * @param requestUri request uri
   */
  public void add(String requestUri) {
    excludes.add(requestUri);
  }

  /**
   * Include prior excluded request uri to xsrf validation.
   * @param requestUri request uri
   * @return {@code true} is uri was excluded
   */
  @CanIgnoreReturnValue
  public boolean remove(String requestUri) {
    return excludes.remove(requestUri);
  }

  /**
   * Returns {@code true} if the request uri is excluded from xsrf validation.
   * @param requestUri request uri
   * @return {@code true} if uri is excluded
   */
  public boolean contains(String requestUri) {
    return excludes.contains(requestUri);
  }
}
