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

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class MergeStrategyNotSupportedException extends BadRequestException {

  private static final long serialVersionUID = 256498734456613496L;

  private static final String CODE = "6eRhF9gU41";

  public MergeStrategyNotSupportedException(Repository repository, MergeStrategy strategy) {
    super(entity(repository).build(), createMessage(strategy));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(MergeStrategy strategy) {
    return "merge strategy " + strategy + " is not supported by this repository";
  }
}
