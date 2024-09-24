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

package sonia.scm.it.webapp;


import sonia.scm.util.Util;


public class Credentials
{

  public Credentials() {}

 
  public Credentials(String username, String password)
  {
    this.password = password;
    this.username = username;
  }


  
  public String getPassword()
  {
    return password;
  }

  
  public String getUsername()
  {
    return username;
  }

  
  public boolean isAnonymous()
  {
    return Util.isEmpty(username) && Util.isEmpty(password);
  }

  //~--- fields ---------------------------------------------------------------

  private String password;

  private String username;
}
