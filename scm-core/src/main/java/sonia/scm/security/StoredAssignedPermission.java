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
    
package sonia.scm.security;

//~--- JDK imports ------------------------------------------------------------

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Permission object which is stored and assigned to a specific user or group.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assigned-permission")
public class StoredAssignedPermission extends AssignedPermission
{

  /** serial version uid */
  private static final long serialVersionUID = -4593919877023168090L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructor is only visible for JAXB.
   *
   */
  public StoredAssignedPermission() {}

  /**
   * Constructs a new StoredAssignedPermission.
   *
   *
   * @param id id of the permission object
   * @param permission assigned permission object
   */
  public StoredAssignedPermission(String id, AssignedPermission permission)
  {
    super(permission);
    this.id = id;

  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the id of the stored permission object.
   *
   *
   * @return id of permission
   */
  public String getId()
  {
    return id;
  }

  //~--- fields ---------------------------------------------------------------

  /** id */
  private String id;
}
