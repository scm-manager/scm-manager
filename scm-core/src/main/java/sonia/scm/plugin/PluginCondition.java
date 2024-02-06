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
    
package sonia.scm.plugin;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.PlatformType;
import sonia.scm.SCMContext;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;
import sonia.scm.version.Version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@XmlRootElement(name = "conditions")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginCondition implements Cloneable, Serializable
{

  private static final long serialVersionUID = 2406156062634968672L;

  private String arch;

  @XmlElement(name = "min-version")
  private String minVersion;

  @XmlElement(name = "name")
  @XmlElementWrapper(name = "os")
  private List<String> os;

  public PluginCondition() {}

  public PluginCondition(String minVersion, List<String> os, String arch)
  {
    this.minVersion = minVersion;
    this.os = os;
    this.arch = arch;
  }


  /**
   * @since 1.11
   */
  @Override
  public PluginCondition clone()
  {
    PluginCondition clone = new PluginCondition(minVersion, null, arch);

    if (Util.isNotEmpty(os))
    {
      clone.setOs(new ArrayList<String>(os));
    }

    return clone;
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

    final PluginCondition other = (PluginCondition) obj;

    return Objects.equal(arch, other.arch)
      && Objects.equal(minVersion, other.minVersion)
      && Objects.equal(os, other.os);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(arch, minVersion, os);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("arch", arch)
                  .add("minVersion", minVersion)
                  .add("os", os)
                  .toString();
    //J+
  }


  
  public String getArch()
  {
    return arch;
  }

  
  public String getMinVersion()
  {
    return minVersion;
  }

  
  public List<String> getOs()
  {
    return os;
  }

  
  public boolean isSupported()
  {
    return isSupported(SCMContext.getContext().getVersion(),
      SystemUtil.getOS(), SystemUtil.getArch());
  }

  public boolean isSupported(String version, String os, String arch)
  {
    boolean supported = true;

    if (Util.isNotEmpty(minVersion) && Util.isNotEmpty(version))
    {
      supported = (minVersion.equalsIgnoreCase(version)
        || Version.parse(version).isNewer(minVersion));
    }

    if (supported && Util.isNotEmpty(this.os) && Util.isNotEmpty(os))
    {
      supported = false;

      PlatformType platformType = PlatformType.createPlatformType(os);

      for (String osType : this.os)
      {
        supported = isOs(osType, platformType);

        if (supported)
        {
          break;
        }
      }
    }

    if (supported && Util.isNotEmpty(this.arch) && Util.isNotEmpty(arch))
    {
      supported = arch.equals(this.arch);
    }

    return supported;
  }


  public void setArch(String arch)
  {
    this.arch = arch;
  }

  public void setMinVersion(String minVersion)
  {
    this.minVersion = minVersion;
  }

  public void setOs(List<String> os)
  {
    this.os = os;
  }


  private boolean isOs(String osType, PlatformType type)
  {
    osType = osType.toLowerCase(Locale.ENGLISH);

    //J-
    return ((osType.indexOf("win") >= 0) && (PlatformType.WINDOWS == type))
      || ((osType.indexOf("unix") >= 0) && type.isUnix())
      || ((osType.indexOf("posix") >= 0) && type.isPosix())
      || ((osType.indexOf("mac") >= 0) && (PlatformType.MAC == type))
      || ((osType.indexOf("linux") >= 0) && (PlatformType.LINUX == type))
      || ((osType.indexOf("solaris") >= 0) && (PlatformType.SOLARIS == type)) 
      || ((osType.indexOf("openbsd") >= 0) && (PlatformType.OPENBSD == type)) 
      || ((osType.indexOf("freebsd") >= 0) && (PlatformType.FREEBSD == type));
    //J+
  }

}
