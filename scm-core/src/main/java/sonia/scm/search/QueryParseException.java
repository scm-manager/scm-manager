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

package sonia.scm.search;

import com.google.common.annotations.Beta;
import sonia.scm.BadRequestException;

import static sonia.scm.ContextEntry.ContextBuilder.only;

/**
 * Is thrown if a query with invalid syntax was executed.
 *
 * @since 2.21.0
 */
@Beta
@SuppressWarnings("java:S110") // large inheritance is ok for exceptions
public class QueryParseException extends BadRequestException {

  private static final String CODE = "5VScek8Xp1";

  public QueryParseException(String query, String message, Exception cause) {
    super(only("query", query), message, cause);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
