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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.spi.ModifyCommandRequest.CreateFileRequest;
import sonia.scm.repository.spi.ModifyCommandRequest.DeleteFileRequest;
import sonia.scm.repository.spi.ModifyCommandRequest.ModifyFileRequest;
import sonia.scm.repository.spi.ModifyCommandRequest.MoveRequest;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyCommandRequestTest {

  @Mock
  private ModifyCommand.Worker worker;

  @Test
  void shouldReplaceBackslashInDelete() throws IOException {
    DeleteFileRequest deleteFileRequest = new DeleteFileRequest("path\\with\\backslash", false);

    deleteFileRequest.execute(worker);

    verify(worker).delete("path/with/backslash", false);
  }

  @Test
  void shouldReplaceBackslashInCreate(@TempDir Path temp) throws IOException {
    CreateFileRequest createFileRequest = new CreateFileRequest("path\\with\\backslash", temp.toFile(), false);

    createFileRequest.execute(worker);

    verify(worker).create("path/with/backslash", temp.toFile(), false);
  }

  @Test
  void shouldReplaceBackslashInModify(@TempDir Path temp) throws IOException {
    ModifyFileRequest modifyFileRequest = new ModifyFileRequest("path\\with\\backslash", temp.toFile());

    modifyFileRequest.execute(worker);

    verify(worker).modify("path/with/backslash", temp.toFile());
  }

  @Test
  void shouldReplaceBackslashInMove() throws IOException {
    MoveRequest moveRequest = new MoveRequest("from\\path", "path\\with\\backslash");

    moveRequest.execute(worker);

    verify(worker).move("from/path", "path/with/backslash");
  }
}
