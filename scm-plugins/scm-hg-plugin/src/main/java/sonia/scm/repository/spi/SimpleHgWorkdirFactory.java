package sonia.scm.repository.spi;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.flags.CloneCommandFlags;
import sonia.scm.repository.util.SimpleWorkdirFactory;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

public class SimpleHgWorkdirFactory extends SimpleWorkdirFactory<Repository, Repository, HgCommandContext> implements HgWorkdirFactory {

  private final Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder;

  @Inject
  public SimpleHgWorkdirFactory(Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder, WorkdirProvider workdirProvider) {
    super(workdirProvider);
    this.hgRepositoryEnvironmentBuilder = hgRepositoryEnvironmentBuilder;
  }
  @Override
  public ParentAndClone<Repository, Repository> cloneRepository(HgCommandContext context, File target, String initialBranch) throws IOException {
    BiConsumer<sonia.scm.repository.Repository, Map<String, String>> repositoryMapBiConsumer =
      (repository, environment) -> hgRepositoryEnvironmentBuilder.get().buildFor(repository, null, environment);
    Repository centralRepository = context.openWithSpecialEnvironment(repositoryMapBiConsumer);
    CloneCommand cloneCommand = CloneCommandFlags.on(centralRepository);
    if (initialBranch != null) {
      cloneCommand.updaterev(initialBranch);
    }
    cloneCommand.execute(target.getAbsolutePath());

    BaseRepository clone = Repository.open(target);

    return new ParentAndClone<>(centralRepository, clone);
  }

  @Override
  protected void closeRepository(Repository repository) {
    repository.close();
  }

  @Override
  protected void closeWorkdirInternal(Repository workdir) throws Exception {
    workdir.close();
  }

  @Override
  protected sonia.scm.repository.Repository getScmRepository(HgCommandContext context) {
    return context.getScmRepository();
  }

  @Override
  public void configure(PullCommand pullCommand) {
    pullCommand.cmdAppend("--config", "hooks.changegroup.scm=python:scmhooks.postHook");
    pullCommand.cmdAppend("--config", "hooks.pretxnchangegroup.scm=python:scmhooks.preHook");
  }
}
