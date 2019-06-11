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
import sonia.scm.util.ValidationUtil;

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
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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
    List<RepositoryLineEntry> repositoryLineEntries = getRepositoryLineEntries();
    doGet(req, resp, repositoryLineEntries);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp, List<RepositoryLineEntry> repositoryLineEntries) {
    HashMap<String, Object> model = new HashMap<>();

    model.put("contextPath", req.getContextPath());
    model.put("submitUrl", req.getRequestURI());
    model.put("repositories", repositoryLineEntries);
    model.put("strategies", getMigrationStrategies());
    model.put("validationErrorsFound", repositoryLineEntries
      .stream()
      .anyMatch(entry -> entry.isNamespaceInvalid() || entry.isNameInvalid()));

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache template = mf.compile("templates/repository-migration.mustache");
    respondWithTemplate(resp, model, template);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    List<RepositoryLineEntry> repositoryLineEntries = getRepositoryLineEntries();

    boolean validationErrorFound = false;
    for (RepositoryLineEntry repositoryLineEntry : repositoryLineEntries) {
      String id = repositoryLineEntry.getId();
      String namespace = req.getParameter("namespace-" + id);
      String name = req.getParameter("name-" + id);
      repositoryLineEntry.setNamespace(namespace);
      repositoryLineEntry.setName(name);

      if (!ValidationUtil.isRepositoryNameValid(namespace)) {
        repositoryLineEntry.setNamespaceValid(false);
        validationErrorFound = true;
      }
      if (!ValidationUtil.isRepositoryNameValid(name)) {
        repositoryLineEntry.setNameValid(false);
        validationErrorFound = true;
      }
    }

    if (validationErrorFound) {
      doGet(req, resp, repositoryLineEntries);
      return;
    }

    repositoryLineEntries.stream()
      .map(RepositoryLineEntry::getId)
      .forEach(
        id -> {
          String strategy = req.getParameter("strategy-" + id);
          String namespace = req.getParameter("namespace-" + id);
          String name = req.getParameter("name-" + id);
          migrationStrategyDao.set(id, MigrationStrategy.valueOf(strategy), namespace, name);
        }
      );

    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache template = mf.compile("templates/repository-migration-restart.mustache");
    Map<String, Object> model = Collections.singletonMap("contextPath", req.getContextPath());

    respondWithTemplate(resp, model, template);

    resp.setStatus(200);

    ScmEventBus.getInstance().post(new RestartEvent(MigrationWizardServlet.class, "wrote migration data"));
  }

  private List<RepositoryLineEntry> getRepositoryLineEntries() {
    List<XmlRepositoryV1UpdateStep.V1Repository> repositoriesWithoutMigrationStrategies =
      repositoryV1UpdateStep.getRepositoriesWithoutMigrationStrategies();
    return repositoriesWithoutMigrationStrategies.stream()
      .map(RepositoryLineEntry::new)
      .sorted(comparing(RepositoryLineEntry::getPath))
      .collect(Collectors.toList());
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

  private static class RepositoryLineEntry {
    private final String id;
    private final String type;
    private final String path;
    private String namespace;
    private String name;
    private boolean namespaceValid = true;
    private boolean nameValid = true;

    public RepositoryLineEntry(XmlRepositoryV1UpdateStep.V1Repository repository) {
      this.id = repository.getId();
      this.type = repository.getType();
      this.path = repository.getPath();
      this.namespace = repository.getNewNamespace();
      this.name = repository.getNewName();
    }

    public String getId() {
      return id;
    }

    public String getType() {
      return type;
    }

    public String getPath() {
      return path;
    }

    public String getNamespace() {
      return namespace;
    }

    public String getName() {
      return name;
    }

    public void setNamespace(String namespace) {
      this.namespace = namespace;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setNamespaceValid(boolean namespaceValid) {
      this.namespaceValid = namespaceValid;
    }

    public void setNameValid(boolean nameValid) {
      this.nameValid = nameValid;
    }

    public boolean isNamespaceInvalid() {
      return !namespaceValid;
    }

    public boolean isNameInvalid() {
      return !nameValid;
    }
  }
}
