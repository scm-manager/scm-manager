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


package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

import sonia.scm.api.rest.Permission;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.SecuritySystem;

/**
 *
 * @author Sebastian Sdorra
 */
public class GroupPermissionResource extends AbstractPermissionResource
{

  /**
   * Constructs ...
   *
   *
   * @param securitySystem
   * @param name
   */
  public GroupPermissionResource(SecuritySystem securitySystem, String name)
  {
    super(securitySystem, name);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  @Override
  protected AssignedPermission transformPermission(Permission permission)
  {
    return new AssignedPermission(name, true, permission.getValue());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Predicate<AssignedPermission> getPredicate()
  {
    return new GroupPredicate(name);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/05/01
   * @author         Enter your name here...    
   */
  private static class GroupPredicate implements Predicate<AssignedPermission>
  {

    /**
     * Constructs ...
     *
     *
     * @param name
     */
    public GroupPredicate(String name)
    {
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param input
     *
     * @return
     */
    @Override
    public boolean apply(AssignedPermission input)
    {
      return input.isGroupPermission() && input.getName().equals(name);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String name;
  }
}
