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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import sonia.scm.HandlerEvent;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 * Event which is fired after a {@link StoredAssignedPermission} was added,
 * removed or changed.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public final class StoredAssignedPermissionEvent implements Serializable
{

  /** serial version uid */
  private static final long serialVersionUID = 706824497813169009L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new StoredAssignedPermissionEvent.
   *
   *
   * @param type type of the event
   * @param permission permission object which has changed
   */
  public StoredAssignedPermissionEvent(HandlerEvent type,
    StoredAssignedPermission permission)
  {
    this.type = type;
    this.permission = permission;
  }

  //~--- methods --------------------------------------------------------------

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

    final StoredAssignedPermissionEvent other =
      (StoredAssignedPermissionEvent) obj;

    return Objects.equal(type, other.type)
      && Objects.equal(permission, other.permission);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(type, permission);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("type", type)
                  .add("permission", permission)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the type of the event.
   *
   *
   * @return type of event
   */
  public HandlerEvent getEventType()
  {
    return type;
  }

  /**
   * Returns the changed permission object.
   *
   *
   * @return changed permission
   */
  public StoredAssignedPermission getPermission()
  {
    return permission;
  }

  //~--- fields ---------------------------------------------------------------

  /** changed permission */
  private StoredAssignedPermission permission;

  /** type of the event */
  private HandlerEvent type;
}
