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
    
package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadmeRepositoryContentInitializerTest {

  @Mock
  private RepositoryContentInitializer.InitializerContext context;

  @Mock
  private RepositoryContentInitializer.CreateFile createFile;

  private Repository repository;

  private ReadmeRepositoryContentInitializer initializer = new ReadmeRepositoryContentInitializer();

  @BeforeEach
  void setUpContext() {
    repository = RepositoryTestData.createHeartOfGold("hg");
    when(context.getRepository()).thenReturn(repository);
    when(context.create("README.md")).thenReturn(createFile);
  }

  @Test
  void shouldCreateReadme() throws IOException {
    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold\n\n" + repository.getDescription());
  }

  @Test
  void shouldCreateReadmeWithoutDescription() throws IOException {
    repository.setDescription(null);

    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold");
  }

}
