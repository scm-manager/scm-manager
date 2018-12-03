package sonia.scm.store;

import sonia.scm.repository.Repository;

public interface StoreFactory<STORE>  {

  STORE getStore(final StoreParameters storeParameters);

  default FloatingStoreParameters<STORE>.WithType forType(Class type) {
    return new FloatingStoreParameters<>(this).new WithType(type);
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

  public class WithType {

    WithType(Class type) {
      FloatingStoreParameters.this.type = type;
    }

    public FloatingStoreParameters<STORE>.WithTypeAndName withName(String name){
      return new WithTypeAndName(name);
    }

  }
  public class WithTypeAndName {

    private WithTypeAndName(String name) {
      FloatingStoreParameters.this.name = name;
    }

    public FloatingStoreParameters<STORE>.WithTypeNameAndRepository forRepository(Repository repository){
      return new WithTypeNameAndRepository(repository);
    }
    public STORE build(){
      return factory.getStore(FloatingStoreParameters.this);
    }
  }

  public class WithTypeNameAndRepository {

    private WithTypeNameAndRepository(Repository repository) {
      FloatingStoreParameters.this.repository = repository;
    }
    public STORE build(){
      return factory.getStore(FloatingStoreParameters.this);
    }
  }
}
