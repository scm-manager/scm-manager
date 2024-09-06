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

package sonia.scm.migration;

public class MigrationInfo {

  private final String id;
  private final String protocol;
  private final String originalRepositoryName;
  private final String namespace;
  private final String name;

  public MigrationInfo(String id, String protocol, String originalRepositoryName, String namespace, String name) {
    this.id = id;
    this.protocol = protocol;
    this.originalRepositoryName = originalRepositoryName;
    this.namespace = namespace;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getOriginalRepositoryName() {
    return originalRepositoryName;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }
}
