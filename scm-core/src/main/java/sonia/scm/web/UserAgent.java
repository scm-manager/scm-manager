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
