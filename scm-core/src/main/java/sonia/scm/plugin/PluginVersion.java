/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

/**
 * Version object for comparing and parsing versions.
 * 
 * TODO for 2.0: rename to version and move to another package.
 *
 * @author Sebastian Sdorra
 */
public class PluginVersion implements Comparable<PluginVersion>
{

  /** the logger for PluginVersion */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginVersion.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new version object
   *
   *
   * @param versionString string representation of the version
   */
  public PluginVersion(String versionString)
  {
    this.unparsedVersion = versionString;

    int index = versionString.indexOf('-');
    String versionPart = null;
    String qualifierPart = null;

    if (index > 0)
    {
      versionPart = versionString.substring(0, index);
      qualifierPart = versionString.substring(index);
    }
    else
    {
      versionPart = versionString;
    }

    parseVersionPart(versionPart);
    type = PluginVersionType.RELEASE;

    if (qualifierPart != null)
    {
      parseQualifierPart(qualifierPart);
    }

    parsedVersion = createParsedVersion();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new version of the given string.
   *
   * TODO throw exception if not parseable
   *
   *
   * @param versionString string representation of the version
   *
   * @return version object
   */
  public static PluginVersion createVersion(String versionString)
  {
    PluginVersion version = null;

    try
    {
      version = new PluginVersion(versionString);
    }
    catch (NumberFormatException ex)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not parse version ".concat(versionString), ex);
      }
    }

    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(PluginVersion o)
  {
    Preconditions.checkNotNull(o);

    //J-
    return ComparisonChain.start()
      .compare(o.major, major)
      .compare(o.minor, minor)
      .compare(o.maintenance, maintenance)
      .compare(o.type.getValue(), type.getValue())
      .compare(o.typeVersion, typeVersion)
      .compareTrueFirst(o.snapshot, snapshot)
      .result();
    //J+
  }

  /**
   * {@inheritDoc}
   */
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

    final PluginVersion other = (PluginVersion) obj;

    return Objects.equal(major, other.major)
      && Objects.equal(minor, other.minor)
      && Objects.equal(maintenance, other.maintenance)
      && Objects.equal(type, other.type)
      && Objects.equal(typeVersion, other.typeVersion)
      && Objects.equal(snapshot, other.snapshot)
      && Objects.equal(parsedVersion, other.parsedVersion);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(major, minor, maintenance, type, typeVersion,
      snapshot, parsedVersion);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return parsedVersion;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the maintenance part of the version.
   *
   *
   * @return maintenance part
   */
  public int getMaintenance()
  {
    return maintenance;
  }

  /**
   * Returns the major part of the version.
   *
   *
   * @return major part
   */
  public int getMajor()
  {
    return major;
  }

  /**
   * Returns the minor part of the version.
   *
   *
   * @return minor part
   */
  public int getMinor()
  {
    return minor;
  }

  /**
   * Returns the string representation of the parsed version.
   *
   *
   * @return parsed version string
   */
  public String getParsedVersion()
  {
    return parsedVersion;
  }

  /**
   * Returns the version type (alpha, beta, milestone, etc.) of the version.
   *
   *
   * @return version type
   */
  public PluginVersionType getType()
  {
    return type;
  }

  /**
   * Returns the version of the type e.g. beta-1 would return 1.
   *
   *
   * @return version of the type
   */
  public int getTypeVersion()
  {
    return typeVersion;
  }

  /**
   * Returns the unparsed string representation of the version.
   *
   *
   * @return unparsed version string
   */
  public String getUnparsedVersion()
  {
    return unparsedVersion;
  }

  /**
   * Returns true if the current version is newer than the given version.
   *
   * @param o other version
   *
   * @return true if newer
   */
  public boolean isNewer(PluginVersion o)
  {
    return compareTo(o) < 0;
  }

  /**
   * Returns true if the current version is newer than the given version.
   *
   *
   * @param versionString other version
   *
   * @return true if newer
   */
  public boolean isNewer(String versionString)
  {
    PluginVersion o = PluginVersion.createVersion(versionString);

    return (o != null) && isNewer(o);
  }

  /**
   * Returns true if the current version is older than the given version.
   *
   *
   * @param o other version
   *
   * @return true if older
   */
  public boolean isOlder(PluginVersion o)
  {
    return compareTo(o) > 0;
  }

  /**
   * Returns true if the current version is older than the given version.
   *
   *
   * @param versionString other version
   *
   * @return true if older
   */
  public boolean isOlder(String versionString)
  {
    PluginVersion o = PluginVersion.createVersion(versionString);

    return (o != null) && isOlder(o);
  }

  /**
   * Returns true if the version is a snapshot.
   *
   *
   * @return true if version is a snapshot
   */
  public boolean isSnapshot()
  {
    return snapshot;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Created parsed string version.
   *
   *
   * @return parsed version
   */
  private String createParsedVersion()
  {
    StringBuilder v = new StringBuilder();

    v.append(major).append(".").append(minor).append(".").append(maintenance);

    String typeId = type.getId();

    if (typeId != null)
    {
      v.append("-").append(typeId).append(typeVersion);
    }

    if (snapshot)
    {
      v.append("-").append("SNAPSHOT");
    }

    return v.toString();
  }

  /**
   * Parses the qualifier part of the version.
   *
   *
   * @param qualifierPart qualifier part
   */
  private void parseQualifierPart(String qualifierPart)
  {
    String qualifier = qualifierPart.trim().toLowerCase(Locale.ENGLISH);

    if (qualifier.contains("snapshot"))
    {
      snapshot = true;
      qualifier = qualifier.replace("snapshot", "");
    }

    if (qualifier.length() > 0)
    {
      for (PluginVersionType versionType : PluginVersionType.values())
      {
        for (String name : versionType.getNames())
        {
          name = name.toLowerCase(Locale.ENGLISH);

          int index = qualifier.indexOf(name);

          if (index > 0)
          {
            type = versionType;
            qualifier = qualifier.substring(index + name.length());
            parseTypeVersion(qualifier);

            break;
          }
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param qualifier
   */
  private void parseTypeVersion(String qualifier)
  {
    String version = null;
    StringBuilder vb = new StringBuilder();

    for (char c : qualifier.toCharArray())
    {
      if (version != null)
      {
        break;
      }
      else if (Character.isDigit(c))
      {
        vb.append(c);
      }
      else if (vb.length() > 0)
      {
        version = vb.toString();
      }
    }

    if (vb.length() > 0)
    {
      version = vb.toString();
    }

    if (version != null)
    {
      typeVersion = Integer.parseInt(version);
    }
  }

  /**
   * Parse version part
   *
   *
   * @param versionPart version part
   */
  private void parseVersionPart(String versionPart)
  {
    String[] parts = versionPart.split("\\.");

    if (parts.length > 0)
    {
      major = Integer.parseInt(parts[0]);

      if (parts.length > 1)
      {
        minor = Integer.parseInt(parts[1]);

        if (parts.length > 2)
        {
          maintenance = Integer.parseInt(parts[2]);
        }
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** parsed version */
  private final String parsedVersion;

  /** unparsed version */
  private final String unparsedVersion;

  /** maintenance part */
  private int maintenance = 0;

  /** major part */
  private int major = 0;

  /** minor part */
  private int minor = 0;

  /** is a snapshot */
  private boolean snapshot;

  /** version type */
  private PluginVersionType type;

  /** type version */
  private int typeVersion = 1;
}
