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

package sonia.scm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.MockUtil;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.mock;


public abstract class ManagerTestBase<T extends ModelObject>
{

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  protected SCMContextProvider contextProvider;
  protected RepositoryLocationResolver locationResolver;

  protected Manager<T> manager;

  protected File temp ;

  @Before
  public void setUp() throws IOException {
    if (temp == null){
      temp = tempFolder.newFolder();
    }
    contextProvider = MockUtil.getSCMContextProvider(temp);
    InitialRepositoryLocationResolver initialRepositoryLocationResolver = new InitialRepositoryLocationResolver(emptySet());
    RepositoryDAO repoDao = mock(RepositoryDAO.class);
    locationResolver = new TempDirRepositoryLocationResolver(temp);
    manager = createManager();
    manager.init(contextProvider);
  }

  @After
  public void tearDown() throws IOException {
    manager.close();
  }


  protected abstract Manager<T> createManager();

}
