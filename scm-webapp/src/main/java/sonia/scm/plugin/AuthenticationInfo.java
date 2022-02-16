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

package sonia.scm.plugin;

import java.time.Instant;

/**
 * Information about the plugin center authentication.
 * @since 2.28.0
 */
public interface AuthenticationInfo {

  /**
   * Returns the username of the SCM-Manager user which has authenticated the plugin center.
   * @return SCM-Manager username
   */
  String getPrincipal();

  /**
   * Returns the subject of the plugin center user.
   * @return plugin center subject
   */
  String getPluginCenterSubject();

  /**
   * Returns the date on which the authentication was performed.
   * @return authentication date
   */
  Instant getDate();

  /**
   * Returns {@code true} if the last authentication has failed.
   * @return {@code true} if the last authentication has failed.
   * @since 2.31.0
   */
  default boolean isFailed() {
    return false;
  }

}
