package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;

public abstract class SimpleWorkdirFactory<R, W, C> implements WorkdirFactory<R, W, C> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleWorkdirFactory.class);

  private final WorkdirProvider workdirProvider;

  public SimpleWorkdirFactory(WorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
  }

  @Override
  public WorkingCopy<R, W> createWorkingCopy(C context, String initialBranch) {
    try {
      File directory = workdirProvider.createNewWorkdir();
      ParentAndClone<R, W> parentAndClone = cloneRepository(context, directory, initialBranch);
      return new WorkingCopy<>(parentAndClone.getClone(), parentAndClone.getParent(), this::closeWorkdir, this::closeCentral, directory);
    } catch (IOException e) {
      throw new InternalRepositoryException(getScmRepository(context), "could not clone repository in temporary directory", e);
    }
  }

  protected abstract Repository getScmRepository(C context);

  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in close
  protected abstract void closeRepository(R repository) throws Exception;
  protected abstract void closeWorkdirInternal(W workdir) throws Exception;

  protected abstract ParentAndClone<R, W> cloneRepository(C context, File target, String initialBranch) throws IOException;

  private void closeCentral(R repository) {
    try {
      closeRepository(repository);
    } catch (Exception e) {
      logger.warn("could not close temporary repository clone", e);
    }
  }

  private void closeWorkdir(W repository) {
    try {
      closeWorkdirInternal(repository);
    } catch (Exception e) {
      logger.warn("could not close temporary repository clone", e);
    }
  }

  protected static class ParentAndClone<R, W> {
    private final R parent;
    private final W clone;

    public ParentAndClone(R parent, W clone) {
      this.parent = parent;
      this.clone = clone;
    }

    public R getParent() {
      return parent;
    }

    public W getClone() {
      return clone;
    }
  }
}
