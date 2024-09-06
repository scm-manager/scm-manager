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

package sonia.scm.user;

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class ChangePasswordNotAllowedException extends BadRequestException {

  private static final String CODE = "9BR7qpDAe1";
  public static final String WRONG_USER_TYPE = "Users of type %s are not allowed to change password";

  public ChangePasswordNotAllowedException(ContextEntry.ContextBuilder context, String type) {
    super(context.build(), String.format(WRONG_USER_TYPE, type));
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
