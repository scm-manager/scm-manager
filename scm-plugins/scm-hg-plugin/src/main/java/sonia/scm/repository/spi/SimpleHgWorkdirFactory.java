package sonia.scm.repository.spi;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;
import sonia.scm.repository.util.SimpleWorkdirFactory;

import java.io.File;
import java.io.IOException;

public class SimpleHgWorkdirFactory extends SimpleWorkdirFactory<RepositoryCloseableWrapper, HgCommandContext> implements HgWorkdirFactory {
  public SimpleHgWorkdirFactory() {
    super(null, new HgCloneProvider());
  }

  public SimpleHgWorkdirFactory(File poolDirectory) {
    super(poolDirectory, null, new HgCloneProvider());
  }

  private static class HgCloneProvider implements CloneProvider<RepositoryCloseableWrapper, HgCommandContext> {

    @Override
    public RepositoryCloseableWrapper cloneRepository(HgCommandContext context, File target) throws IOException {
      String execute = CloneCommand.on(context.open()).execute(target.getAbsolutePath());
      return new RepositoryCloseableWrapper(Repository.open(target));
    }
  }
}

class RepositoryCloseableWrapper implements AutoCloseable {
  private final Repository delegate;

  RepositoryCloseableWrapper(Repository delegate) {
    this.delegate = delegate;
  }

  Repository get() {
    return delegate;
  }

  @Override
  public void close() {
  }
}
