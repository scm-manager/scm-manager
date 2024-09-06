/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
