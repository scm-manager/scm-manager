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

package sonia.scm.security.gpg;

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public final class DeletingReadonlyKeyNotAllowedException extends BadRequestException {

  public DeletingReadonlyKeyNotAllowedException(String keyId) {
    super(ContextEntry.ContextBuilder.entity(RawGpgKey.class, keyId).build(), "deleting readonly gpg keys is not allowed");
  }

  private static final String CODE = "3US6mweXy1";

  @Override
  public String getCode() {
    return CODE;
  }
}
