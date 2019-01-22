package sonia.scm.security;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.ConfigurationEntryStoreFactory;

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryPermissionAssigner {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryPermissionAssigner.class);
  private static final String NAME = "permissions";
  private static final String REPOSITORY_PERMISSION_DESCRIPTOR = "META-INF/scm/repository-permissions.xml";
  private final ConfigurationEntryStoreFactory storeFactory;
  private final AvailableRepositoryPermissions availablePermissions;

  @Inject
  public RepositoryPermissionAssigner(ConfigurationEntryStoreFactory storeFactory, PluginLoader pluginLoader) {
    this.storeFactory = storeFactory;
    this.availablePermissions = readAvailablePermissions(pluginLoader);
  }

  public Collection<String> availableVerbs() {
    return availablePermissions.availableVerbs;
  }

  public Collection<RoleDescriptor> availableRoles() {
    return availablePermissions.availableRoles;
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

        logger.debug("read permission descriptor from {}", descriptorUrl);

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
      logger.trace("permissions from {}: {}", descriptorUrl, descriptorWrapper);
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
      this.availableVerbs = Collections.unmodifiableCollection(availableVerbs);
      this.availableRoles = Collections.unmodifiableCollection(availableRoles);
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

    public Collection<String> getVerbs() {
      return Collections.unmodifiableCollection(verbs.verbs);
    }

    public String toString() {
      return "Role " + name + " (" + verbs.verbs.stream().collect(Collectors.joining(", ")) + ")";
    }
  }
}
