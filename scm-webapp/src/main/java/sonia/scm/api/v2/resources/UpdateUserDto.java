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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import sonia.scm.util.ValidationUtil;

import java.time.Instant;

interface UpdateUserDto {

  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  String getName();

  @NotEmpty
  String getDisplayName();

  @Email
  String getMail();

  boolean isExternal();

  String getPassword();

  boolean isActive();

  String getType();

  Instant getLastModified();
}
