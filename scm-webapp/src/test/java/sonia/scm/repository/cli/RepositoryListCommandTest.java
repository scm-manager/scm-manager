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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.RepositoryManager;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryListCommandTest {

  @Mock
  private TemplateRenderer templateRenderer;
  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryToRepositoryCommandDtoMapper mapper;

  @InjectMocks
  private RepositoryListCommand command;

  @Test
  void shouldReturnShortTemplate() {
    command.setShortTemplate(true);
    command.run();

    verify(templateRenderer, never()).createTable();
    verify(templateRenderer).renderToStdout(any(), anyMap());
  }

  @Test
  void shouldReturnTableTemplate() {
    Table table = mock(Table.class);
    when(templateRenderer.createTable()).thenReturn(table);

    ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
    doNothing().when(templateRenderer).renderToStdout(templateCaptor.capture(), mapCaptor.capture());
    command.run();

    Map<String, Object> map = mapCaptor.getValue();
    assertThat(map).hasSize(2);
    assertThat(map.get("rows")).isInstanceOf(Table.class);
    assertThat(map.get("repos")).isInstanceOf(List.class);
  }
}
