/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
