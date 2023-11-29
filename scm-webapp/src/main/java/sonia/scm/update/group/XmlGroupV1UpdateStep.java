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
    
package sonia.scm.update.group;

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
import sonia.scm.group.Group;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.update.V1Properties;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.update.V1PropertyReader.GROUP_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class XmlGroupV1UpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(XmlGroupV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlGroupDAO groupDAO;
  private final ConfigurationEntryStore<V1Properties> propertyStore;

  @Inject
  public XmlGroupV1UpdateStep(
    SCMContextProvider contextProvider,
    XmlGroupDAO groupDAO,
    ConfigurationEntryStoreFactory configurationEntryStoreFactory
  ) {
    this.contextProvider = contextProvider;
    this.groupDAO = groupDAO;
    this.propertyStore = configurationEntryStoreFactory
      .withType(V1Properties.class)
      .withName(GROUP_PROPERTY_READER.getStoreName())
      .build();
  }

  @Override
  public void doUpdate() throws JAXBException {
    Optional<Path> v1GroupsFile = determineV1File();
    if (!v1GroupsFile.isPresent()) {
      LOG.info("no v1 file for groups found");
      return;
    }
    XmlGroupV1UpdateStep.V1GroupDatabase v1Database = readV1Database(v1GroupsFile.get());
    if (v1Database.groupList != null && v1Database.groupList.groups != null) {
      v1Database.groupList.groups.forEach(this::update);
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.group.xml";
  }

  private void update(V1Group v1Group) {
    LOG.debug("updating group {}", v1Group.name);
    Group group = new Group(
      v1Group.type,
      v1Group.name,
      v1Group.members);
    group.setDescription(v1Group.description);
    group.setCreationDate(v1Group.creationDate);
    group.setLastModified(v1Group.lastModified);
    groupDAO.add(group);

    propertyStore.put(v1Group.name, v1Group.properties);
  }

  private XmlGroupV1UpdateStep.V1GroupDatabase readV1Database(Path v1GroupsFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlGroupV1UpdateStep.V1GroupDatabase.class);
    return (XmlGroupV1UpdateStep.V1GroupDatabase) jaxbContext.createUnmarshaller().unmarshal(v1GroupsFile.toFile());
  }

  private Optional<Path> determineV1File() {
    Path existingGroupsFile = resolveConfigFile("groups");
    Path groupsV1File = resolveConfigFile("groupsV1");
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

  private Path resolveConfigFile(String name) {
    return contextProvider
      .resolve(
        Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME).resolve(name + StoreConstants.FILE_EXTENSION)
      );
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "group")
  private static class V1Group {
    private V1Properties properties;
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
