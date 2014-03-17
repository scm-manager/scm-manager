/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
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
   * used. Use the {@link #get(javax.servlet.http.HttpServletRequest)} method 
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
