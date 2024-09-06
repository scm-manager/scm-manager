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

import java.net.URL;

/**
 * RootURL is able to return the root url of the SCM-Manager instance,
 * regardless of the scope (web request, async hook, ssh command, etc.).
 *
 * @since 2.3.1
 */
public interface RootURL {

  /**
   * Returns the root url of the SCM-Manager instance.
   *
   * @return root url
   */
  URL get();

  /**
   * Returns the root url of the SCM-Manager instance as string.
   */
  default String getAsString() {
    return get().toExternalForm();
  }

}
