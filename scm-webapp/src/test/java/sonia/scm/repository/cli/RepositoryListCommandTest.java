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
