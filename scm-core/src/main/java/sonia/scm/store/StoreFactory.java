package sonia.scm.store;

public interface StoreFactory<STORE>  {

  STORE getStore(final StoreParameters storeParameters);
}
