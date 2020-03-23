/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.filter;

import static sonia.scm.api.v2.resources.ScmPathInfo.REST_API_PATH;

/**
 * Useful constants for filter implementations.
 *
 * @since 2.0.0
 * @author Sebastian Sdorra
 */
public final class Filters
{

  /** Field description */
  public static final String PATTERN_ALL = "/*";

  /** Field description */
  public static final String PATTERN_DEBUG = "/debug.html";

  /** Field description */
  public static final String PATTERN_RESTAPI = REST_API_PATH + "/*";

  /** authentication priority */
  public static final int PRIORITY_AUTHENTICATION = 5000;

  /** authorization priority */
  public static final int PRIORITY_AUTHORIZATION = 6000;

  /** base url priority */
  public static final int PRIORITY_BASEURL = 1000;

  /** post authentication priority */
  public static final int PRIORITY_POST_AUTHENTICATION = 5500;

  /** pre authorization priority */
  public static final int PRIORITY_POST_AUTHORIZATION = 6500;

  /** post base url priority */
  public static final int PRIORITY_POST_BASEURL = 1500;

  /** pre authentication priority */
  public static final int PRIORITY_PRE_AUTHENTICATION = 4500;

  /** pre authorization priority */
  public static final int PRIORITY_PRE_AUTHORIZATION = 5500;

  /** pre base url priority */
  public static final int PRIORITY_PRE_BASEURL = 500;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private Filters() {}
}
