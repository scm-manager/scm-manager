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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import javax.xml.bind.annotation.*;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Plugin extends ScmModule
{

  /**
   * Constructs ...
   *
   */
  Plugin() {}

  /**
   * Constructs ...
   *
   *
   * @param scmVersion
   * @param information
   * @param resources
   * @param condition
   * @param childFirstClassLoader
   * @param dependencies
   */
  public Plugin(int scmVersion, PluginInformation information,
    PluginResources resources, PluginCondition condition,
    boolean childFirstClassLoader, Set<String> dependencies)
  {
    this.scmVersion = scmVersion;
    this.information = information;
    this.resources = resources;
    this.condition = condition;
    this.childFirstClassLoader = childFirstClassLoader;
    this.dependencies = dependencies;
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

    final Plugin other = (Plugin) obj;

    return Objects.equal(scmVersion, other.scmVersion)
      && Objects.equal(condition, other.condition)
      && Objects.equal(information, other.information)
      && Objects.equal(resources, other.resources)
      && Objects.equal(childFirstClassLoader, other.childFirstClassLoader)
      && Objects.equal(dependencies, other.dependencies);
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
    return Objects.hashCode(scmVersion, condition, information, resources,
      childFirstClassLoader, dependencies);
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
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("scmVersion", scmVersion)
                      .add("condition", condition)
                      .add("information", information)
                      .add("resources", resources)
                      .add("childFirstClassLoader", childFirstClassLoader)
                      .add("dependencies", dependencies)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginCondition getCondition()
  {
    return condition;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 2.0.0
   */
  public Set<String> getDependencies()
  {
    if (dependencies == null)
    {
      dependencies = ImmutableSet.of();
    }

    return dependencies;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginInformation getInformation()
  {
    return information;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginResources getResources()
  {
    return resources;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getScmVersion()
  {
    return scmVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isChildFirstClassLoader()
  {
    return childFirstClassLoader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "child-first-classloader")
  private boolean childFirstClassLoader;

  /** Field description */
  @XmlElement(name = "conditions")
  private PluginCondition condition;

  /** Field description */
  @XmlElement(name = "dependency")
  @XmlElementWrapper(name = "dependencies")
  private Set<String> dependencies;

  /** Field description */
  private PluginInformation information;

  /** Field description */
  private PluginResources resources;

  /** Field description */
  @XmlElement(name = "scm-version")
  private int scmVersion = 1;
}
