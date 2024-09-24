/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.work;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.Metrics;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;

import java.io.File;

/**
 * This class is responsible to govern the creation, the reuse and the destruction
 * of working copies. For every repository type there has to be an implementation
 * of this class to provide the repository specific logic to create, initialize,
 * reclaim and clean up working copies. To do this, the following methods have to be
 * implemented:
 *
 * <dl>
 *   <dt>{@link SimpleWorkingCopyFactory#initialize(C, File, String)}</dt>
 *   <dd>Creates a new clone of the repository for the given context in the given
 *     directory with the given branch checked out (if branches are supported).</dd>
 *   <dt>{@link SimpleWorkingCopyFactory#reclaim(C, File, String)}</dt>
 *   <dd>Reclaim the working directory with a already checked out clone of the
 *     repository given in the context, so that the directory is not modified in
 *     respect to the repository and the given branch is checked out (if branches
 *     are supported).</dd>
 *   <dt>{@link SimpleWorkingCopyFactory#closeWorkingCopy(W)}</dt>
 *   <dd>Closes resources allocated for the working copy, so that the directory can
 *     be put to the cache. Will be called at the end of the operation.</dd>
 *   <dt>{@link SimpleWorkingCopyFactory#closeRepository(R)}</dt>
 *   <dd>Closes resources allocated for the central repository.</dd>
 * </dl>
 * <br>
 * The general process looks like this:
 * <br>
 * <img src="doc-files/SimpleWorkingCopyFactory_Sequence.png"/>
 * @param <R> Type of central repository location
 * @param <W> Type of working copy for repository
 * @param <C> Type of repository context
 */
/*
http://www.plantuml.com/plantuml/uml/jLF1JiCm3BtdAtAkr7r0aQf9XN42JGE9SvHumyA9goHbAr-FnZgKDbCPGXnZl_ViFDlB49MFdINnm0QtVSFMAcVA-WbjIt2FyONz6xfTmss_KZgoxsKbjGSL8Kc96NnPooJOi8jmY4LHdKJccKbipKpL3bAOs7dkMldiUnbPUj2aq8e9fwppwjKPgoYUUJ9qMaC8suv4pXYf5CL5H2sdxQQz0WNuhhLLIE5cy54ws5yKF6I2cnD_fP30t1qqj17PNVwoGR_s_8u6_E3r8-o7X9W0odfgzLKseiE8Yl03_iSoP_8svbQpabVlP3rQ-35niLXCxo59LuQFhvzGcZYCR9azgW4-WxY2diJ_gBI1bWCUtx-xJtqQR7FKo6UNmvL-XLlqy2Kdbk1CP-aJ
@startuml
ModifyCommand->SimpleGitWorkingCopyFactory : createWorkingCopy
SimpleGitWorkingCopyFactory-> WorkingCopyContext**:create
SimpleGitWorkingCopyFactory->WorkingCopyPool:getWorkingCopy
group Try to reclaim
WorkingCopyPool->WorkingCopyContext:reclaim
alt reclaim successful
WorkingCopyContext->WorkingCopy**
WorkingCopyContext->> WorkingCopyPool:WorkingCopy
else reclaim fails; create new
WorkingCopyContext->x WorkingCopyPool:ReclaimFailedException
WorkingCopyPool->WorkdirProvider:createNewWorkdir
WorkdirProvider->>WorkingCopyPool
WorkingCopyPool->WorkingCopyContext:initialize
WorkingCopyContext->WorkingCopy**
WorkingCopyContext->> WorkingCopyPool:WorkingCopy
end
WorkingCopyPool->>SimpleGitWorkingCopyFactory: WorkingCopy
SimpleGitWorkingCopyFactory->>ModifyCommand: WorkingCopy
...
ModifyCommand->WorkingCopy:doWork
...
ModifyCommand->WorkingCopy:close
WorkingCopy->SimpleGitWorkingCopyFactory:close
SimpleGitWorkingCopyFactory->SimpleGitWorkingCopyFactory:closeWorkingCopy
SimpleGitWorkingCopyFactory->SimpleGitWorkingCopyFactory:closeRepository
SimpleGitWorkingCopyFactory->WorkingCopyPool:contextClosed
WorkingCopyPool->WorkingCopyPool:cacheDirectory
@enduml
*/
public abstract class SimpleWorkingCopyFactory<R, W, C extends RepositoryProvider> implements WorkingCopyFactory<R, W, C>, ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleWorkingCopyFactory.class);

  private final WorkingCopyPool workingCopyPool;
  private final MeterRegistry meterRegistry;

  /**
   * Constructs a new {@link SimpleWorkingCopyFactory}
   *
   * @param workingCopyPool pool which provides working copies
   * @deprecated since 2.16.0 use {@link SimpleWorkingCopyFactory(WorkingCopyPool, MeterRegistry)} instead
   */
  @Deprecated
  public SimpleWorkingCopyFactory(WorkingCopyPool workingCopyPool) {
    this(workingCopyPool, new CompositeMeterRegistry());
  }

  /**
   * Constructs a new {@link SimpleWorkingCopyFactory}
   *
   * @param workingCopyPool pool which provides working copies
   * @param meterRegistry registry to collect metrics
   */
  public SimpleWorkingCopyFactory(WorkingCopyPool workingCopyPool, MeterRegistry meterRegistry) {
    this.workingCopyPool = workingCopyPool;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public WorkingCopy<R, W> createWorkingCopy(C repositoryContext, String initialBranch) {
    WorkingCopyContext workingCopyContext = createWorkingCopyContext(repositoryContext, initialBranch);
    return workingCopyPool.getWorkingCopy(workingCopyContext);
  }

  private WorkingCopyContext createWorkingCopyContext(C repositoryContext, String initialBranch) {
    return new WorkingCopyContext(
      initialBranch,
      repositoryContext
    );
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    workingCopyPool.shutdown();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // nothing to do
  }

  protected abstract ParentAndClone<R, W> initialize(C context, File target, String initialBranch);

  protected abstract ParentAndClone<R, W> reclaim(C context, File target, String initialBranch) throws ReclaimFailedException;

  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in closeCentral
  protected abstract void closeRepository(R repository) throws Exception;

  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in closeWorkingCopy
  protected abstract void closeWorkingCopy(W workingCopy) throws Exception;

  public static class ReclaimFailedException extends Exception {
    public ReclaimFailedException(String message) {
      super(message);
    }

    public ReclaimFailedException(Throwable cause) {
      super(cause);
    }

    public ReclaimFailedException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class ParentAndClone<R, W> {
    private final R parent;
    private final W clone;
    private final File directory;

    public ParentAndClone(R parent, W clone, File directory) {
      this.parent = parent;
      this.clone = clone;
      this.directory = directory;
    }

    R getParent() {
      return parent;
    }

    W getClone() {
      return clone;
    }

    File getDirectory() {
      return directory;
    }
  }

  public class WorkingCopyContext {
    private final String requestedBranch;
    private final C repositoryContext;
    private final Timer.Sample sample;

    public WorkingCopyContext(String requestedBranch, C repositoryContext) {
      this.requestedBranch = requestedBranch;
      this.repositoryContext = repositoryContext;
      sample = Timer.start(meterRegistry);
    }

    public Repository getScmRepository() {
      return repositoryContext.get();
    }

    public WorkingCopy<R, W> reclaim(File workdir) throws SimpleWorkingCopyFactory.ReclaimFailedException {
      return createWorkingCopyFromParentAndClone(SimpleWorkingCopyFactory.this.reclaim(repositoryContext, workdir, requestedBranch));
    }

    public WorkingCopy<R, W> initialize(File workdir) {
      return createWorkingCopyFromParentAndClone(SimpleWorkingCopyFactory.this.initialize(repositoryContext, workdir, requestedBranch));
    }

    public WorkingCopy<R, W> createWorkingCopyFromParentAndClone(ParentAndClone<R, W> parentAndClone) {
      return new WorkingCopy<>(parentAndClone.getClone(), parentAndClone.getParent(), () -> close(parentAndClone), parentAndClone.getDirectory());
    }

    private void close(ParentAndClone<R, W> parentAndClone) {
      try {
        closeResources(parentAndClone);
      } finally {
        sample.stop(Metrics.workingCopyTimer(meterRegistry, repositoryContext.get().getType()));
      }
    }

    private void closeResources(ParentAndClone<R, W> parentAndClone) {
      try {
        closeWorkingCopy(parentAndClone.getClone());
      } catch (Exception e) {
        LOG.warn("could not close clone for {} in directory {}", getScmRepository(), parentAndClone.getDirectory(), e);
      }
      try {
        closeRepository(parentAndClone.getParent());
      } catch (Exception e) {
        LOG.warn("could not close central repository for {}", getScmRepository(), e);
      }
      try {
        workingCopyPool.contextClosed(this, parentAndClone.getDirectory());
      } catch (Exception e) {
        LOG.warn("could not close context for {} with directory {}", getScmRepository(), parentAndClone.getDirectory(), e);
      }
    }
  }
}
