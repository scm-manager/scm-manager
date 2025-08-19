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

package sonia.scm.repository.api;

import sonia.scm.BadRequestException;
import sonia.scm.repository.Repository;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class FastForwardNotPossible extends BadRequestException {

  private static final String CODE = "Re6hF5g14U";

  public FastForwardNotPossible(Repository repository, String source, String target) {
    super(entity(repository).build(), createMessage(source, target));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(String source, String target) {
    return String.format("Fast forward not possible with source revision %s and target revision %s", source, target);
  }
}
