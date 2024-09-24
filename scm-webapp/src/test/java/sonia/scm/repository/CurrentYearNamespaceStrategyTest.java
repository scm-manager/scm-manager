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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentYearNamespaceStrategyTest {

  @Mock
  private Clock clock;
  private NamespaceStrategy namespaceStrategy;

  @BeforeEach
  void setupObjectUnderTest() {
    namespaceStrategy = new CurrentYearNamespaceStrategy(clock);
  }

  @Test
  void shouldReturn1985() {
    LocalDateTime dateTime = LocalDateTime.of(1985, 4, 9, 21, 42);
    when(clock.instant()).thenReturn(dateTime.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    String namespace = namespaceStrategy.createNamespace(RepositoryTestData.createHeartOfGold());
    assertThat(namespace).isEqualTo("1985");
  }

}
