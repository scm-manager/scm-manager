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
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 * The software agent that is acting on behalf of a user. The user agent 
 * represents a browser or one of the repository client (svn, git or hg).
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.45
 */
public final class UserAgent
{

  /**
   * Constructs a new user agent
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
   * Returns the {@link Builder} for the UserAgent.
   *
   *
   * @param name name of the UserAgent
   *
   * @return builder for UserAgent
   */
  public static Builder builder(String name)
  {
    return new Builder(name);
  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, browser, basicAuthenticationCharset);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("name", name)
                      .add("browser", browser)
                      .add("basicAuthenticationCharset", basicAuthenticationCharset)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the {@link Charset}, which is used to decode the basic 
   * authentication header.
   *
   * @return {@link Charset} for basic authentication
   */
  public Charset getBasicAuthenticationCharset()
  {
    return basicAuthenticationCharset;
  }

  /**
   * Returns the name of UserAgent.
   *
   *
   * @return name of UserAgent
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns {@code true} if UserAgent is a browser.
   *
   *
   * @return {@code true} if UserAgent is a browser 
   */
  public boolean isBrowser()
  {
    return browser;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Builder class for {@link UserAgent}.
   */
  public static class Builder
  {

    /**
     * Constructs a new UserAgent builder.
     *
     *
     * @param name name of the UserAgent
     */
    public Builder(String name)
    {
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Sets {@link Charset} which is used to decode the basic authentication.
     *
     *
     * @param basicAuthenticationCharset charset for basic authentication
     *
     * @return {@code this}
     */
    public Builder basicAuthenticationCharset(
      Charset basicAuthenticationCharset)
    {
      this.basicAuthenticationCharset =
        checkNotNull(basicAuthenticationCharset);

      return this;
    }

    /**
     * Set to {@code true} if the {@link UserAgent} is a browser.
     *
     *
     * @param browser {@code true} for a browser
     *
     * @return {@code this}
     */
    public Builder browser(boolean browser)
    {
      this.browser = browser;

      return this;
    }

    /**
     * Builds the {@link UserAgent}.
     *
     *
     * @return new {@link UserAgent}
     */
    public UserAgent build()
    {
      return new UserAgent(name, browser, basicAuthenticationCharset);
    }

    //~--- fields -------------------------------------------------------------

    /** name of UserAgent */
    private final String name;

    /** indicator for browsers */
    private boolean browser = true;

    /** basic authentication charset */
    private Charset basicAuthenticationCharset = Charsets.ISO_8859_1;
  }


  //~--- fields ---------------------------------------------------------------

  /** basic authentication charset */
  private final Charset basicAuthenticationCharset;

  /** indicator for browsers */
  private final boolean browser;

  /** name of UserAgent */
  private final String name;
}
