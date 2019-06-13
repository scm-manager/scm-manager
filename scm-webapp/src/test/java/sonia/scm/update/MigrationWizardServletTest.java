package sonia.scm.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.update.repository.MigrationStrategy;
import sonia.scm.update.repository.MigrationStrategyDao;
import sonia.scm.update.repository.V1Repository;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationWizardServletTest {

  @Mock
  XmlRepositoryV1UpdateStep updateStep;
  @Mock
  MigrationStrategyDao migrationStrategyDao;

  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;

  String renderedTemplateName;
  Map<String, Object> renderedModel;

  MigrationWizardServlet servlet;

  @BeforeEach
  void initServlet() {
    servlet = new MigrationWizardServlet(updateStep, migrationStrategyDao) {
      @Override
      void respondWithTemplate(HttpServletResponse resp, Map<String, Object> model, String templateName) {
        renderedTemplateName = templateName;
        renderedModel = model;
      }
    };
  }

  @Test
  void shouldUseRepositoryTypeAsNamespaceForNamesWithSingleElement() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "simple"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("namespace")
      .contains("git");
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("name")
      .contains("simple");
  }

  @Test
  void shouldUseDirectoriesForNamespaceAndNameForNamesWithTwoElements() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "two/dirs"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("namespace")
      .contains("two");
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("name")
      .contains("dirs");
  }

  @Test
  void shouldUseDirectoriesForNamespaceAndConcatenatedNameForNamesWithMoreThanTwoElements() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "more/than/two/dirs"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("namespace")
      .contains("more");
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("name")
      .contains("than_two_dirs");
  }

  @Test
  void shouldUseTypeAndNameAsPath() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("path")
      .contains("git/name");
  }

  @Test
  void shouldKeepId() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("id")
      .contains("id");
  }

  @Test
  void shouldNotBeInvalidAtFirstRequest() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("validationErrorsFound")).isEqualTo(false);
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("namespaceInvalid")
      .contains(false);
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("nameInvalid")
      .contains(false);
  }

  @Test
  void shouldValidateNamespaceAndNameOnPost() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );
    doReturn("invalid namespace").when(request).getParameter("namespace-id");
    doReturn("invalid name").when(request).getParameter("name-id");
    doReturn("COPY").when(request).getParameter("strategy-id");

    servlet.doPost(request, response);

    assertThat(renderedModel.get("validationErrorsFound")).isEqualTo(true);
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("namespaceInvalid")
      .contains(true);
    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("nameInvalid")
      .contains(true);
  }

  @Test
  void shouldKeepSelectedMigrationStrategy() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    doReturn("we need an").when(request).getParameter("namespace-id");
    doReturn("error for this test").when(request).getParameter("name-id");
    doReturn("INLINE").when(request).getParameter("strategy-id");

    servlet.doPost(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("selectedStrategy")
      .contains(MigrationStrategy.INLINE);
  }

  @Test
  void shouldUseCopyWithoutMigrationStrategy() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    doReturn("we need an").when(request).getParameter("namespace-id");
    doReturn("error for this test").when(request).getParameter("name-id");
    doReturn("").when(request).getParameter("strategy-id");

    servlet.doPost(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("selectedStrategy")
      .contains(MigrationStrategy.COPY);
  }

  @Test
  void shouldStoreValidMigration() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );
    doReturn("namespace").when(request).getParameter("namespace-id");
    doReturn("name").when(request).getParameter("name-id");
    doReturn("COPY").when(request).getParameter("strategy-id");

    servlet.doPost(request, response);

    verify(migrationStrategyDao).set("id", MigrationStrategy.COPY, "namespace", "name");
  }
}
