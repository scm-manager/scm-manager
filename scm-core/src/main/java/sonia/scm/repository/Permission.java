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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

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
   * @param writeable
   */
  public Permission(String name, boolean writeable)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = false;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param writeable
   * @param groupPermission
   */
  public Permission(String name, boolean writeable, boolean groupPermission)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = groupPermission;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param writeable
   * @param groupPermission
   * @param path
   */
  public Permission(String name, boolean writeable, boolean groupPermission,
                    String path)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = groupPermission;
    this.path = path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append(name);

    if (groupPermission)
    {
      buffer.append(" (Group)");
    }

    buffer.append(" - r");

    if (writeable)
    {
      buffer.append("w");
    }

    if (Util.isNotEmpty(path))
    {
      buffer.append(" ").append(path);
    }

    return buffer.toString();
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
  public String getPath()
  {
    return path;
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

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isRootPermission()
  {
    return Util.isEmpty(path);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isWriteable()
  {
    return writeable;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean groupPermission;

  /** Field description */
  private String name;

  /** Field description */
  private String path = "";

  /** Field description */
  private boolean writeable;
}
