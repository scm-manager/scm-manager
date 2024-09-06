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

package sonia.scm.update;

import com.google.common.base.Stopwatch;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.update.repository.DefaultMigrationStrategyDAO;
import sonia.scm.update.repository.MigrationStrategy;
import sonia.scm.update.repository.V1Repository;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.update.MigrationWizardServlet.PROPERTY_WAIT_TIME;

@ExtendWith(MockitoExtension.class)
class MigrationWizardServletTest {

  @Mock
  XmlRepositoryV1UpdateStep updateStep;
  @Mock
  DefaultMigrationStrategyDAO migrationStrategyDao;
  @Mock
  Restarter restarter;

  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;

  String renderedTemplateName;
  Map<String, Object> renderedModel;

  MigrationWizardServlet servlet;

  @BeforeEach
  void initServlet() {
    servlet = new MigrationWizardServlet(updateStep, migrationStrategyDao, restarter) {
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
  void shouldKeepOriginalName() {
    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );

    servlet.doGet(request, response);

    assertThat(renderedModel.get("repositories"))
      .asList()
      .extracting("originalName")
      .contains("name");
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

    verify(migrationStrategyDao).set("id", "git", "name", MigrationStrategy.COPY, "namespace", "name");
  }

  @Test
  void shouldRestartAfterValidMigration() throws InterruptedException {

    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );
    doReturn("namespace").when(request).getParameter("namespace-id");
    doReturn("name").when(request).getParameter("name-id");
    doReturn("COPY").when(request).getParameter("strategy-id");

    final CountDownLatch countDownLatch = new CountDownLatch(1);
    when(restarter.isSupported()).thenReturn(true);
    doAnswer(ic -> {
      countDownLatch.countDown();
      return null;
    }).when(restarter).restart(any(), any());

    servlet.doPost(request, response);

    boolean restarted = countDownLatch.await(500L, TimeUnit.MILLISECONDS);

    assertThat(restarted).isTrue();
  }

  @Test
  void shouldRestartAfterValidMigrationWithSystemProperty() throws InterruptedException {
    System.setProperty(PROPERTY_WAIT_TIME, "100");
    Stopwatch stopwatch = Stopwatch.createStarted();

    when(updateStep.getRepositoriesWithoutMigrationStrategies()).thenReturn(
      Collections.singletonList(new V1Repository("id", "git", "name"))
    );
    doReturn("namespace").when(request).getParameter("namespace-id");
    doReturn("name").when(request).getParameter("name-id");
    doReturn("COPY").when(request).getParameter("strategy-id");

    final CountDownLatch countDownLatch = new CountDownLatch(1);
    when(restarter.isSupported()).thenReturn(true);
    doAnswer(ic -> {
      stopwatch.stop();
      countDownLatch.countDown();
      return null;
    }).when(restarter).restart(any(), any());

    servlet.doPost(request, response);

    boolean restarted = countDownLatch.await(750L, TimeUnit.MILLISECONDS);

    assertThat(stopwatch.elapsed()).isGreaterThanOrEqualTo(Duration.ofMillis(100L));
    assertThat(restarted).isTrue();

    System.clearProperty(PROPERTY_WAIT_TIME);
  }
}
