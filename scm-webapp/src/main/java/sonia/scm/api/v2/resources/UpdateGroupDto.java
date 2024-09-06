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
import jakarta.validation.constraints.Pattern;
import sonia.scm.util.ValidationUtil;

import java.time.Instant;
import java.util.List;

interface UpdateGroupDto {

  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  String getName();

  String getDescription();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  Instant getLastModified();

  String getType();

  List<String> getMembers();

  boolean isExternal();
}
