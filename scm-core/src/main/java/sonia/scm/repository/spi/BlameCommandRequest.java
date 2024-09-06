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
public final class BlameCommandRequest extends FileBaseCommandRequest
{

  private static final long serialVersionUID = 6421975024231127315L;



  @Override
  public BlameCommandRequest clone()
  {
    BlameCommandRequest clone = null;

    try
    {
      clone = (BlameCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("BlameCommandRequest seems not to be cloneable");
    }

    return clone;
  }
}
