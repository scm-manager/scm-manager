/**
* Copyright (c) 2010, Sebastian Sdorra
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
* 3. Neither the name of SCM-Manager; nor the names of its
*    contributors may be used to endorse or promote products derived from this
*    software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* http://bitbucket.org/sdorra/scm-manager
*
*/



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.nio.charset.Charset;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.45
 */
public final class UserAgent
{

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param browser
   * @param basicAuthenticationCharset
   */
  private UserAgent(String name, boolean browser,
    Charset basicAuthenticationCharset)
  {
    this.name = checkNotNull(name);
    this.browser = browser;
    this.basicAuthenticationCharset = checkNotNull(basicAuthenticationCharset);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public static Builder builder(String name)
  {
    return new Builder(name);
  }

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final UserAgent other = (UserAgent) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(browser, other.browser)
      && Objects.equal(basicAuthenticationCharset, basicAuthenticationCharset);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, browser, basicAuthenticationCharset);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("name", name)
                  .add("browser", browser)
                  .add("basicAuthenticationCharset", basicAuthenticationCharset)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Charset getBasicAuthenticationCharset()
  {
    return basicAuthenticationCharset;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isBrowser()
  {
    return browser;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/10/15
   * @author         Enter your name here...    
   */
  public static class Builder
  {

    /**
     * Constructs ...
     *
     *
     * @param name
     */
    public Builder(String name)
    {
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param basicAuthenticationCharset
     *
     * @return
     */
    public Builder basicAuthenticationCharset(
      Charset basicAuthenticationCharset)
    {
      this.basicAuthenticationCharset =
        checkNotNull(basicAuthenticationCharset);

      return this;
    }

    /**
     * Method description
     *
     *
     * @param browser
     *
     * @return
     */
    public Builder browser(boolean browser)
    {
      this.browser = browser;

      return this;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public UserAgent build()
    {
      return new UserAgent(name, browser, basicAuthenticationCharset);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final String name;

    /** Field description */
    private boolean browser = true;

    /** Field description */
    private Charset basicAuthenticationCharset = Charsets.ISO_8859_1;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Charset basicAuthenticationCharset;

  /** Field description */
  private final boolean browser;

  /** Field description */
  private final String name;
}
