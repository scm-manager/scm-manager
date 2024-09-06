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

package sonia.scm.plugin;

import sonia.scm.ExceptionWithContext;

import java.util.Collections;

/**
 * Exception is thrown if the exchange of a refresh token to an access token fails.
 *
 * @since 2.28.0
 */
public class FetchAccessTokenFailedException extends ExceptionWithContext {

  public FetchAccessTokenFailedException(String message) {
    super(Collections.emptyList(), message);
  }

  public FetchAccessTokenFailedException(String message, Exception cause) {
    super(Collections.emptyList(), message, cause);
  }

  @Override
  public String getCode() {
    return "AHSqALeEv1";
  }
}
