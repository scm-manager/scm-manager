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

package sonia.scm.security;

import com.google.inject.Inject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryRole;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

public class SystemRepositoryPermissionProvider {

  private static final Logger logger = LoggerFactory.getLogger(SystemRepositoryPermissionProvider.class);
  private static final String REPOSITORY_PERMISSION_DESCRIPTOR = "META-INF/scm/repository-permissions.xml";
  private final List<String> availableVerbs;
  private final List<String> readOnlyVerbs;
  private final List<RepositoryRole> availableRoles;

  @Inject
  public SystemRepositoryPermissionProvider(PluginLoader pluginLoader) {
    AvailableRepositoryPermissions availablePermissions = readAvailablePermissions(pluginLoader);
    this.availableVerbs = removeDuplicates(availablePermissions.availableVerbs);
    this.readOnlyVerbs = removeDuplicates(availablePermissions.readOnlyVerbs);
    this.availableRoles = removeDuplicates(availablePermissions.availableRoles.stream().map(r -> new RepositoryRole(r.name, r.verbs.verbs.stream().map(verb -> verb.value).collect(toList()), "system")).collect(toList()));
  }

  public List<String> availableVerbs() {
    return availableVerbs;
  }

  public List<String> readOnlyVerbs() {
    return readOnlyVerbs;
  }

  public List<RepositoryRole> availableRoles() {
    return availableRoles;
  }

  private static AvailableRepositoryPermissions readAvailablePermissions(PluginLoader pluginLoader) {
    Collection<String> availableVerbs = new ArrayList<>();
    Collection<String> readOnlyVerbs = new ArrayList<>();
    Collection<RoleDescriptor> availableRoles = new ArrayList<>();

    try {
      JAXBContext context =
        JAXBContext.newInstance(RepositoryPermissionsRoot.class);

      // Querying permissions from uberClassLoader returns also the permissions from plugin
      Enumeration<URL> descriptorEnum =
        pluginLoader.getUberClassLoader().getResources(REPOSITORY_PERMISSION_DESCRIPTOR);

      while (descriptorEnum.hasMoreElements()) {
        URL descriptorUrl = descriptorEnum.nextElement();

        logger.debug("read repository permission descriptor from {}", descriptorUrl);

        RepositoryPermissionsRoot repositoryPermissionsRoot = parsePermissionDescriptor(context, descriptorUrl);
        repositoryPermissionsRoot.verbs.verbs.forEach(verb -> availableVerbs.add(verb.value));
        repositoryPermissionsRoot.verbs.verbs.stream().filter(verb -> verb.readOnly).map(verb -> verb.value).forEach(readOnlyVerbs::add);
        mergeRolesInto(availableRoles, repositoryPermissionsRoot.roles.roles);
      }
    } catch (IOException ex) {
      logger.error("could not read permission descriptors", ex);
    } catch (JAXBException ex) {
      logger.error(
        "could not create jaxb context to read permission descriptors", ex);
    }

    return new AvailableRepositoryPermissions(availableVerbs, readOnlyVerbs, availableRoles);
  }

  private static void mergeRolesInto(Collection<RoleDescriptor> targetRoles, List<RoleDescriptor> additionalRoles) {
    additionalRoles.forEach(r -> addOrMergeInto(targetRoles, r));
  }

  private static void addOrMergeInto(Collection<RoleDescriptor> targetRoles, RoleDescriptor additionalRole) {
    Optional<RoleDescriptor> existingRole = targetRoles
      .stream()
      .filter(r -> r.name.equals(additionalRole.name))
      .findFirst();
    if (existingRole.isPresent()) {
      existingRole.get().verbs.verbs.addAll(additionalRole.verbs.verbs);
    } else {
      targetRoles.add(additionalRole);
    }
  }

  private static RepositoryPermissionsRoot parsePermissionDescriptor(JAXBContext context, URL descriptorUrl) {
    try {
      RepositoryPermissionsRoot descriptorWrapper =
        (RepositoryPermissionsRoot) context.createUnmarshaller().unmarshal(
          descriptorUrl);
      logger.trace("repository permissions from {}: {}", descriptorUrl, descriptorWrapper.verbs.verbs);
      logger.trace("repository roles from {}: {}", descriptorUrl, descriptorWrapper.roles.roles);
      return descriptorWrapper;
    } catch (JAXBException ex) {
      logger.error("could not parse permission descriptor", ex);
      return new RepositoryPermissionsRoot();
    }
  }

  private static <T> List<T> removeDuplicates(Collection<T> items) {
    return items.stream().distinct().collect(toList());
  }

  private static class AvailableRepositoryPermissions {
    private final Collection<String> availableVerbs;
    private final Collection<String> readOnlyVerbs;
    private final Collection<RoleDescriptor> availableRoles;

    private AvailableRepositoryPermissions(Collection<String> availableVerbs, Collection<String> readOnlyVerbs, Collection<RoleDescriptor> availableRoles) {
      this.availableVerbs = unmodifiableCollection(availableVerbs);
      this.readOnlyVerbs = unmodifiableCollection(readOnlyVerbs);
      this.availableRoles = unmodifiableCollection(availableRoles);
    }
  }

  @XmlRootElement(name = "repository-permissions")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class RepositoryPermissionsRoot {
    private VerbListDescriptor verbs = new VerbListDescriptor();
    private RoleListDescriptor roles = new RoleListDescriptor();
  }

  @XmlRootElement(name = "verbs")
  private static class VerbListDescriptor {
    @XmlElement(name = "verb")
    private Set<Verb> verbs = new LinkedHashSet<>();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "verb")
  private static class Verb {
    @XmlValue
    private String value;
    @XmlAttribute(name = "read-only")
    private boolean readOnly;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;

      if (!(o instanceof Verb)) return false;

      Verb verb = (Verb) o;

      return new EqualsBuilder()
        .append(value, verb.value)
        .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37)
        .append(value)
        .toHashCode();
    }
  }

  @XmlRootElement(name = "roles")
  private static class RoleListDescriptor {
    @XmlElement(name = "role")
    private List<RoleDescriptor> roles = new ArrayList<>();
  }

  @XmlRootElement(name = "role")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class RoleDescriptor {
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "verbs")
    private VerbListDescriptor verbs = new VerbListDescriptor();
  }
}
