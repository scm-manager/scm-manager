package sonia.scm.user.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static sonia.scm.version.Version.parse;

@Extension
public class XmlUserV1UpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(XmlUserV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlUserDAO userDAO;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public XmlUserV1UpdateStep(SCMContextProvider contextProvider, XmlUserDAO userDAO, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.contextProvider = contextProvider;
    this.userDAO = userDAO;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() throws JAXBException {
    Optional<Path> v1UsersFile = determineV1File();
    if (!v1UsersFile.isPresent()) {
      LOG.info("no v1 file for users found");
      return;
    }
    Collection<String> adminUsers = determineAdminUsers();
    LOG.debug("found the following admin users from global config: {}", adminUsers);
    XmlUserV1UpdateStep.V1UserDatabase v1Database = readV1Database(v1UsersFile.get());
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    v1Database.userList.users.forEach(user -> update(user, adminUsers, securityStore));
  }

  private Collection<String> determineAdminUsers() throws JAXBException {
    Path configDirectory = determineConfigDirectory();
    Path existingConfigFile = configDirectory.resolve("config" + StoreConstants.FILE_EXTENSION);
    if (existingConfigFile.toFile().exists()) {
      return extractAdminUsersFromConfigFile(existingConfigFile);
    } else {
      return emptyList();
    }
  }

  private Collection<String> extractAdminUsersFromConfigFile(Path existingConfigFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlUserV1UpdateStep.V1Configuration.class);
    V1Configuration v1Configuration = (V1Configuration) jaxbContext.createUnmarshaller().unmarshal(existingConfigFile.toFile());
    return ofNullable(v1Configuration.adminUsers)
      .map(userList -> userList.split(","))
      .map(Arrays::asList)
      .orElse(emptyList());
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.user.xml";
  }

  private void update(V1User v1User, Collection<String> adminUsers, ConfigurationEntryStore<AssignedPermission> securityStore) {
    LOG.debug("updating user {}", v1User.name);
    User user = new User(
      v1User.name,
      v1User.displayName,
      v1User.mail,
      v1User.password,
      v1User.type,
      v1User.active);
    user.setCreationDate(v1User.creationDate);
    user.setLastModified(v1User.lastModified);
    userDAO.add(user);

    if (v1User.admin || adminUsers.contains(v1User.name)) {
      LOG.debug("setting admin permissions for user {}", v1User.name);
      securityStore.put(new AssignedPermission(v1User.name, "*"));
    }
  }

  private XmlUserV1UpdateStep.V1UserDatabase readV1Database(Path v1UsersFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlUserV1UpdateStep.V1UserDatabase.class);
    return (XmlUserV1UpdateStep.V1UserDatabase) jaxbContext.createUnmarshaller().unmarshal(v1UsersFile.toFile());
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  private Optional<Path> determineV1File() {
    Path configDirectory = determineConfigDirectory();
    Path existingUsersFile = configDirectory.resolve("users" + StoreConstants.FILE_EXTENSION);
    Path usersV1File = configDirectory.resolve("usersV1" + StoreConstants.FILE_EXTENSION);
    if (existingUsersFile.toFile().exists()) {
      try {
        Files.move(existingUsersFile, usersV1File);
      } catch (IOException e) {
        throw new UpdateException("could not move old users file to " + usersV1File.toAbsolutePath());
      }
      LOG.info("moved old users file to {}", usersV1File.toAbsolutePath());
      return of(usersV1File);
    }
    return empty();
  }

  private Path determineConfigDirectory() {
    return new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME).toPath();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "user")
  private static class V1User {
    private Map<String, String> properties;
    private boolean admin;
    private long creationDate;
    private String displayName;
    private Long lastModified;
    private String mail;
    private String name;
    private String password;
    private String type;
    private boolean active;

    @Override
    public String toString() {
      return "V1User{" +
        "properties=" + properties +
        ", admin='" + admin + '\'' +
        ", creationDate=" + creationDate + '\'' +
        ", displayName=" + displayName + '\'' +
        ", lastModified=" + lastModified + '\'' +
        ", mail='" + mail + '\'' +
        ", name='" + name + '\'' +
        ", password=" + password + '\'' +
        ", type='" + type + '\'' +
        ", active='" + active + '\'' +
        '}';
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "scm-config")
  private static class V1Configuration {
    @XmlElement(name = "admin-users")
    private String adminUsers;
  }

  private static class UserList {
    @XmlElement(name = "user")
    private List<V1User> users;
  }

  @XmlRootElement(name = "user-db")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class V1UserDatabase {
    private long creationTime;
    private Long lastModified;
    @XmlElement(name = "users")
    private XmlUserV1UpdateStep.UserList userList;
  }

}
