package sonia.scm.user.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
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
import java.util.List;
import java.util.Map;

import static sonia.scm.version.Version.parse;

@Extension
public class XmlUserV1UpdateStep implements UpdateStep {

  private static Logger LOG = LoggerFactory.getLogger(XmlUserV1UpdateStep.class);

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
    if (!determineV1File().exists()) {
      return;
    }
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlUserV1UpdateStep.V1UserDatabase.class);
    XmlUserV1UpdateStep.V1UserDatabase v1Database = readV1Database(jaxbContext);
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    v1Database.userList.users.forEach(user -> update(user, securityStore));
  }

  @Override
  public Version getTargetVersion() {
    return parse("0.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.user.xml";
  }

  private void update(XmlUserV1UpdateStep.V1User v1User, ConfigurationEntryStore<AssignedPermission> securityStore) {
    User user = new User(
      v1User.name,
      v1User.displayName,
      v1User.mail,
      v1User.password,
      v1User.type,
      v1User.active);
    userDAO.add(user);

    if (v1User.admin) {
      securityStore.put(new AssignedPermission(v1User.name, "*"));
    }
  }

  private XmlUserV1UpdateStep.V1UserDatabase readV1Database(JAXBContext jaxbContext) throws JAXBException {
    return (XmlUserV1UpdateStep.V1UserDatabase) jaxbContext.createUnmarshaller().unmarshal(determineV1File());
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  private File determineV1File() {
    File configDirectory = new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME);
    for (File file : configDirectory.listFiles()) {
      if (file.getName().equals("users" + StoreConstants.FILE_EXTENSION)) {
        file.renameTo(new File(configDirectory + "/usersV1" + StoreConstants.FILE_EXTENSION));
      }
    }
    return new File(configDirectory, "usersV1" + StoreConstants.FILE_EXTENSION);
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
