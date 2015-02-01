/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.filter;

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
  public static final String PATTERN_RESOURCE_REGEX =
    "^/(?:resources|api|plugins|index)[\\./].*(?:html|\\.css|\\.js|\\.xml|\\.json|\\.txt)";

  /** Field description */
  public static final String PATTERN_RESTAPI = "/api/rest/*";

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
