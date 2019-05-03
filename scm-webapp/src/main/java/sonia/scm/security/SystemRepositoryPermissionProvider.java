package sonia.scm.security;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryRole;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;

class SystemRepositoryPermissionProvider {

  private static final Logger logger = LoggerFactory.getLogger(SystemRepositoryPermissionProvider.class);
  private static final String REPOSITORY_PERMISSION_DESCRIPTOR = "META-INF/scm/repository-permissions.xml";
  private final List<String> availableVerbs;
  private final List<RepositoryRole> availableRoles;

  @Inject
  public SystemRepositoryPermissionProvider(PluginLoader pluginLoader) {
    AvailableRepositoryPermissions availablePermissions = readAvailablePermissions(pluginLoader);
    this.availableVerbs = unmodifiableList(new ArrayList<>(availablePermissions.availableVerbs));
    this.availableRoles = unmodifiableList(new ArrayList<>(availablePermissions.availableRoles.stream().map(r -> new RepositoryRole(r.name, r.verbs.verbs, "system")).collect(Collectors.toList())));
  }

  public List<String> availableVerbs() {
    return availableVerbs;
  }

  public List<RepositoryRole> availableRoles() {
    return availableRoles;
  }

  private static AvailableRepositoryPermissions readAvailablePermissions(PluginLoader pluginLoader) {
    Collection<String> availableVerbs = new ArrayList<>();
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
        availableVerbs.addAll(repositoryPermissionsRoot.verbs.verbs);
        mergeRolesInto(availableRoles, repositoryPermissionsRoot.roles.roles);
      }
    } catch (IOException ex) {
      logger.error("could not read permission descriptors", ex);
    } catch (JAXBException ex) {
      logger.error(
        "could not create jaxb context to read permission descriptors", ex);
    }

    return new AvailableRepositoryPermissions(availableVerbs, availableRoles);
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

  private static class AvailableRepositoryPermissions {
    private final Collection<String> availableVerbs;
    private final Collection<RoleDescriptor> availableRoles;

    private AvailableRepositoryPermissions(Collection<String> availableVerbs, Collection<RoleDescriptor> availableRoles) {
      this.availableVerbs = unmodifiableCollection(availableVerbs);
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
    private Set<String> verbs = new LinkedHashSet<>();
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
