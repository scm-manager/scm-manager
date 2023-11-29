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

package sonia.scm.repository.cli;

import jakarta.inject.Inject;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class RepositoryPermissionBeanMapper {

  private final RepositoryRoleManager roleManager;
  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @Inject
  RepositoryPermissionBeanMapper(RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver) {
    this.roleManager = roleManager;
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  Collection<RepositoryPermissionBean> createPermissionBeans(Collection<RepositoryPermission> permissions, boolean keys) {
    return permissions
      .stream()
      .map(permission -> createPermissionBean(permission, keys))
      .collect(toList());
  }

  private RepositoryPermissionBean createPermissionBean(RepositoryPermission permission, boolean keys) {
    Collection<String> effectiveVerbs;
    if (permission.getRole() == null) {
      effectiveVerbs = permission.getVerbs();
    } else {
      effectiveVerbs = roleManager.get(permission.getRole()).getVerbs();
    }
    return new RepositoryPermissionBean(
      permission.isGroupPermission(),
      permission.getName(),
      permission.getRole() == null? "CUSTOM": permission.getRole(),
      keys? effectiveVerbs: getDescriptions(effectiveVerbs)
    );
  }

  private Collection<String> getDescriptions(Collection<String> effectiveVerbs) {
    return effectiveVerbs.stream().map(this::getDescription).collect(Collectors.toList());
  }

  private String getDescription(String verb) {
    return permissionDescriptionResolver.getDescription(verb).orElse(verb);
  }
}
