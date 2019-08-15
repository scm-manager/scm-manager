package sonia.scm.migration;

public class MigrationInfo {

  private final String id;
  private final String protocol;
  private final String originalRepositoryName;
  private final String namespace;
  private final String name;

  public MigrationInfo(String id, String protocol, String originalRepositoryName, String namespace, String name) {
    this.id = id;
    this.protocol = protocol;
    this.originalRepositoryName = originalRepositoryName;
    this.namespace = namespace;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getOriginalRepositoryName() {
    return originalRepositoryName;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }
}
