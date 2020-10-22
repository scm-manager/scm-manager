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

package sonia.scm.version;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------

/**
 * Version object for comparing and parsing versions.
 *
 *
 * @author Sebastian Sdorra
 */
public final class Version implements Comparable<Version>
{

  private static final Pattern MAVEN_UNIQUE_SNAPSHOT = Pattern.compile("-[0-9]{8}\\.[0-9]{6}-[0-9]+");

  /**
   * Constructs a new version object
   *
   *
   * @param versionString string representation of the version
   */
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

  //~--- methods --------------------------------------------------------------

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

  /**
   * {@inheritDoc}
   */
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

    final Version other = (Version) obj;

    return Objects.equal(major, other.major)
      && Objects.equal(minor, other.minor) && Objects.equal(patch, other.patch)
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
    return Objects.hashCode(major, minor, patch, type, typeVersion, snapshot,
      parsedVersion);
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
   * Returns the patch part of the version.
   *
   *
   * @return patch part
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
   *
   *
   * @return unparsed version string
   */
  public String getUnparsedVersion()
  {
    return unparsedVersion;
  }

  /**
   * Returns true if the given version is newer.
   *
   * @param o other version
   *
   * @return true if newer
   */
  public boolean isNewer(Version o)
  {
    return compareTo(o) < 0;
  }

  /**
   * Returns true if the given version is newer.
   *
   *
   * @param versionString other version
   *
   * @return true if newer
   */
  public boolean isNewer(String versionString) {
    return isNewer(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is newer or equal.
   * @param versionString other version
   * @return true if newer
   * @since 2.4.0
   */
  public boolean isNewerOrEqual(String versionString) {
    return isNewerOrEqual(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is newer or equal.
   * @param o other version
   * @return {@code true} if newer or equal
   * @since 2.4.0
   */
  public boolean isNewerOrEqual(Version o) {
    return compareTo(o) <= 0;
  }

  /**
   * Returns true if the given version is older.
   *
   *
   * @param o other version
   *
   * @return true if older
   */
  public boolean isOlder(Version o)
  {
    return compareTo(o) > 0;
  }

  /**
   * Returns true if the given version is older.
   *
   *
   * @param versionString other version
   *
   * @return true if older
   */
  public boolean isOlder(String versionString) {
    return isOlder(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is older or equal.
   * @param versionString other version
   * @return {@code true} if older or equal
   * @since 2.4.0
   */
  public boolean isOlderOrEqual(String versionString) {
    return isOlderOrEqual(Version.parse(versionString));
  }

  /**
   * Returns true if the given version is older or equal.
   * @param o other version
   * @return {@code true} if older or equal
   * @since 2.4.0
   */
  public boolean isOlderOrEqual(Version o) {
    return compareTo(o) >= 0;
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
   *
   *
   * @param qualifierPart qualifier part
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
          patch = Integer.parseInt(parts[2]);
        }
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** parsed version */
  private final String parsedVersion;

  /** unparsed version */
  private final String unparsedVersion;

  /** major part */
  private int major = 0;

  /** minor part */
  private int minor = 0;

  /** patch part */
  private int patch = 0;

  /** is a snapshot */
  private boolean snapshot;

  /** version type */
  private VersionType type;

  /** type version */
  private int typeVersion = 1;
}
