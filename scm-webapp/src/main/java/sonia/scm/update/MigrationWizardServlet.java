package sonia.scm.update;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import sonia.scm.boot.RestartEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.update.repository.MigrationStrategy;
import sonia.scm.update.repository.MigrationStrategyDao;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Singleton
class MigrationWizardServlet extends HttpServlet {

  private final XmlRepositoryV1UpdateStep repositoryV1UpdateStep;
  private final MigrationStrategyDao migrationStrategyDao;

  @Inject
  MigrationWizardServlet(XmlRepositoryV1UpdateStep repositoryV1UpdateStep, MigrationStrategyDao migrationStrategyDao) {
    this.repositoryV1UpdateStep = repositoryV1UpdateStep;
    this.migrationStrategyDao = migrationStrategyDao;
  }

  public boolean wizardNecessary() {
    return !repositoryV1UpdateStep.missingMigrationStrategies().isEmpty();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<XmlRepositoryV1UpdateStep.V1Repository> missingMigrationStrategies = repositoryV1UpdateStep.missingMigrationStrategies();

    resp.setStatus(200);

    HashMap<String, Object> model = new HashMap<>();

    model.put("submitUrl", req.getRequestURI());
    model.put("repositories", missingMigrationStrategies);
    model.put("strategies", getMigrationStrategies());

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile("templates/repository-migration.mustache");
    mustache.execute(resp.getWriter(), model).flush();
  }

  private List<String> getMigrationStrategies() {
    return stream(MigrationStrategy.values()).map(Enum::name).collect(toList());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setStatus(200);

    req.getParameterMap().forEach(
      (name, strategy) -> migrationStrategyDao.set(name, MigrationStrategy.valueOf(strategy[0]))
    );

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile("templates/repository-migration-restart.mustache");
    mustache.execute(resp.getWriter(), new Object()).flush();

    ScmEventBus.getInstance().post(new RestartEvent(MigrationWizardServlet.class, "wrote migration data"));
  }
}
