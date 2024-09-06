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

package sonia.scm.version;


import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version object for comparing and parsing versions.
 */
public final class Version implements Comparable<Version>
{
  private final String parsedVersion;

  private final String unparsedVersion;

  /** major part */
  private int major = 0;

  /** minor part */
  private int minor = 0;

  /** patch part */
  private int patch = 0;

  private boolean snapshot;

  private VersionType type;

  private int typeVersion = 1;

  private static final Pattern MAVEN_UNIQUE_SNAPSHOT = Pattern.compile("-[0-9]{8}\\.[0-9]{6}-[0-9]+");

  private Version(String versionString)
  {
    this.unparsedVersion = versionString;

    int index = versionString.indexOf('-');
    String versionPart;
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
    type = VersionType.RELEASE;

    if (qualifierPart != null)
    {
      parseQualifierPart(qualifierPart);
    }

    parsedVersion = createParsedVersion();
  }


  /**
   * Creates a new version of the given string.
   *
   * @param versionString string representation of the version
   *
   * @return version object
   */
  public static Version parse(String versionString)
  {
    Version version = null;

    try
    {
      version = new Version(versionString);
    }
    catch (NumberFormatException ex)
    {
      throw new VersionParseException(
        "could not parse version ".concat(versionString), ex);
    }

    return version;
  }


  @Override
  public int compareTo(Version o)
  {
    Preconditions.checkNotNull(o);

    //J-
    return ComparisonChain.start()
      .compare(o.major, major)
      .compare(o.minor, minor)
      .compare(o.patch, patch)
      .compare(o.type.getValue(), type.getValue())
      .compare(o.typeVersion, typeVersion)
      .compareTrueFirst(o.snapshot, snapshot)
      .result();
    //J+
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

    final Version other = (Version) obj;

    return Objects.equal(major, other.major)
      && Objects.equal(minor, other.minor) && Objects.equal(patch, other.patch)
      && Objects.equal(type, other.type)
      && Objects.equal(typeVersion, other.typeVersion)
      && Objects.equal(snapshot, other.snapshot)
      && Objects.equal(parsedVersion, other.parsedVersion);
  }


  @Override
  public int hashCode()
  {
    return Objects.hashCode(major, minor, patch, type, typeVersion, snapshot,
      parsedVersion);
  }


  @Override
  public String toString()
  {
    return parsedVersion;
  }


  /**
   * Returns the major part of the version.
   */
  public int getMajor()
  {
    return major;
  }

  /**
   * Returns the minor part of the version.
   */
  public int getMinor()
  {
    return minor;
  }

  /**
   * Returns the string representation of the parsed version.
   */
  public String getParsedVersion()
  {
    return parsedVersion;
  }

  /**
   * Returns the patch part of the version.
   */
  public int getPatch()
  {
    return patch;
  }

  /**
   * Returns the version type (alpha, beta, milestone, etc.) of the version.
   *
   *
   * @return version type
   */
  public VersionType getType()
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
   */
  public String getUnparsedVersion()
  {
    return unparsedVersion;
  }

  /**
   * Returns true if the given version is newer.
   */
  public boolean isNewer(Version o)
  {
    return compareTo(o) < 0;
  }

  /**
   * Returns true if the given version is newer.
   */
  public boolean isNewer(String versionString) {
    return isNewer(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is newer or equal.
   *
   * @since 2.4.0
   */
  public boolean isNewerOrEqual(String versionString) {
    return isNewerOrEqual(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is newer or equal.
   *
   * @since 2.4.0
   */
  public boolean isNewerOrEqual(Version o) {
    return compareTo(o) <= 0;
  }

  /**
   * Returns true if the given version is older.
   */
  public boolean isOlder(Version o)
  {
    return compareTo(o) > 0;
  }

  /**
   * Returns true if the given version is older.
   */
  public boolean isOlder(String versionString) {
    return isOlder(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is older or equal.
   *
   * @since 2.4.0
   */
  public boolean isOlderOrEqual(String versionString) {
    return isOlderOrEqual(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is older or equal.
   *
   * @since 2.4.0
   */
  public boolean isOlderOrEqual(Version o) {
    return compareTo(o) >= 0;
  }


  /**
   * Returns true if the version is a snapshot.
   */
  public boolean isSnapshot()
  {
    return snapshot;
  }

  /**
   * Created parsed string version.
   */
  private String createParsedVersion()
  {
    StringBuilder v = new StringBuilder();

    v.append(major).append(".").append(minor).append(".").append(patch);

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
   */
  private void parseQualifierPart(String qualifierPart)
  {
    String qualifier = qualifierPart.trim().toLowerCase(Locale.ENGLISH);

    if (qualifier.contains("snapshot")) {
      snapshot = true;
      qualifier = qualifier.replace("snapshot", "");
    } else {
      Matcher matcher = MAVEN_UNIQUE_SNAPSHOT.matcher(qualifier);
      if (matcher.matches()) {
        snapshot = true;
        qualifier = matcher.replaceAll("-");
      }
    }

    if (qualifier.length() > 0)
    {
      for (VersionType versionType : VersionType.values())
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
          patch = Integer.parseInt(parts[2]);
        }
      }
    }
  }

}
