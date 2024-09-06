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
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.util.ValidationUtil;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupDto extends HalRepresentation implements UpdateGroupDto, CreateGroupDto {

  private Instant creationDate;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;
  private String type;
  private List<String> members;
  private boolean external;

  GroupDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
