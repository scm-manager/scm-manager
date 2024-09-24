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


import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameNamespaceStrategyTest {

  @Mock
  private Subject subject;

  private final NamespaceStrategy usernameNamespaceStrategy = new UsernameNamespaceStrategy();

  @BeforeEach
  void setupSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void clearThreadContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReturnPrimaryPrincipal() {
    when(subject.getPrincipal()).thenReturn("trillian");

    String namespace = usernameNamespaceStrategy.createNamespace(RepositoryTestData.createHeartOfGold());
    assertThat(namespace).isEqualTo("trillian");
  }

  @Test
  void shouldReplaceSpacesInPrincipalName() {
    when(subject.getPrincipal()).thenReturn("arthur dent");

    String namespace = usernameNamespaceStrategy.createNamespace(RepositoryTestData.createHeartOfGold());
    assertThat(namespace).isEqualTo("arthur_dent");
  }
}
