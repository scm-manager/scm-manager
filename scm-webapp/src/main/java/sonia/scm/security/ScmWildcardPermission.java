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

  Collection<ScmWildcardPermission> limit(Scope scope) {
    Collection<ScmWildcardPermission> result = new ArrayList<>();
    for (String s : scope) {
      limit(s).ifPresent(result::add);
    }
    return result;
  }

  Optional<ScmWildcardPermission> limit(String scope) {
    return limit(new ScmWildcardPermission(scope));
  }

  Optional<ScmWildcardPermission> limit(ScmWildcardPermission scope) {
    if (this.implies(scope)) {
      return of(scope);
    }
    if (scope.implies(this)) {
      return of(this);
    }

    final List<Set<String>> theseParts = getParts();
    final List<Set<String>> scopeParts = scope.getParts();

    if (!getEntries(theseParts, 0).equals(getEntries(scopeParts, 0))) {
      return empty();
    }

    String type = getEntries(scopeParts, 0).iterator().next();
    Collection<String> verbs = intersect(theseParts, scopeParts, 1);
    Collection<String> ids = intersect(theseParts, scopeParts, 2);

    if (verbs.isEmpty() || ids.isEmpty()) {
      return empty();
    }

    return of(new ScmWildcardPermission(type + ":" + String.join(",", verbs) + ":" + String.join(",", ids)));
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
