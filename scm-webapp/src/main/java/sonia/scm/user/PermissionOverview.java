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

package sonia.scm.user;

import lombok.Getter;
import sonia.scm.repository.Repository;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

/**
 * The permission overview aggregates groups a user is a member of and all namespaces
 * and repositories that have permissions configured for this user or one of its groups.
 * This is the result of {@link PermissionOverviewCollector#create(String)}.
 *
 * @since 2.42.0
 */
public class PermissionOverview {

  private final Collection<GroupEntry> relevantGroups;
  private final Collection<String> relevantNamespaces;
  private final Collection<Repository> relevantRepositories;

  public PermissionOverview(Collection<GroupEntry> relevantGroups, Collection<String> relevantNamespaces, Collection<Repository> relevantRepositories) {
    this.relevantGroups = relevantGroups;
    this.relevantNamespaces = relevantNamespaces;
    this.relevantRepositories = relevantRepositories;
  }

  public Collection<GroupEntry> getRelevantGroups() {
    return unmodifiableCollection(relevantGroups);
  }

  public Collection<String> getRelevantNamespaces() {
    return unmodifiableCollection(relevantNamespaces);
  }

  public Collection<Repository> getRelevantRepositories() {
    return unmodifiableCollection(relevantRepositories);
  }

  @Getter
  public static class GroupEntry {
    private final String name;
    private final boolean permissions;
    private final boolean externalOnly;

    public GroupEntry(String name, boolean permissions, boolean externalOnly) {
      this.name = name;
      this.permissions = permissions;
      this.externalOnly = externalOnly;
    }
  }
}
