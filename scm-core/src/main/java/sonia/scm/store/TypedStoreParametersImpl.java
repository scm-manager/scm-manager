package sonia.scm.store;

class TypedStoreParametersImpl<T> implements TypedStoreParameters<T> {
  private Class<T> type;
  private String name;
  private String repositoryId;

  @Override
  public Class<T> getType() {
    return type;
  }

  void setType(Class<T> type) {
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  @Override
  public String getRepositoryId() {
    return repositoryId;
  }

  void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }
}
