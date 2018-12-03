package sonia.scm.store;

import sonia.scm.repository.Repository;

/**
 * The fields of the {@link StoreParameters} are used from the {@link StoreFactory} to create a store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public class StoreParameters {

  private Class type;
  private String name;
  private Repository repository;

  public Class getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Repository getRepository() {
    return repository;
  }

  public static WithType forType(Class type){
    return new StoreParameters().new WithType(type);
  }

  public class WithType {

    private WithType(Class type) {
      StoreParameters.this.type = type;
    }

    public WithTypeAndName withName(String name){
      return new WithTypeAndName(name);
    }

  }
  public class WithTypeAndName {

    private WithTypeAndName(String name) {
      StoreParameters.this.name = name;
    }

    public WithTypeNameAndRepository forRepository(Repository repository){
      return new WithTypeNameAndRepository(repository);
    }
    public StoreParameters build(){
      return StoreParameters.this;
    }
  }

  public class WithTypeNameAndRepository {

    private WithTypeNameAndRepository(Repository repository) {
      StoreParameters.this.repository = repository;
    }
    public StoreParameters build(){
      return StoreParameters.this;
    }
  }
}
