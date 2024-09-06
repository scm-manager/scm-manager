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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import sonia.scm.repository.Branch;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("java:S2160") // we do not need this for dto
public class BranchDto extends HalRepresentation {

  @NotEmpty
  @Length(min = 1, max = 1000)
  @Pattern(regexp = Branch.VALID_BRANCH_NAMES)
  private String name;
  private String revision;
  private boolean defaultBranch;
  @JsonInclude(NON_NULL)
  private Instant lastCommitDate;
  private PersonDto lastCommitter;
  private boolean stale;

  BranchDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
