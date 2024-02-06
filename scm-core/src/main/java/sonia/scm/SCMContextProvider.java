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

package sonia.scm;

import sonia.scm.version.Version;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static java.lang.String.format;

/**
 * The main class for retrieving the home and the version of the SCM-Manager.
 * This class is a singleton which can be retrieved via injection
 * or with the static {@link SCMContext#getContext()} method.
 *
 */
public interface SCMContextProvider {
  /**
   * Returns the base directory of the SCM-Manager.
   */
  File getBaseDirectory();

  /**
   * Resolves the given path against the base directory.
   *
   * @param path path to resolve
   *
   * @return absolute resolved path
   *
   * @since 2.0.0
   */
  Path resolve(Path path);

  /**
   * Returns the current stage of SCM-Manager.
   *
   * @since 1.12
   */
  Stage getStage();

  /**
   * Returns an exception which is occurred on context startup.
   * The method returns null if the start was successful.
   *
   *
   * @return startup exception of null
   * @since 1.14
   */
  Throwable getStartupError();

  /**
   * Returns the version of the SCM-Manager.
   */
  String getVersion();

  /**
   * Returns the version of the SCM-Manager used in documentation urls (e.g. version 2.17.0 and 2.17.1 will all result
   * in 2.17.x). The default implementation works for versions with three parts (major version, minor version,
   * and patch version, where the patch version will be replaced with an 'x').
   *
   * @since 2.17.0
   */
  default String getDocumentationVersion() {
    Version parsedVersion = Version.parse(getVersion());
    return format("%s.%s.x", parsedVersion.getMajor(), parsedVersion.getMinor());
  }

  /**
   * Returns the instance id of the SCM-Manager used.
   *
   * @since 2.30.0
   */
  default String getInstanceId() {
    return UUID.randomUUID().toString();
  }
}
