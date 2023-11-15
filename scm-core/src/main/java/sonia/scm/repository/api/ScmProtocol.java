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

/**
 * An ScmProtocol represents a concrete protocol provided by the SCM-Manager instance
 * to interact with a repository depending on its type. There may be multiple protocols
 * available for a repository type (eg. http and ssh).
 */
public interface ScmProtocol {

  /**
   * The type of the concrete protocol, eg. "http" or "ssh".
   */
  String getType();

  /**
   * The URL to access the repository providing this protocol.
   */
  String getUrl();

  /**
   * Whether the protocol can be used as an anonymous user.
   */
  default boolean isAnonymousEnabled() {
    return true;
  }

  /**
   * Priority for frontend evaluation order
   *
   * @return priority number
   * @since 2.48.0
   */
  default int getPriority() {
    return 100;
  }
}
