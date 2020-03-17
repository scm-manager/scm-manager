/**
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
package sonia.scm.legacy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyRepositoryServiceTest {

  @Mock
  private RepositoryManager repositoryManager;

  private LegacyRepositoryService legacyRepositoryService;
  private final Repository repository = new Repository("abc123", "git", "space", "repo");

  @Before
  public void init() {
    legacyRepositoryService  = new LegacyRepositoryService(repositoryManager);
  }

  @Test
  public void findRepositoryNameSpaceAndNameForRepositoryId() {
    when(repositoryManager.get(any(String.class))).thenReturn(repository);
    NamespaceAndNameDto namespaceAndName = legacyRepositoryService.getNameAndNamespaceForRepositoryId("abc123");
    assertThat(namespaceAndName.getName()).isEqualToIgnoringCase("repo");
    assertThat(namespaceAndName.getNamespace()).isEqualToIgnoringCase("space");
  }

  @Test(expected = NotFoundException.class)
  public void shouldGetNotFoundExceptionIfRepositoryNotExists() throws NotFoundException {
    when(repositoryManager.get(any(String.class))).thenReturn(null);
    legacyRepositoryService.getNameAndNamespaceForRepositoryId("456def");
  }
}
