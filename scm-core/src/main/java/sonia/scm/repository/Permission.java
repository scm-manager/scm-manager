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



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = -2915175031430884040L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Permission() {}

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public Permission(String name)
  {
    this();
    this.name = name;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param type
   */
  public Permission(String name, PermissionType type)
  {
    this(name);
    this.type = type;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param type
   * @param groupPermission
   */
  public Permission(String name, PermissionType type, boolean groupPermission)
  {
    this(name, type);
    this.groupPermission = groupPermission;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PermissionType getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupPermission
   */
  public void setGroupPermission(boolean groupPermission)
  {
    this.groupPermission = groupPermission;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param type
   */
  public void setType(PermissionType type)
  {
    this.type = type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean groupPermission = false;

  /** Field description */
  private String name;

  /** Field description */
  private PermissionType type = PermissionType.READ;
}
