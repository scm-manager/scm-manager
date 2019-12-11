package sonia.scm.repository;

import com.google.inject.AbstractModule;
import sonia.scm.lifecycle.modules.CloseableModule;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;

public class ExecutorModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SyncAsyncExecutorProvider.class).to(DefaultSyncAsyncExecutorProvider.class);
  }
}
