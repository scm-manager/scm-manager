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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SvnLookupCommandTest {

  @Mock
  SvnContext context;

  @Mock
  SVNRepository svnRepository;

  @InjectMocks
  SvnLookupCommand command;

  @Test
  void shouldReturnEmptyOptional() {
    LookupCommandRequest<String> request = new LookupCommandRequest<>();
    request.setType(String.class);
    request.setArgs(new String[]{"propget"});

    Optional<String> result = command.lookup(request);

    assertThat(result).isNotPresent();
  }

  @Test
  void shouldReturnRepositoryUUID() throws SVNException {
    String uuid = "trillian-hitchhiker-42";
    when(context.open()).thenReturn(svnRepository);
    when(svnRepository.getRepositoryUUID(true)).thenReturn(uuid);

    LookupCommandRequest<String> request = new LookupCommandRequest<>();
    request.setType(String.class);
    request.setArgs(new String[]{"propget", "uuid", "/"});

    Optional<String> result = command.lookup(request);

    assertThat(result).isPresent();
    assertThat(result.get())
      .isInstanceOf(String.class)
      .isEqualTo(uuid);
  }
}
