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

package sonia.scm.repository;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nonnull;
import org.javahg.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.hooks.HookEnvironment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgRepositoryFactoryTest {

  private HgRepositoryHandler handler;

  @Mock
  private HookEnvironment hookEnvironment;

  @Mock
  private HgEnvironmentBuilder environmentBuilder;

  private HgRepositoryFactory factory;

  private sonia.scm.repository.Repository heartOfGold;
  private Repository repository;

  @BeforeEach
  void setUpFactory(@TempDir Path directory) {
    handler = HgTestUtil.createHandler(directory.toFile());
    assumeTrue(handler.isConfigured());

    HgConfigResolver resolver = new HgConfigResolver(handler);
    factory = new HgRepositoryFactory(resolver, hookEnvironment, environmentBuilder);
    heartOfGold = createRepository();
  }

  @AfterEach
  void tearDown() {
    if (repository != null) {
      repository.close();
    }
  }

  @Test
  void shouldOpenRepositoryForRead() {
    repository = factory.openForRead(heartOfGold);

    assertThat(repository).isNotNull();
    verify(environmentBuilder).read(heartOfGold);
  }

  @Test
  void shouldOpenRepositoryForWrite() {
    repository = factory.openForWrite(heartOfGold);

    assertThat(repository).isNotNull();
    verify(environmentBuilder).write(heartOfGold);
  }

  @Test
  void shouldFallbackToUTF8OnUnknownEncoding() {
    handler.getConfig().setEncoding("unknown");

    repository = factory.openForRead(heartOfGold);

    assertThat(repository.getBaseRepository().getConfiguration().getEncoding()).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  void shouldSetPendingChangesetState() {
    when(hookEnvironment.isPending()).thenReturn(true);

    repository = factory.openForRead(heartOfGold);

    assertThat(repository.getBaseRepository().getConfiguration().isEnablePendingChangesets())
      .isTrue();
  }

  @Test
  void shouldPassEnvironment() {
    when(environmentBuilder.read(heartOfGold)).thenReturn(ImmutableMap.of("spaceship", "heartOfGold"));

    repository = factory.openForRead(heartOfGold);

    assertThat(repository.getBaseRepository().getConfiguration().getEnvironment())
      .containsEntry("spaceship", "heartOfGold");
  }

  @Nonnull
  private sonia.scm.repository.Repository createRepository() {
    sonia.scm.repository.Repository heartOfGold = RepositoryTestData.createHeartOfGold("hg");
    heartOfGold.setId("42");

    handler.create(heartOfGold);
    return heartOfGold;
  }

}
