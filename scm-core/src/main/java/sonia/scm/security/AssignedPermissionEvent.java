/**
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

package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.HandlerEventType;
import sonia.scm.event.Event;

import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * Event which is fired after a {@link StoredAssignedPermission} was added,
 * removed or changed.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@Event
public final class AssignedPermissionEvent implements Serializable
{

  /** serial version uid */
  private static final long serialVersionUID = 706824497813169009L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new AssignedPermissionEvent.
   *
   *
   * @param type type of the event
   * @param permission permission object which has changed
   */
  public AssignedPermissionEvent(HandlerEventType type,
                                 AssignedPermission permission)
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

    final AssignedPermissionEvent other =
      (AssignedPermissionEvent) obj;

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
    return MoreObjects.toStringHelper(this)
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
  public HandlerEventType getEventType()
  {
    return type;
  }

  /**
   * Returns the changed permission object.
   *
   *
   * @return changed permission
   */
  public AssignedPermission getPermission()
  {
    return permission;
  }

  //~--- fields ---------------------------------------------------------------

  /** changed permission */
  private AssignedPermission permission;

  /** type of the event */
  private HandlerEventType type;
}
