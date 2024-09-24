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
