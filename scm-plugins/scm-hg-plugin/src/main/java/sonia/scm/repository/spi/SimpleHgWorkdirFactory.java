package sonia.scm.repository.spi;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;
import com.aragost.javahg.commands.PullCommand;
import sonia.scm.repository.util.SimpleWorkdirFactory;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleHgWorkdirFactory extends SimpleWorkdirFactory<RepositoryCloseableWrapper, HgCommandContext> implements HgWorkdirFactory {

  @Inject
  public SimpleHgWorkdirFactory(Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder) {
    this(hgRepositoryEnvironmentBuilder, new HookConfigurer());
  }

  SimpleHgWorkdirFactory(Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder, Consumer<PullCommand> hookConfigurer) {
    super(new HgCloneProvider(hgRepositoryEnvironmentBuilder, hookConfigurer));
  }

  private static class HgCloneProvider implements CloneProvider<RepositoryCloseableWrapper, HgCommandContext> {

    private final Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder;
    private final Consumer<PullCommand> hookConfigurer;

    private HgCloneProvider(Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder, Consumer<PullCommand> hookConfigurer) {
      this.hgRepositoryEnvironmentBuilder = hgRepositoryEnvironmentBuilder;
      this.hookConfigurer = hookConfigurer;
    }

    @Override
    public RepositoryCloseableWrapper cloneRepository(HgCommandContext context, File target) throws IOException {
      BiConsumer<sonia.scm.repository.Repository, Map<String, String>> repositoryMapBiConsumer = (repository, environment) -> {
        hgRepositoryEnvironmentBuilder.get().buildFor(repository, null, environment);
      };
      Repository centralRepository = context.openWithSpecialEnvironment(repositoryMapBiConsumer);
      CloneCommand.on(centralRepository).execute(target.getAbsolutePath());
      return new RepositoryCloseableWrapper(Repository.open(target), centralRepository, hookConfigurer);
    }
  }

  @Override
  protected sonia.scm.repository.Repository getRepository(HgCommandContext context) {
    return null;
  }
}

class RepositoryCloseableWrapper implements AutoCloseable {
  private final Repository delegate;
  private final Repository centralRepository;
  private final Consumer<PullCommand> hookConfigurer;

  RepositoryCloseableWrapper(Repository delegate, Repository centralRepository, Consumer<PullCommand> hookConfigurer) {
    this.delegate = delegate;
    this.centralRepository = centralRepository;
    this.hookConfigurer = hookConfigurer;
  }

  Repository get() {
    return delegate;
  }

  @Override
  public void close() {
    try {
      PullCommand pullCommand = PullCommand.on(centralRepository);
      hookConfigurer.accept(pullCommand);
      pullCommand.execute(delegate.getDirectory().getAbsolutePath());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    centralRepository.close();
  }
}

class HookConfigurer implements Consumer<PullCommand> {
  @Override
  public void accept(PullCommand pullCommand) {
    pullCommand.cmdAppend("--config", "hooks.changegroup.scm=python:scmhooks.postHook");
    pullCommand.cmdAppend("--config", "hooks.pretxnchangegroup.scm=python:scmhooks.preHook");
  }
}
