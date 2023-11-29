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

package sonia.scm.update.repository;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.update.UpdateStepTestUtil;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicFlagUpdateStepTest {

  @Mock
  XmlUserDAO userDAO;
  @Mock
  XmlRepositoryDAO repositoryDAO;
  @Captor
  ArgumentCaptor<Repository> repositoryCaptor;

  private UpdateStepTestUtil testUtil;
  private PublicFlagUpdateStep updateStep;
  private Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void mockScmHome(@TempDir Path tempDir) throws IOException {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new PublicFlagUpdateStep(testUtil.getContextProvider(), userDAO, repositoryDAO);

    //prepare backup xml
    V1RepositoryFileSystem.createV1Home(tempDir);
    Files.move(tempDir.resolve("config").resolve("repositories.xml"), tempDir.resolve("config").resolve("repositories.xml.v1.backup"));
  }

  @Test
  void shouldNotFailForDeletedRepository() throws JAXBException {
    when(userDAO.getAll()).thenReturn(Collections.emptyList());
    when(userDAO.get("_anonymous")).thenReturn(SCMContext.ANONYMOUS);

    updateStep.doUpdate();

    verify(repositoryDAO, never()).modify(any());
  }

  @Nested
  class WithExistingRepository {

    @BeforeEach
    void mockRepository() {
      when(repositoryDAO.get((String) any())).thenReturn(REPOSITORY);
    }

    @Test
    void shouldDeleteOldAnonymousUserIfExists() throws JAXBException {
      User anonymous = new User("anonymous");
      when(userDAO.getAll()).thenReturn(Collections.singleton(anonymous));
      doReturn(anonymous).when(userDAO).get("anonymous");
      doReturn(SCMContext.ANONYMOUS).when(userDAO).get(SCMContext.USER_ANONYMOUS);

      updateStep.doUpdate();

      verify(userDAO).delete(anonymous);
    }

    @Test
    void shouldNotTryToDeleteOldAnonymousUserIfNotExists() throws JAXBException {
      when(userDAO.getAll()).thenReturn(Collections.emptyList());
      doReturn(SCMContext.ANONYMOUS).when(userDAO).get(SCMContext.USER_ANONYMOUS);

      updateStep.doUpdate();

      verify(userDAO, never()).delete(any());
    }

    @Test
    void shouldCreateNewAnonymousUserIfNotExists() throws JAXBException {
      doReturn(SCMContext.ANONYMOUS).when(userDAO).get(SCMContext.USER_ANONYMOUS);
      when(userDAO.getAll()).thenReturn(Collections.singleton(new User("trillian")));

      updateStep.doUpdate();

      verify(userDAO).add(SCMContext.ANONYMOUS);
    }

    @Test
    void shouldNotCreateNewAnonymousUserIfAlreadyExists() throws JAXBException {
      doReturn(SCMContext.ANONYMOUS).when(userDAO).get(SCMContext.USER_ANONYMOUS);
      when(userDAO.getAll()).thenReturn(Collections.singleton(new User("_anonymous")));

      updateStep.doUpdate();

      verify(userDAO, never()).add(SCMContext.ANONYMOUS);
    }

    @Test
    void shouldMigratePublicFlagToAnonymousRepositoryPermission() throws JAXBException {
      when(userDAO.getAll()).thenReturn(Collections.emptyList());
      when(userDAO.get("_anonymous")).thenReturn(SCMContext.ANONYMOUS);

      updateStep.doUpdate();

      verify(repositoryDAO, times(2)).modify(repositoryCaptor.capture());

      RepositoryPermission migratedRepositoryPermission = repositoryCaptor.getValue().getPermissions().iterator().next();
      assertThat(migratedRepositoryPermission.getName()).isEqualTo(SCMContext.USER_ANONYMOUS);
      assertThat(migratedRepositoryPermission.getRole()).isEqualTo("READ");
      assertThat(migratedRepositoryPermission.isGroupPermission()).isFalse();
    }
  }
}
