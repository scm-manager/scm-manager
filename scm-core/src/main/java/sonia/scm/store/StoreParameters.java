package sonia.scm.store;

import sonia.scm.repository.Repository;

/**
 * The fields of the {@link StoreParameters} are used from the {@link StoreFactory} to create a store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public class StoreParameters<STORE> {

  private Class type;
  private String name;
  private Repository repository;

  private final StoreFactory<STORE> factory;

  StoreParameters(StoreFactory<STORE> factory) {
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
      StoreParameters.this.type = type;
    }

    public StoreParameters<STORE>.WithTypeAndName withName(String name){
      return new WithTypeAndName(name);
    }

  }
  public class WithTypeAndName {

    private WithTypeAndName(String name) {
      StoreParameters.this.name = name;
    }

    public StoreParameters<STORE>.WithTypeNameAndRepository forRepository(Repository repository){
      return new WithTypeNameAndRepository(repository);
    }
    public STORE build(){
      return factory.getStore(StoreParameters.this);
    }
  }

  public class WithTypeNameAndRepository {

    private WithTypeNameAndRepository(Repository repository) {
      StoreParameters.this.repository = repository;
    }
    public STORE build(){
      return factory.getStore(StoreParameters.this);
    }
  }
}
