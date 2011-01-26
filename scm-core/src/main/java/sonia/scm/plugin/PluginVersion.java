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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.AssertUtil;

/**
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
   * Constructs ...
   *
   *
   * @param versionString
   */
  public PluginVersion(String versionString)
  {
    this.unparsedVersion = versionString;

    int index = versionString.indexOf("-");
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
   * Enum description
   *
   *
   * @param versionString
   *
   * @return
   */

  /**
   * Method description
   *
   *
   * @param versionString
   *
   * @return
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
   * Method description
   *
   *
   * @param o
   *
   * @return
   */
  @Override
  public int compareTo(PluginVersion o)
  {
    AssertUtil.assertIsNotNull(o);

    int result = o.major - major;

    if (result == 0)
    {
      result = o.minor - minor;

      if (result == 0)
      {
        result = o.maintenance - maintenance;

        if (result == 0)
        {
          result = o.type.getValue() - type.getValue();

          if (result == 0)
          {
            result = o.typeVersion - typeVersion;

            if (result == 0)
            {
              if (o.snapshot &&!snapshot)
              {
                result = 0;
              }
              else if (!o.snapshot && snapshot)
              {
                result = 1;
              }
            }
          }
        }
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    return parsedVersion;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMaintenance()
  {
    return maintenance;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMajor()
  {
    return major;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMinor()
  {
    return minor;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getParsedVersion()
  {
    return parsedVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginVersionType getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getTypeVersion()
  {
    return typeVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUnparsedVersion()
  {
    return unparsedVersion;
  }

  /**
   * Method description
   *
   *
   * @param o
   *
   * @return
   */
  public boolean isNewer(PluginVersion o)
  {
    return compareTo(o) < 0;
  }

  /**
   * Method description
   *
   *
   * @param versionString
   *
   * @return
   */
  public boolean isNewer(String versionString)
  {
    PluginVersion o = PluginVersion.createVersion(versionString);

    return (o != null) && isNewer(o);
  }

  /**
   * Method description
   *
   *
   * @param o
   *
   * @return
   */
  public boolean isOlder(PluginVersion o)
  {
    return compareTo(o) > 0;
  }

  /**
   * Method description
   *
   *
   * @param versionString
   *
   * @return
   */
  public boolean isOlder(String versionString)
  {
    PluginVersion o = PluginVersion.createVersion(versionString);

    return (o != null) && isOlder(o);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSnapshot()
  {
    return snapshot;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
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
   * Method description
   *
   *
   * @param qualifierPart
   */
  private void parseQualifierPart(String qualifierPart)
  {
    String qualifier = qualifierPart.trim().toLowerCase();

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
          name = name.toLowerCase();

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
   * Method description
   *
   *
   * @param versionPart
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

  /** Field description */
  private int maintenance = 0;

  /** Field description */
  private int major = 0;

  /** Field description */
  private int minor = 0;

  /** Field description */
  private String parsedVersion;

  /** Field description */
  private boolean snapshot;

  /** Field description */
  private PluginVersionType type;

  /** Field description */
  private int typeVersion = 1;

  /** Field description */
  private String unparsedVersion;
}
