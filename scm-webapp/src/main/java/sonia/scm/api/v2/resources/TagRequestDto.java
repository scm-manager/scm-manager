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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static sonia.scm.repository.Branch.VALID_BRANCH_NAMES;
import static sonia.scm.repository.Tag.VALID_REV;

@Getter
@Setter
public class TagRequestDto {
  @Pattern(regexp = VALID_REV)
  @NotEmpty
  @Length(min = 1, max = 100)
  private String revision;

  @Pattern(regexp = VALID_BRANCH_NAMES)
  @NotEmpty
  @Length(min = 1, max = 1000)
  private String name;
}
