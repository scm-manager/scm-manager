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

package sonia.scm.repository;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import sonia.scm.auditlog.AuditEntry;


@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = {"svn", "config"})
public class SvnConfig extends RepositoryConfig
{

  @SuppressWarnings("WeakerAccess") // This might be needed for permission checking
  public static final String PERMISSION = "svn";

  @XmlElement(name = "enable-gzip")
  private boolean enabledGZip = false;

  private Compatibility compatibility = Compatibility.NONE;

  public Compatibility getCompatibility()
  {
    if (compatibility == null)
    {
      compatibility = Compatibility.NONE;
    }

    return compatibility;
  }

  
  public boolean isEnabledGZip()
  {
    return enabledGZip;
  }



  public void setCompatibility(Compatibility compatibility)
  {
    this.compatibility = compatibility;
  }


  public void setEnabledGZip(boolean enabledGZip)
  {
    this.enabledGZip = enabledGZip;
  }

  @Override
  @XmlTransient // Only for permission checks, don't serialize to XML
  public String getId() {
    // Don't change this without migrating SCM permission configuration!
    return PERMISSION;
  }
}
