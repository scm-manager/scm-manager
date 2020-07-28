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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "plugin")
@XmlAccessorType(XmlAccessType.FIELD)
public final class InstalledPluginDescriptor extends ScmModule implements PluginDescriptor
{

  private static final PluginCondition EMPTY_CONDITION = new PluginCondition();

  /**
   * Constructs ...
   *
   */
  InstalledPluginDescriptor() {}

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
  public InstalledPluginDescriptor(int scmVersion, PluginInformation information,
                                   PluginResources resources, PluginCondition condition,
                                   boolean childFirstClassLoader, Set<String> dependencies, Set<String> optionalDependencies)
  {
    this.scmVersion = scmVersion;
    this.information = information;
    this.resources = resources;
    this.condition = condition;
    this.childFirstClassLoader = childFirstClassLoader;
    this.dependencies = dependencies;
    this.optionalDependencies = optionalDependencies;
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

    final InstalledPluginDescriptor other = (InstalledPluginDescriptor) obj;

    return Objects.equal(scmVersion, other.scmVersion)
      && Objects.equal(condition, other.condition)
      && Objects.equal(information, other.information)
      && Objects.equal(resources, other.resources)
      && Objects.equal(childFirstClassLoader, other.childFirstClassLoader)
      && Objects.equal(dependencies, other.dependencies)
      && Objects.equal(optionalDependencies, other.optionalDependencies);
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
      childFirstClassLoader, dependencies, optionalDependencies);
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
                  .add("optionalDependencies", optionalDependencies)
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
  @Override
  public PluginCondition getCondition()
  {
    return MoreObjects.firstNonNull(condition, EMPTY_CONDITION);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 2.0.0
   */
  @Override
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
   *
   * @since 2.0.0
   */
  @Override
  public Set<String> getOptionalDependencies() {
    if (optionalDependencies == null)
    {
      optionalDependencies = ImmutableSet.of();
    }

    return optionalDependencies;
  }

  public Set<String> getDependenciesInclusiveOptionals() {
    return ImmutableSet.copyOf(Iterables.concat(getDependencies(), getOptionalDependencies()));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
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
  @XmlElement(name = "dependency")
  @XmlElementWrapper(name = "optional-dependencies")
  private Set<String> optionalDependencies;

  /** Field description */
  @XmlElement(name = "information")
  private PluginInformation information;

  /** Field description */
  private PluginResources resources;

  /** Field description */
  @XmlElement(name = "scm-version")
  private int scmVersion = 1;
}
