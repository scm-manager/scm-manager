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
    
package sonia.scm.update.user;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
import sonia.scm.update.V1Properties;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.update.V1PropertyReader.USER_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class XmlUserV1UpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(XmlUserV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlUserDAO userDAO;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;
  private final ConfigurationEntryStore<V1Properties> propertyStore;

  @Inject
  public XmlUserV1UpdateStep(SCMContextProvider contextProvider, XmlUserDAO userDAO, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.contextProvider = contextProvider;
    this.userDAO = userDAO;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
    this.propertyStore = configurationEntryStoreFactory
      .withType(V1Properties.class)
      .withName(USER_PROPERTY_READER.getStoreName())
      .build();
  }

  @Override
  public void doUpdate() throws JAXBException {
    Optional<Path> v1UsersFile = determineV1File("users");
    determineV1File("security");
    if (!v1UsersFile.isPresent()) {
      LOG.info("no v1 file for users found");
      return;
    }
    XmlUserV1UpdateStep.V1UserDatabase v1Database = readV1Database(v1UsersFile.get());
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    v1Database.userList.users.forEach(user -> update(user, securityStore));
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.user.xml";
  }

  private void update(V1User v1User, ConfigurationEntryStore<AssignedPermission> securityStore) {
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

    if (v1User.admin) {
      LOG.debug("setting admin permissions for user {}", v1User.name);
      securityStore.put(new AssignedPermission(v1User.name, "*"));
    }

    propertyStore.put(v1User.name, v1User.properties);
  }

  private XmlUserV1UpdateStep.V1UserDatabase readV1Database(Path v1UsersFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlUserV1UpdateStep.V1UserDatabase.class);
    return (XmlUserV1UpdateStep.V1UserDatabase) jaxbContext.createUnmarshaller().unmarshal(v1UsersFile.toFile());
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  private Optional<Path> determineV1File(String filename) {
    Path existingFile = resolveConfigFile(filename);
    Path v1File = resolveConfigFile(filename + "V1");
    if (existingFile.toFile().exists()) {
      try {
        Files.move(existingFile, v1File);
      } catch (IOException e) {
        throw new UpdateException("could not move old " + filename + " file to " + v1File.toAbsolutePath());
      }
      LOG.info("moved old " + filename + " file to {}", v1File.toAbsolutePath());
      return of(v1File);
    }
    return empty();
  }

  private Path resolveConfigFile(String name) {
    return contextProvider
      .resolve(
        Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME).resolve(name + StoreConstants.FILE_EXTENSION)
      );
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "user")
  private static class V1User {
    private V1Properties properties;
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
