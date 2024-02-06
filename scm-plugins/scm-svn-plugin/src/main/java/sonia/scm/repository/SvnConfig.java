/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
