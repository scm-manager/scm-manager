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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Abstract base class for {@link PushResponse} and {@link PullResponse}.
 *
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPushOrPullResponse
{
  /** count of pushed/pulled changesets */
  @XmlElement(name = "changeset-count")
  private long changesetCount;

  public AbstractPushOrPullResponse() {}

  public AbstractPushOrPullResponse(long changesetCount)
  {
    this.changesetCount = changesetCount;
  }

  /**
   * Returns the count of pushed/pulled changesets.
   */
  public long getChangesetCount()
  {
    return changesetCount;
  }

}
