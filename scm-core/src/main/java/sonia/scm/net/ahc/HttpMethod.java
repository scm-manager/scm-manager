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

package sonia.scm.net.ahc;

/**
 * Http methods.
 *
 * @since 1.46
 */
public final class HttpMethod
{

  public static final String DELETE = "DELETE";

  public static final String GET = "GET";

  public static final String HEAD = "HEAD";

  public static final String OPTIONS = "OPTIONS";

  public static final String POST = "POST";

  public static final String PUT = "PUT";


  private HttpMethod() {}
}
