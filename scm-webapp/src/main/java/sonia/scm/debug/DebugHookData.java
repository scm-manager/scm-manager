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

package sonia.scm.debug;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collection;
import java.util.Date;

/**
 * Received data from repository hook event.
 *
 */
@XmlRootElement(name = "hook")
@XmlAccessorType(XmlAccessType.FIELD)
public class DebugHookData
{
  private Date date;
  private Collection<String> changesets;

  /**
   * This constructor should only be used by JAXB.
   */
  public DebugHookData()
  {
  }

  /**
   * Constructs a new instance.
   * 
   * @param changesets collection of changeset ids
   */
  public DebugHookData(Collection<String> changesets)
  {
    this.date = new Date();
    this.changesets = changesets;
  }

  /**
   * Returns the receiving date.
   */
  public Date getDate()
  {
    return date;
  }

  /**
   * Return collection of changeset ids.
   */
  public Collection<String> getChangesets()
  {
    return changesets;
  }
  
  
}
