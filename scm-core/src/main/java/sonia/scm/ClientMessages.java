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
    
package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.i18n.I18nMessages;

//~--- JDK imports ------------------------------------------------------------


/**
 * I18n messages which are send back to client.
 *
 * @author Sebastian Sdorra
 * @since 1.37
 */
public final class ClientMessages
{

  /**
   * Constructs a new instance of ClientMessages. This constructor should not be 
   * used. Use the {@link #get(jakarta.servlet.http.HttpServletRequest)} method
   * instead.
   */
  public ClientMessages() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Returns the localized string for a failed authentication.
   *
   *
   * @return localized string
   */
  public String failedAuthentication()
  {
    return failedAuthentication;
  }

  /**
   * Returns the localized string for "not enough privileges.
   *
   *
   * @return localized string
   */
  public String notEnoughPrivileges()
  {
    return notEnoughPrivileges;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns an instance {@link ClientMessages}.
   *
   * @param request servlet request
   * 
   * @return instance of client messages
   */
  public static ClientMessages get(HttpServletRequest request)
  {
    return I18nMessages.get(ClientMessages.class, request);
  }

  //~--- fields ---------------------------------------------------------------

  /** failed authentication */
  private String failedAuthentication;

  /** not enough privileges */
  private String notEnoughPrivileges;
}
