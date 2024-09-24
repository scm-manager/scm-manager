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


import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.util.Util;



@XmlRootElement(name = "config")
@AuditEntry(labels = {"hg", "config"})
public class HgGlobalConfig extends RepositoryConfig {

  public static final String PERMISSION = "hg";

  private String encoding = "UTF-8";

  private String hgBinary;

  private boolean showRevisionInId = false;

  private boolean enableHttpPostArgs = false;

  @Override
  @XmlTransient // Only for permission checks, don't serialize to XML
  public String getId() {
    // Don't change this without migrating SCM permission configuration!
    return PERMISSION;
  }

  
  public String getEncoding()
  {
    return encoding;
  }

  
  public String getHgBinary()
  {
    return hgBinary;
  }

  
  public boolean isShowRevisionInId()
  {
    return showRevisionInId;
  }

  public boolean isEnableHttpPostArgs() {
    return enableHttpPostArgs;
  }

  
  @Override
  public boolean isValid()
  {
    return super.isValid() && Util.isNotEmpty(hgBinary);
  }



  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }


  public void setHgBinary(String hgBinary)
  {
    this.hgBinary = hgBinary;
  }


  public void setShowRevisionInId(boolean showRevisionInId)
  {
    this.showRevisionInId = showRevisionInId;
  }

  public void setEnableHttpPostArgs(boolean enableHttpPostArgs) {
    this.enableHttpPostArgs = enableHttpPostArgs;
  }

}
