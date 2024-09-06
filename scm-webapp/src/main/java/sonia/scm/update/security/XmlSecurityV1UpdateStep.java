/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.update.security;

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
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static sonia.scm.version.Version.parse;

@Extension
public class XmlSecurityV1UpdateStep implements UpdateStep {

  private static final Pattern v1PermissionPattern = Pattern.compile("^repository:\\*:(READ|WRITE|OWNER)$");

  private static final Logger LOG = LoggerFactory.getLogger(XmlSecurityV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public XmlSecurityV1UpdateStep(SCMContextProvider contextProvider, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.contextProvider = contextProvider;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() throws JAXBException {
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();

    forAllAdmins(user -> createSecurityEntry(user, false, securityStore),
        group -> createSecurityEntry(group, true, securityStore));

    mapV1Permissions(securityStore);
  }

  private void mapV1Permissions(ConfigurationEntryStore<AssignedPermission> securityStore) throws JAXBException {
    Path v1SecurityFile = determineConfigDirectory().resolve("securityV1" + StoreConstants.FILE_EXTENSION);

    if (!v1SecurityFile.toFile().exists()) {
      LOG.info("no v1 file for security found");
      return;
    }

    JAXBContext jaxbContext = JAXBContext.newInstance(XmlSecurityV1UpdateStep.V1Security.class);
    V1Security v1Security = (V1Security) jaxbContext.createUnmarshaller().unmarshal(v1SecurityFile.toFile());

    v1Security.entries.forEach(assignedPermission -> {
      Matcher matcher = v1PermissionPattern.matcher(assignedPermission.value.permission);
      if (matcher.matches()) {
        String newPermission = convertRole(matcher.group(1));
        securityStore.put(new AssignedPermission(
          assignedPermission.value.name,
          Boolean.parseBoolean(assignedPermission.value.groupPermission),
          newPermission
        ));
      }
    });
  }

  private String convertRole(String role) {
    String newPermission;
    switch (role) {
      case "OWNER":
        newPermission = "repository:*";
        break;
      case "WRITE":
        newPermission = "repository:read,pull,push:*";
        break;
      case "READ":
        newPermission = "repository:read,pull:*";
        break;
      default:
        newPermission = "";
    }
    return newPermission;
  }

  private void forAllAdmins(Consumer<String> userConsumer, Consumer<String> groupConsumer) throws JAXBException {
    Path configDirectory = determineConfigDirectory();
    Path existingConfigFile = configDirectory.resolve("config" + StoreConstants.FILE_EXTENSION);
    if (existingConfigFile.toFile().exists()) {
      forAllAdmins(existingConfigFile, userConsumer, groupConsumer);
    }
  }

  private void forAllAdmins(
    Path existingConfigFile, Consumer<String> userConsumer, Consumer<String> groupConsumer
  ) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(XmlSecurityV1UpdateStep.V1Configuration.class);
    V1Configuration v1Configuration = (V1Configuration) jaxbContext.createUnmarshaller().unmarshal(existingConfigFile.toFile());

    ofNullable(v1Configuration.adminUsers).ifPresent(users -> forAll(users, userConsumer));
    ofNullable(v1Configuration.adminGroups).ifPresent(groups -> forAll(groups, groupConsumer));
  }

  private void forAll(String entries, Consumer<String> consumer) {
    Arrays.stream(entries.split(",")).forEach(consumer);
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.security.xml";
  }

  private void createSecurityEntry(String name, boolean group, ConfigurationEntryStore<AssignedPermission> securityStore) {
    LOG.debug("setting admin permissions for {} {}", group? "group": "user", name);
    securityStore.put(new AssignedPermission(name, group, "*"));
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  private Path determineConfigDirectory() {
    return new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME).toPath();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "scm-config")
  private static class V1Configuration {
    @XmlElement(name = "admin-users")
    private String adminUsers;
    @XmlElement(name = "admin-groups")
    private String adminGroups;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "configuration")
  private static class V1Security {
    @XmlElement(name = "entry")
    private List<Entry> entries = new ArrayList<>();
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  private static class Entry {
    @XmlElement(name = "key")
    private String key;
    @XmlElement(name = "value")
    private Value value;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  private static class Value {
    @XmlElement(name = "permission")
    String permission;
    @XmlElement(name = "name")
    String name;
    @XmlElement(name = "group-permission")
    String groupPermission;
  }
}
