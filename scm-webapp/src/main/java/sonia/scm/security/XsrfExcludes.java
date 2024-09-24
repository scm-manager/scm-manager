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

package sonia.scm.security;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import jakarta.inject.Singleton;

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
