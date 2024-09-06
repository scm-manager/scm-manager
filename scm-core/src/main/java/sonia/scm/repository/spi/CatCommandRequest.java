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

/**
 *
 * @since 1.17
 */
@EqualsAndHashCode(callSuper = true)
public final class CatCommandRequest extends FileBaseCommandRequest
{

  private static final long serialVersionUID = -6404958421249874551L;


  
  @Override
  public CatCommandRequest clone()
  {
    CatCommandRequest clone = null;

    try
    {
      clone = (CatCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("CatCommandRequest seems not to be cloneable");
    }

    return clone;
  }
}
