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



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import sonia.scm.util.HttpUtil;

/**
 * The form builder is able to add form parameters to a request.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class FormContentBuilder
{

  /**
   * Constructs a new {@link FormContentBuilder}.
   *
   *
   * @param request request
   */
  public FormContentBuilder(AdvancedHttpRequestWithBody request)
  {
    this.request = request;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Build the formular content and append it to the request.
   *
   *
   * @return request instance
   */
  public AdvancedHttpRequestWithBody build()
  {
    request.contentType("application/x-www-form-urlencoded");
    request.stringContent(builder.toString());

    return request;
  }

  /**
   * Adds a formular parameter.
   *
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return {@code this}
   */
  public FormContentBuilder fields(String name, Iterable<? extends Object> values)
  {
    for (Object v : values)
    {
      append(name, v);
    }

    return this;
  }

  /**
   * Adds a formular parameter.
   *
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return {@code this}
   */
  public FormContentBuilder field(String name, Object... values)
  {
    for (Object v : values)
    {
      append(name, v);
    }

    return this;
  }

  private void append(String name, Object value)
  {
    if (!Strings.isNullOrEmpty(name))
    {
      if (builder.length() > 0)
      {
        builder.append("&");
      }

      builder.append(HttpUtil.encode(name)).append("=");

      if (value != null)
      {
        builder.append(HttpUtil.encode(value.toString()));
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** content builder */
  private final StringBuilder builder = new StringBuilder();

  /** request */
  private final AdvancedHttpRequestWithBody request;
}
