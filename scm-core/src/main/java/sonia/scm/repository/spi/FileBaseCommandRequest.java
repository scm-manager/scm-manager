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

package sonia.scm.repository.spi;


import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * @since 1.17
 */
@EqualsAndHashCode
@ToString
public abstract class FileBaseCommandRequest
  implements Resetable, Serializable, Cloneable {

  private static final long serialVersionUID = -3442101119408346165L;

   @Override
  public void reset()
  {
    path = null;
    revision = null;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public String getPath()
  {
    return path;
  }

  public String getRevision()
  {
    return revision;
  }

  @Override
  protected FileBaseCommandRequest clone() throws CloneNotSupportedException
  {
    FileBaseCommandRequest clone = null;

    try
    {
      clone = (FileBaseCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError(
        "FileBaseCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  private String path;

  private String revision;
}
