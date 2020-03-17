/**
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
