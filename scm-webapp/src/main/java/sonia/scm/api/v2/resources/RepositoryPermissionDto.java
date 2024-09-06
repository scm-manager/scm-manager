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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import sonia.scm.util.ValidationUtil;

import java.util.Collection;

@Getter @Setter @ToString @NoArgsConstructor
@EitherRoleOrVerbs
public class RepositoryPermissionDto extends HalRepresentation implements UpdateRepositoryPermissionDto {

  public static final String GROUP_PREFIX = "@";

  @NotNull
  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;

  @NoBlankStrings
  private Collection<String> verbs;

  private String role;

  private boolean groupPermission = false;

  public RepositoryPermissionDto(String permissionName, boolean groupPermission) {
    name = permissionName;
    this.groupPermission = groupPermission;
  }

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
