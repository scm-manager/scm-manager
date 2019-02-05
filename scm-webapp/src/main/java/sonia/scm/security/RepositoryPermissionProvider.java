package sonia.scm.security;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;

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
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;

public class RepositoryPermissionProvider {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryPermissionProvider.class);
  private static final String REPOSITORY_PERMISSION_DESCRIPTOR = "META-INF/scm/repository-permissions.xml";
  private final Collection<String> availableVerbs;
  private final Collection<RepositoryRole> availableRoles;

  @Inject
  public RepositoryPermissionProvider(PluginLoader pluginLoader) {
    AvailableRepositoryPermissions availablePermissions = readAvailablePermissions(pluginLoader);
    this.availableVerbs = unmodifiableCollection(new LinkedHashSet<>(availablePermissions.availableVerbs));
    this.availableRoles = unmodifiableCollection(new LinkedHashSet<>(availablePermissions.availableRoles.stream().map(r -> new RepositoryRole(r.name, r.verbs.verbs)).collect(Collectors.toList())));
  }

  public Collection<String> availableVerbs() {
    return availableVerbs;
  }

  public Collection<RepositoryRole> availableRoles() {
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
        availableRoles.addAll(repositoryPermissionsRoot.roles.roles);
      }
    } catch (IOException ex) {
      logger.error("could not read permission descriptors", ex);
    } catch (JAXBException ex) {
      logger.error(
        "could not create jaxb context to read permission descriptors", ex);
    }

    return new AvailableRepositoryPermissions(availableVerbs, availableRoles);
  }

  @SuppressWarnings("unchecked")
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
    private List<String> verbs = new ArrayList<>();
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
