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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 * The SecuritySystem manages global permissions.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public interface SecuritySystem
{

  /**
   * Store a new permission.
   *
   *
   * @param permission permission to be stored
   *
   * @return stored permission
   */
  public StoredAssignedPermission addPermission(AssignedPermission permission);

  /**
   * Delete stored permission.
   *
   *
   * @param permission permission to be deleted
   */
  public void deletePermission(StoredAssignedPermission permission);

  /**
   * Delete stored permission.
   *
   *
   * @param id id  of the permission
   */
  public void deletePermission(String id);

  /**
   * Modify stored permission.
   *
   *
   * @param permission stored permisison
   */
  public void modifyPermission(StoredAssignedPermission permission);

  //~--- get methods ----------------------------------------------------------

  /**
   * Return all stored permissions.
   *
   *
   * @return stored permission
   */
  public List<StoredAssignedPermission> getAllPermissions();

  /**
   * Return all available permissions.
   *
   *
   * @return available permissions
   */
  public List<PermissionDescriptor> getAvailablePermissions();

  /**
   * Return the stored permission which is stored with the given id.
   *
   *
   * @param id id of the stored permission
   *
   * @return stored permission
   */
  public StoredAssignedPermission getPermission(String id);

  /**
   * Returns all stored permissions which are matched by the given
   * {@link Predicate}.
   *
   *
   * @param predicate predicate to filter
   *
   * @return filtered permissions
   */
  public List<StoredAssignedPermission> getPermissions(
    Predicate<AssignedPermission> predicate);
}
