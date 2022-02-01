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

package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import javax.inject.Provider;
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
