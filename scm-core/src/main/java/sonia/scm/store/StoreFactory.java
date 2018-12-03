package sonia.scm.store;

import sonia.scm.repository.Repository;

public interface StoreFactory<STORE>  {

  STORE getStore(final StoreParameters storeParameters);

  default FloatingStoreParameters<STORE>.Builder withName(String name) {
    return new FloatingStoreParameters<>(this).new Builder(name);
  }
}

final class FloatingStoreParameters<STORE> implements StoreParameters {

  private Class type;
  private String name;
  private Repository repository;

  private final StoreFactory<STORE> factory;

  FloatingStoreParameters(StoreFactory<STORE> factory) {
    this.factory = factory;
  }

  public Class getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Repository getRepository() {
    return repository;
  }

  public class Builder {

    Builder(String name) {
      FloatingStoreParameters.this.name = name;
    }

    public FloatingStoreParameters<STORE>.Builder withType(Class type) {
      FloatingStoreParameters.this.type = type;
      return this;
    }

    public FloatingStoreParameters<STORE>.Builder forRepository(Repository repository) {
      FloatingStoreParameters.this.repository = repository;
      return this;
    }

    public STORE build(){
      return factory.getStore(FloatingStoreParameters.this);
    }
  }
}
