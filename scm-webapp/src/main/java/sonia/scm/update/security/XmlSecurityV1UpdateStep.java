package sonia.scm.update.security;

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

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static sonia.scm.version.Version.parse;

@Extension
public class XmlSecurityV1UpdateStep implements UpdateStep {

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
    return parse("2.0.0");
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
}
