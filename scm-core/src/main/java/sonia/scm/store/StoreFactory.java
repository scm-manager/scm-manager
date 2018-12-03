package sonia.scm.store;

public interface StoreFactory<STORE>  {

  STORE getStore(final StoreParameters<STORE> storeParameters);

  default StoreParameters<STORE>.WithType forType(Class type) {
    return new StoreParameters<>(this).new WithType(type);
  }
}
