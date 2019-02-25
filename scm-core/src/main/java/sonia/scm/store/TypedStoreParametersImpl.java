package sonia.scm.store;

import sonia.scm.repository.Repository;

class TypedStoreParametersImpl<T> implements TypedStoreParameters<T> {
  private Class<T> type;
  private String name;
  private Repository repository;

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
  public Repository getRepository() {
    return repository;
  }

  void setRepository(Repository repository) {
    this.repository = repository;
  }
}
