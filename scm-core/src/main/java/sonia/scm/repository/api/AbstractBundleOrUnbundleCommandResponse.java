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

package sonia.scm.repository.api;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
/**
 * Abstract class for bundle or unbundle command.
 *
 * @since 1.43
 */
public abstract class AbstractBundleOrUnbundleCommandResponse
{
  /** count of bundled/unbundled changesets */
  private final long changesetCount;

  protected AbstractBundleOrUnbundleCommandResponse(long changesetCount)
  {
    this.changesetCount = changesetCount;
  }

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

    final AbstractBundleOrUnbundleCommandResponse other =
      (AbstractBundleOrUnbundleCommandResponse) obj;

    return Objects.equal(changesetCount, other.changesetCount);
  }

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(changesetCount);
  }

 
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("changesetCount", changesetCount)
                  .toString();
    //J+
  }


  /**
   * Returns the count of bundled/unbundled changesets.
   */
  public long getChangesetCount()
  {
    return changesetCount;
  }

}
