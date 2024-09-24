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

package sonia.scm;


import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.i18n.I18nMessages;


/**
 * I18n messages which are send back to client.
 *
 * @since 1.37
 */
public final class ClientMessages
{
  private String failedAuthentication;
  private String notEnoughPrivileges;

  /**
   * Constructs a new instance of ClientMessages. This constructor should not be 
   * used. Use the {@link #get(jakarta.servlet.http.HttpServletRequest)} method
   * instead.
   */
  public ClientMessages() {}


  /**
   * Returns the localized string for a failed authentication.
   */
  public String failedAuthentication()
  {
    return failedAuthentication;
  }

  /**
   * Returns the localized string for not enough privileges.
   */
  public String notEnoughPrivileges()
  {
    return notEnoughPrivileges;
  }


  /**
   * Returns an instance {@link ClientMessages}.
   *
   * @param request servlet request
   */
  public static ClientMessages get(HttpServletRequest request)
  {
    return I18nMessages.get(ClientMessages.class, request);
  }
}
