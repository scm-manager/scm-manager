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

package sonia.scm.net;

import java.util.Collection;

/**
 * Proxy server configuration.
 *
 * @since 2.23.0
 */
public interface ProxyConfiguration {

  boolean isEnabled();

  /**
   * Return the hostname or ip address of the proxy server.
   */
  String getHost();

  /**
   * Returns port of the proxy server.
   */
  int getPort();

  /**
   * Returns a list of hostnames which should not be routed over the proxy server.
   */
  Collection<String> getExcludes();

  /**
   * Returns the username for proxy server authentication.
   */
  String getUsername();

  /**
   * Returns thr password for proxy server authentication.
   */
  String getPassword();

  /**
   * Return {@code true} if the proxy server required authentication.
   */
  boolean isAuthenticationRequired();
}
