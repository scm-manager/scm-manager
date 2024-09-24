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

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class ScmWildcardPermission extends WildcardPermission {
  public ScmWildcardPermission(String permissionString) {
    super(permissionString);
  }

  /**
   * Limits this permission to the given scope. This will result in a collection of new permissions. This
   * collection can be empty (but this will not return <code>null</code>). Three examples:
   * <table>
   *   <tr>
   *     <th>This permission</th>
   *     <th>Scope</th>
   *     <th>Resulting permission(s)</th>
   *   </tr>
   *   <tr>
   *     <td><code>repository:*:42</code></td>
   *     <td><code>repository:read,pull:*</code></td>
   *     <td><code>repository:read,pull:42</code></td>
   *   </tr>
   *   <tr>
   *     <td><code>repository:read:*</code></td>
   *     <td><code>repository:*:42</code>, <code>repository:*:1337</code></td>
   *     <td><code>repository:read:42</code>, <code>repository:read:1337</code></td>
   *   </tr>
   *   <tr>
   *     <td><code>user:*:*</code></td>
   *     <td><code>repository:read,pull:*</code></td>
   *     <td><i>empty</i></td>
   *   </tr>
   * </table>
   * @param scope The scope this permission should be limited to.
   * @return A collection with the resulting permissions (mind that this can be empty, but not <code>null</code>).
   */
  Collection<ScmWildcardPermission> limit(Scope scope) {
    Collection<ScmWildcardPermission> result = new ArrayList<>();
    for (String s : scope) {
      limit(s).ifPresent(result::add);
    }
    return result;
  }

  /**
   * Limits this permission to a scope with a single permission. For examples see {@link #limit(String)}.
   * @param scope The single scope.
   * @return An {@link Optional} with the resulting permission if there was a overlap between this and the scope, or
   *   an empty {@link Optional} otherwise.
   */
  Optional<ScmWildcardPermission> limit(String scope) {
    return limit(new ScmWildcardPermission(scope));
  }

  /**
   * Limits this permission to a scope with a single permission. For examples see {@link #limit(String)}.
   * @param scope The single scope.
   * @return An {@link Optional} with the resulting permission if there was a overlap between this and the scope, or
   *   an empty {@link Optional} otherwise.
   */
  Optional<ScmWildcardPermission> limit(ScmWildcardPermission scope) {
    // if one permission is a subset of the other, we can return the smaller one.
    if (this.implies(scope)) {
      return of(scope);
    }
    if (scope.implies(this)) {
      return of(this);
    }

    // First we check, whether the subjects are the same. We do not use permissions with different subjects, so we
    // either have both this the same subject, or we have no overlap.
    final List<Set<String>> theseParts = getParts();
    final List<Set<String>> scopeParts = scope.getParts();

    if (!getEntries(theseParts, 0).equals(getEntries(scopeParts, 0))) {
      return empty();
    }

    String subject = getEntries(scopeParts, 0).iterator().next();

    // Now we create the intersections of verbs and ids to create the resulting permission
    // (if not one of the resulting sets is empty)
    Collection<String> verbs = intersect(theseParts, scopeParts, 1);
    Collection<String> ids = intersect(theseParts, scopeParts, 2);

    if (verbs.isEmpty() || ids.isEmpty()) {
      return empty();
    }

    return of(new ScmWildcardPermission(subject + ":" + String.join(",", verbs) + ":" + String.join(",", ids)));
  }

  private Collection<String> intersect(List<Set<String>> theseParts, List<Set<String>> scopeParts, int position) {
    final Set<String> theseEntries = getEntries(theseParts, position);
    final Set<String> scopeEntries = getEntries(scopeParts, position);
    if (isWildcard(theseEntries)) {
      return scopeEntries;
    }
    if (isWildcard(scopeEntries)) {
      return theseEntries;
    }
    return CollectionUtils.intersection(theseEntries, scopeEntries);
  }

  /**
   * Handles "shortened" permissions like <code>repository:read</code> that should be <code>repository:read:*</code>.
   */
  private Set<String> getEntries(List<Set<String>> theseParts, int position) {
    if (position >= theseParts.size()) {
      return singleton(WILDCARD_TOKEN);
    }
    return theseParts.get(position);
  }

  private boolean isWildcard(Set<String> entries) {
    return entries.size() == 1 && entries.contains(WILDCARD_TOKEN);
  }
}
