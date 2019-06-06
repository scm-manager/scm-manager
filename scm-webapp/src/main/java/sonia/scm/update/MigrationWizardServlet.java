package sonia.scm.update;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;

@Singleton
class MigrationWizardServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationWizardServlet.class);

  private final XmlRepositoryV1UpdateStep repositoryV1UpdateStep;
  private final MigrationStrategyDao migrationStrategyDao;

  @Inject
  MigrationWizardServlet(XmlRepositoryV1UpdateStep repositoryV1UpdateStep, MigrationStrategyDao migrationStrategyDao) {
    this.repositoryV1UpdateStep = repositoryV1UpdateStep;
    this.migrationStrategyDao = migrationStrategyDao;
  }

  public boolean wizardNecessary() {
    return !repositoryV1UpdateStep.getRepositoriesWithoutMigrationStrategies().isEmpty();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    List<XmlRepositoryV1UpdateStep.V1Repository> repositoriesWithoutMigrationStrategies =
      repositoryV1UpdateStep.getRepositoriesWithoutMigrationStrategies();

    HashMap<String, Object> model = new HashMap<>();

    model.put("contextPath", req.getContextPath());
    model.put("submitUrl", req.getRequestURI());
    model.put("repositories", repositoriesWithoutMigrationStrategies);
    model.put("strategies", getMigrationStrategies());

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache template = mf.compile("templates/repository-migration.mustache");
    respondWithTemplate(resp, model, template);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    resp.setStatus(200);

    req.getParameterMap().forEach(
      (name, strategy) -> migrationStrategyDao.set(name, MigrationStrategy.valueOf(strategy[0]))
    );

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache template = mf.compile("templates/repository-migration-restart.mustache");
    Map<String, Object> model = Collections.singletonMap("contextPath", req.getContextPath());

    respondWithTemplate(resp, model, template);

    ScmEventBus.getInstance().post(new RestartEvent(MigrationWizardServlet.class, "wrote migration data"));
  }

  private MigrationStrategy[] getMigrationStrategies() {
    return MigrationStrategy.values();
  }

  private void respondWithTemplate(HttpServletResponse resp, Map<String, Object> model, Mustache template) {
    PrintWriter writer;
    try {
      writer = resp.getWriter();
    } catch (IOException e) {
      LOG.error("could not create writer for response", e);
      resp.setStatus(500);
      return;
    }
    template.execute(writer, model);
    writer.flush();
    resp.setStatus(200);
  }
}
