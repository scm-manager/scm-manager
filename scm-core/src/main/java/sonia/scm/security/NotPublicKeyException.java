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

package sonia.scm.security;

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

import java.util.List;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class NotPublicKeyException extends BadRequestException {
  public NotPublicKeyException(List<ContextEntry> context, String message) {
    super(context, message);
  }

  public NotPublicKeyException(List<ContextEntry> context, String message, Exception cause) {
    super(context, message, cause);
  }

  @Override
  public String getCode() {
    return "BxS5wX2v71";
  }
}
