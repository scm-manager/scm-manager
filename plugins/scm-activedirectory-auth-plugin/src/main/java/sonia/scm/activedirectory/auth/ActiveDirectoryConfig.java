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



package sonia.scm.activedirectory.auth;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author David M. Carr
 */
@XmlRootElement(name = "activedirectory-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiveDirectoryConfig
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAdminGroups()
  {
    return adminGroups;
  }
  
  /**
   * Method description
   *
   *
   * @return
   */
  public String getAdminUsers()
  {
    return adminUsers;
  }
  
  Set<String> getAdminGroupSet() {
    return split(adminGroups);
  }
  
  Set<String> getAdminUserSet() {
    return split(adminUsers);
  }
  
  private Set<String> split(String rawString) {
    Set<String> tokens = new HashSet<String>();
    for(String token : rawString.split(",")) {
      if(token.trim().length() > 0) {
        tokens.add(token);
      }
    }
    return tokens;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param passwordSuffix
   */
  public void setAdminGroups(String adminGroups)
  {
    this.adminGroups = adminGroups;
  }
  
  /**
   * Method description
   *
   *
   * @param passwordSuffix
   */
  public void setAdminUsers(String adminUsers)
  {
    this.adminUsers = adminUsers;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "admin-groups")
  private String adminGroups = "";
  
  /** Field description */
  @XmlElement(name = "admin-users")
  private String adminUsers = "";
  
}
