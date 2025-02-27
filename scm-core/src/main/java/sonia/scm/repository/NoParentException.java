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

package sonia.scm.repository;

import lombok.Getter;
import sonia.scm.BadRequestException;

import static java.util.Collections.emptyList;

/**
 * Thrown when a changeset has no parent.
 * @since 3.8
 */
@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
@Getter
public class NoParentException extends BadRequestException {

  public NoParentException(String changeset) {
    super(emptyList(), String.format("%s has no parent.", changeset));
    this.revision = changeset;
  }

  private final String revision;

  @Override
  public String getCode() {
    return "a37jI66dup";
  }
}
