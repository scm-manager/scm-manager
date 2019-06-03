package sonia.scm.group.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.group.Group;
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
public class XmlGroupV1UpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(XmlGroupV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlGroupDAO groupDAO;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public XmlGroupV1UpdateStep(SCMContextProvider contextProvider, XmlGroupDAO groupDAO, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.contextProvider = contextProvider;
    this.groupDAO = groupDAO;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() throws JAXBException {
    Optional<Path> v1GroupsFile = determineV1File();
    if (!v1GroupsFile.isPresent()) {
      LOG.info("no v1 file for groups found");
      return;
    }
    Collection<String> adminGroups = determineAdminGroups();
    LOG.debug("found the following admin groups from global config: {}", adminGroups);
    XmlGroupV1UpdateStep.V1GroupDatabase v1Database = readV1Database(v1GroupsFile.get());
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    v1Database.groupList.groups.forEach(group -> update(group, adminGroups, securityStore));
  }

  private Collection<String> determineAdminGroups() throws JAXBException {
    Path configDirectory = determineConfigDirectory();
    Path existingConfigFile = configDirectory.resolve("config" + StoreConstants.FILE_EXTENSION);
    if (existingConfigFile.toFile().exists()) {
      return extractAdminGroupsFromConfigFile(existingConfigFile);
    } else {
      return emptyList();
    }
  }

  private Collection<String> extractAdminGroupsFromConfigFile(Path existingConfigFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlGroupV1UpdateStep.V1Configuration.class);
    V1Configuration v1Configuration = (V1Configuration) jaxbContext.createUnmarshaller().unmarshal(existingConfigFile.toFile());
    return ofNullable(v1Configuration.adminGroups)
      .map(groupList -> groupList.split(","))
      .map(Arrays::asList)
      .orElse(emptyList());
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.group.xml";
  }

  private void update(V1Group v1Group, Collection<String> adminGroups, ConfigurationEntryStore<AssignedPermission> securityStore) {
    LOG.debug("updating group {}", v1Group.name);
    Group group = new Group(
      v1Group.type,
      v1Group.name,
      v1Group.members);
    group.setDescription(v1Group.description);
    group.setCreationDate(v1Group.creationDate);
    group.setLastModified(v1Group.lastModified);
    groupDAO.add(group);

    if (adminGroups.contains(v1Group.name)) {
      LOG.debug("setting admin permissions for group {}", v1Group.name);
      securityStore.put(new AssignedPermission(v1Group.name, true, "*"));
    }
  }

  private XmlGroupV1UpdateStep.V1GroupDatabase readV1Database(Path v1GroupsFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlGroupV1UpdateStep.V1GroupDatabase.class);
    return (XmlGroupV1UpdateStep.V1GroupDatabase) jaxbContext.createUnmarshaller().unmarshal(v1GroupsFile.toFile());
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  private Optional<Path> determineV1File() {
    Path configDirectory = determineConfigDirectory();
    Path existingGroupsFile = configDirectory.resolve("groups" + StoreConstants.FILE_EXTENSION);
    Path groupsV1File = configDirectory.resolve("groupsV1" + StoreConstants.FILE_EXTENSION);
    if (existingGroupsFile.toFile().exists()) {
      try {
        Files.move(existingGroupsFile, groupsV1File);
      } catch (IOException e) {
        throw new UpdateException("could not move old groups file to " + groupsV1File.toAbsolutePath());
      }
      LOG.info("moved old groups file to {}", groupsV1File.toAbsolutePath());
      return of(groupsV1File);
    }
    return empty();
  }

  private Path determineConfigDirectory() {
    return new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME).toPath();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "group")
  private static class V1Group {
    private Map<String, String> properties;
    private long creationDate;
    private String description;
    private Long lastModified;
    private String name;
    private String type;
    @XmlElement(name = "members")
    private List<String> members;

    @Override
    public String toString() {
      return "V1Group{" +
        "properties=" + properties +
        ", creationDate=" + creationDate + '\'' +
        ", description=" + description + '\'' +
        ", lastModified=" + lastModified + '\'' +
        ", name='" + name + '\'' +
        ", type='" + type + '\'' +
        '}';
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "scm-config")
  private static class V1Configuration {
    @XmlElement(name = "admin-groups")
    private String adminGroups;
  }

  private static class GroupList {
    @XmlElement(name = "group")
    private List<V1Group> groups;
  }

  @XmlRootElement(name = "group-db")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class V1GroupDatabase {
    private long creationTime;
    private Long lastModified;
    @XmlElement(name = "groups")
    private XmlGroupV1UpdateStep.GroupList groupList;
  }

}
