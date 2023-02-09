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
