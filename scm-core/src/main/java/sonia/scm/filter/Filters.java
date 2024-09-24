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

package sonia.scm.filter;

import static sonia.scm.api.v2.resources.ScmPathInfo.REST_API_PATH;

/**
 * Useful constants for filter implementations.
 *
 * @since 2.0.0
 */
public final class Filters
{

  public static final String PATTERN_ALL = "/*";

  public static final String PATTERN_DEBUG = "/debug.html";

  public static final String PATTERN_RESTAPI = REST_API_PATH + "/*";

  public static final int PRIORITY_AUTHENTICATION = 5000;

  public static final int PRIORITY_AUTHORIZATION = 6000;

  public static final int PRIORITY_BASEURL = 1000;

  public static final int PRIORITY_POST_AUTHENTICATION = 5500;

  public static final int PRIORITY_POST_AUTHORIZATION = 6500;

  public static final int PRIORITY_POST_BASEURL = 1500;

  public static final int PRIORITY_PRE_AUTHENTICATION = 4500;

  public static final int PRIORITY_PRE_AUTHORIZATION = 5500;

  public static final int PRIORITY_PRE_BASEURL = 500;


  private Filters() {}
}
