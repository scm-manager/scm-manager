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

package sonia.scm.plugin;


import com.google.common.annotations.VisibleForTesting;
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
public class PluginCondition implements Cloneable, Serializable {

  private static final long serialVersionUID = 2406156062634968672L;

  private String arch;

  @XmlElement(name = "min-version")
  private String minVersion;

  @XmlElement(name = "name")
  @XmlElementWrapper(name = "os")
  private List<String> os;

  public PluginCondition() {
  }

  public PluginCondition(String minVersion, List<String> os, String arch) {
    this.minVersion = minVersion;
    this.os = os;
    this.arch = arch;
  }


  /**
   * @since 1.11
   */
  @Override
  public PluginCondition clone() {
    PluginCondition clone = new PluginCondition(minVersion, null, arch);

    if (Util.isNotEmpty(os)) {
      clone.setOs(new ArrayList<String>(os));
    }

    return clone;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final PluginCondition other = (PluginCondition) obj;

    return Objects.equal(arch, other.arch)
      && Objects.equal(minVersion, other.minVersion)
      && Objects.equal(os, other.os);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(arch, minVersion, os);
  }


  @Override
  public String toString() {
    if (minVersion == null && os == null && arch == null) {
      return "No required conditions.";
    }

    StringBuilder message = new StringBuilder("Required conditions: ");

    if (minVersion != null) {
      message.append("minimal SCM version: ").append(minVersion);

      if (os != null || arch != null) {
        message.append("; ");
      }
    }

    if (os != null) {
      message.append("supported OS:");

      for (String currentOS : os) {
        message.append(" ").append(currentOS);
      }

      if (arch != null) {
        message.append("; ");
      }
    }

    if (arch != null) {
      message.append("architecture: ").append(arch);
    }


    return message.toString();
  }


  public String getArch() {
    return arch;
  }


  public String getMinVersion() {
    return minVersion;
  }


  public List<String> getOs() {
    return os;
  }

  /**
   * @deprecated Please use {@link #getConditionCheckResult()} instead.
   */
  @Deprecated
  public boolean isSupported() {
    return isSupported(SCMContext.getContext().getVersion(),
      SystemUtil.getOS(), SystemUtil.getArch());
  }

  @VisibleForTesting
  boolean isSupported(String version, String os, String arch) {
    return getConditionCheckResult(version, os, arch) == CheckResult.OK;
  }

  public CheckResult getConditionCheckResult() {
    return getConditionCheckResult(SCMContext.getContext().getVersion(), SystemUtil.getOS(), SystemUtil.getArch());
  }

  public CheckResult getConditionCheckResult(String version, String os, String arch) {
    if (!isVersionConditionMet(version)) {
      return CheckResult.VERSION_MISMATCH;
    } else if (!isOsConditionMet(os)) {
      return CheckResult.OS_MISMATCH;
    } else if (!isArchConditionMet(arch)) {
      return CheckResult.ARCHITECTURE_MISMATCH;
    } else {
      return CheckResult.OK;
    }
  }

  private boolean isArchConditionMet(String arch) {
    if (!(arch != null && this.arch == null) && Util.isNotEmpty(arch) && Util.isNotEmpty(arch)) {
      return arch.equals(this.arch);
    }
    return this.arch == null;
  }

  private boolean isOsConditionMet(String os) {
    if (!(os != null && this.os == null) && Util.isNotEmpty(this.os) && Util.isNotEmpty(os)) {

      PlatformType platformType = PlatformType.createPlatformType(os);

      for (String osType : this.os) {

        if (isOs(osType, platformType)) {
          return true;
        }
      }
      return false;
    }
    return this.os == null;
  }

  private boolean isVersionConditionMet(String version) {

    if (!(version != null && minVersion == null) && Util.isNotEmpty(minVersion) && Util.isNotEmpty(version)) {
      return minVersion.equalsIgnoreCase(version) || Version.parse(version).isNewer(minVersion);

    } else {
      return minVersion == null;
    }
  }


  public void setArch(String arch) {
    this.arch = arch;
  }

  public void setMinVersion(String minVersion) {
    this.minVersion = minVersion;
  }

  public void setOs(List<String> os) {
    this.os = os;
  }


  private boolean isOs(String osType, PlatformType type) {
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

  public enum CheckResult {
    OK,
    VERSION_MISMATCH,
    OS_MISMATCH,
    ARCHITECTURE_MISMATCH
  }
}
