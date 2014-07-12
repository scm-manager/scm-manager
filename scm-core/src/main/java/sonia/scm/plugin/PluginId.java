/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

/**
 * Id of a plugin. The id of a plugin consists of the groupId, artifactId and 
 * the version of the plugin.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class PluginId
{

  /** Field description */
  private static final String DELIMITER = ":";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param groupId
   * @param artifactId
   * @param version
   */
  public PluginId(String groupId, String artifactId, String version)
  {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
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

    final PluginId other = (PluginId) obj;

    return Objects.equal(this.groupId, other.groupId)
      && Objects.equal(this.artifactId, other.artifactId)
      && Objects.equal(this.version, other.version);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(groupId, artifactId, version);
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
    return Joiner.on(DELIMITER).join(groupId, artifactId, version);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getArtifactId()
  {
    return artifactId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getGroupId()
  {
    return groupId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getIdWithoutVersion()
  {
    return Joiner.on(DELIMITER).join(groupId, artifactId);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getVersion()
  {
    return version;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String artifactId;

  /** Field description */
  private final String groupId;

  /** Field description */
  private final String version;
}
