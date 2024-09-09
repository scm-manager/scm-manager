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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static sonia.scm.repository.Branch.VALID_BRANCH_NAMES;

@NoArgsConstructor
@Getter
@Setter
public class GitConfigDto extends HalRepresentation implements UpdateGitConfigDto {

  private boolean disabled = false;
  private boolean allowDisable;

  private String gcExpression;

  private boolean nonFastForwardDisallowed;

  @NotEmpty
  @Length(min = 1, max = 1000)
  @Pattern(regexp = VALID_BRANCH_NAMES)
  private String defaultBranch;

  @Min(1)
  private int lfsWriteAuthorizationExpirationInMinutes;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
