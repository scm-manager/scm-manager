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

package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import jakarta.inject.Provider;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(ShiroExtension.class)
@ExtendWith(MockitoExtension.class)
@SubjectAware(
  value = "trillian"
)
class RepositoryLinkEnricherTest {

  private static final Repository REPOSITORY = RepositoryTestData.create42Puzzle("git");

  private RepositoryLinkEnricher repositoryLinkEnricher;

  @Mock
  private HalAppender appender;

  @BeforeEach
  void initEnricher() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    repositoryLinkEnricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider);
    REPOSITORY.setId("id-1");
  }

  @Test
  void shouldNotAppendLinkIfNotPermitted() {
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    repositoryLinkEnricher.enrich(context, appender);

    verify(appender, never()).appendLink(eq("defaultBranch"), any(String.class));
  }

  @Test
  @SubjectAware(
    permissions = "repository:read:id-1"
  )
  void shouldAppendDefaultBranchLink() {
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    repositoryLinkEnricher.enrich(context, appender);

    verify(appender).appendLink("defaultBranch", "/api/v2/config/git/hitchhiker/42Puzzle/default-branch");
  }

}
