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

package sonia.scm.server;

public class Listener {

  private final String scheme;
  private final int port;
  private final String contextPath;

  public Listener(String scheme, int port, String contextPath) {
    this.scheme = scheme;
    this.port = port;
    this.contextPath = contextPath;
  }

  public String getScheme() {
    return scheme;
  }

  public int getPort() {
    return port;
  }

  public String getContextPath() {
    return contextPath;
  }
}
