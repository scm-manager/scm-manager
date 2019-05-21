package sonia.scm.repository.update;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sonia.scm.version.Version.parse;

@Extension
public class XmlRepositoryV1UpdateStep implements UpdateStep {

  private final SCMContextProvider contextProvider;
  private final XmlRepositoryDAO dao;

  @Inject
  public XmlRepositoryV1UpdateStep(SCMContextProvider contextProvider, XmlRepositoryDAO dao) {
    this.contextProvider = contextProvider;
    this.dao = dao;
  }

  @Override
  public Version getTargetVersion() {
    return parse("0.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.repository.xml";
  }

  @Override
  public void doUpdate() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryDatabase.class);
    V1RepositoryDatabase v1Database = readV1Database(jaxbContext);
    v1Database.repositoryList.repositories.forEach(this::update);
  }

  private void update(V1Repository v1Repository) {
    Repository repository = new Repository(
      v1Repository.id,
      v1Repository.type,
      getNamespace(v1Repository),
      getName(v1Repository),
      v1Repository.contact,
      v1Repository.description,
      createPermissions(v1Repository));
    dao.add(repository);
  }

  private RepositoryPermission[] createPermissions(V1Repository v1Repository) {
    if (v1Repository.permissions == null) {
      return new RepositoryPermission[0];
    }
    return v1Repository.permissions
      .stream()
      .map(this::createPermission)
      .toArray(RepositoryPermission[]::new);
  }

  private RepositoryPermission createPermission(V1Permission v1Permission) {
    return new RepositoryPermission(v1Permission.name, v1Permission.type, v1Permission.groupPermission);
  }

  private String getNamespace(V1Repository v1Repository) {
    String[] nameParts = getNameParts(v1Repository.name);
    return nameParts.length > 1? nameParts[0]: v1Repository.type;
  }

  private String getName(V1Repository v1Repository) {
    String[] nameParts = getNameParts(v1Repository.name);
    return nameParts.length == 1? nameParts[0]: concatPathElements(nameParts);
  }

  private String concatPathElements(String[] nameParts) {
    return Arrays.stream(nameParts).skip(1).collect(Collectors.joining("_"));
  }

  private String[] getNameParts(String v1Name) {
    return v1Name.split("/");
  }

  private V1RepositoryDatabase readV1Database(JAXBContext jaxbContext) throws JAXBException {
    return (V1RepositoryDatabase) jaxbContext.createUnmarshaller().unmarshal(determineV1File());
  }

  private File determineV1File() {
    File configDirectory = new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME);
    return new File(configDirectory, "repositories" + StoreConstants.FILE_EXTENSION);
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "permissions")
  public static class V1Permission {
    private boolean groupPermission;
    private String name;
    private String type;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "repositories")
  public static class V1Repository {
    private Map<String, String> properties;
    private String contact;
    private long creationDate;
    private Long lastModified;
    private String description;
    private String id;
    private String name;
    private boolean isPublic;
    private boolean archived;
    private String type;
    private List<V1Permission> permissions;

    @Override
    public String toString() {
      return "V1Repository{" +
        "properties=" + properties +
        ", contact='" + contact + '\'' +
        ", creationDate=" + creationDate +
        ", lastModified=" + lastModified +
        ", description='" + description + '\'' +
        ", id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", isPublic=" + isPublic +
        ", archived=" + archived +
        ", type='" + type + '\'' +
        '}';
    }
  }

  public static class RepositoryList {
    @XmlElement(name = "repository")
    private List<V1Repository> repositories;
  }

  @XmlRootElement(name = "repository-db")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class V1RepositoryDatabase {
    private long creationTime;
    private Long lastModified;
    @XmlElement(name = "repositories")
    private RepositoryList repositoryList;
  }
}
