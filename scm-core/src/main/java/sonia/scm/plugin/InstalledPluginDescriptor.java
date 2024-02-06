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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Set;
import java.util.stream.Collectors;


@XmlRootElement(name = "plugin")
@XmlAccessorType(XmlAccessType.FIELD)
public final class InstalledPluginDescriptor extends ScmModule implements PluginDescriptor
{

  @XmlElement(name = "child-first-classloader")
  private boolean childFirstClassLoader;

  @XmlElement(name = "conditions")
  private PluginCondition condition;

  @XmlElement(name = "dependency")
  @XmlElementWrapper(name = "dependencies")
  private Set<NameAndVersion> dependencies;

  @XmlElement(name = "dependency")
  @XmlElementWrapper(name = "optional-dependencies")
  private Set<NameAndVersion> optionalDependencies;

  @XmlElement(name = "information")
  private PluginInformation information;

  private PluginResources resources;

  @XmlElement(name = "scm-version")
  private int scmVersion = 1;

  private static final PluginCondition EMPTY_CONDITION = new PluginCondition();

  InstalledPluginDescriptor() {}

  /**
   * @deprecated this constructor uses dependencies with plain strings,
   *             which is deprecated because the version information is missing.
   *             This class should not instantiated manually, it is designed to be loaded by jaxb.
   */
  @Deprecated
  public InstalledPluginDescriptor(int scmVersion, PluginInformation information,
                                   PluginResources resources, PluginCondition condition,
                                   boolean childFirstClassLoader, Set<String> dependencies, Set<String> optionalDependencies)
  {
    this.scmVersion = scmVersion;
    this.information = information;
    this.resources = resources;
    this.condition = condition;
    this.childFirstClassLoader = childFirstClassLoader;
    this.dependencies = mapToNameAndVersionSet(dependencies);
    this.optionalDependencies = mapToNameAndVersionSet(optionalDependencies);
  }

  private static Set<NameAndVersion> mapToNameAndVersionSet(Set<String> dependencies) {
    if (dependencies == null){
      return ImmutableSet.of();
    }
    return dependencies.stream()
      .map(d -> new NameAndVersion(d, null))
      .collect(Collectors.toSet());
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

    final InstalledPluginDescriptor other = (InstalledPluginDescriptor) obj;

    return Objects.equal(scmVersion, other.scmVersion)
      && Objects.equal(condition, other.condition)
      && Objects.equal(information, other.information)
      && Objects.equal(resources, other.resources)
      && Objects.equal(childFirstClassLoader, other.childFirstClassLoader)
      && Objects.equal(dependencies, other.dependencies)
      && Objects.equal(optionalDependencies, other.optionalDependencies);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(scmVersion, condition, information, resources,
      childFirstClassLoader, dependencies, optionalDependencies);
  }

  
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


  
  @Override
  public PluginCondition getCondition()
  {
    return MoreObjects.firstNonNull(condition, EMPTY_CONDITION);
  }

  /**
   * @since 2.0.0
   */
  @Override
  public Set<String> getDependencies() {
    return mapToStringSet(getDependenciesWithVersion());
  }

  /**
   * Returns name and versions of the plugins which are this plugin depends on.
   * @since 2.4.0
   */
  public Set<NameAndVersion> getDependenciesWithVersion() {
    if (dependencies == null) {
      dependencies = ImmutableSet.of();
    }
    return dependencies;
  }

  /**
   * @since 2.0.0
   */
  @Override
  public Set<String> getOptionalDependencies() {
    return mapToStringSet(getOptionalDependenciesWithVersion());
  }

  /**
   * Returns name and versions of the plugins which are this plugin optional depends on.
   * @since 2.4.0
   */
  public Set<NameAndVersion> getOptionalDependenciesWithVersion() {
    if (optionalDependencies == null) {
      optionalDependencies = ImmutableSet.of();
    }
    return optionalDependencies;
  }

  public Set<String> getDependenciesInclusiveOptionals() {
    return ImmutableSet.copyOf(Iterables.concat(getDependencies(), getOptionalDependencies()));
  }

  private Set<String> mapToStringSet(Set<NameAndVersion> dependencies) {
    return dependencies.stream()
      .map(NameAndVersion::getName)
      .collect(Collectors.toSet());
  }

  
  @Override
  public PluginInformation getInformation()
  {
    return information;
  }

  
  public PluginResources getResources()
  {
    return resources;
  }

  
  public int getScmVersion()
  {
    return scmVersion;
  }

  
  public boolean isChildFirstClassLoader()
  {
    return childFirstClassLoader;
  }

}
