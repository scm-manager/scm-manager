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

package sonia.scm.repository.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsClearCommandTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;

  @InjectMocks
  private RepositoryPermissionsClearCommand command;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Nested
  class ForExistingRepository {

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
    }

    @Test
    void shouldClearPermissionsForUser() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("trillian", List.of("read"), false)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("trillian");

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).isEmpty();
        return true;
      }));
    }

    @Test
    void shouldClearPermissionsForGroup() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("hog", "READ", true)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("hog");
      command.setForGroup(true);

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).isEmpty();
        return true;
      }));
    }
  }

  @Test
  void shouldHandleIllegalNamespaceNameParameter() {
    command.setRepositoryNamespaceAndName("illegal name");
    command.setName("trillian");

    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldHandleNotExistingRepository() {
    command.setRepositoryNamespaceAndName("no/repository");
    command.setName("trillian");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }
}
