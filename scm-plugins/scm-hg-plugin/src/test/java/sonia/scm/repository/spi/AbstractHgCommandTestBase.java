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


import org.junit.After;
import org.junit.Before;

import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.MockUtil;

import java.io.IOException;


public class AbstractHgCommandTestBase extends ZippedRepositoryTestBase
{

   @After
  public void close() {
    if (cmdContext != null)
    {
      cmdContext.close();
    }
  }

  @Before
  public void initHgHandler() throws IOException {
    this.handler = HgTestUtil.createHandler(tempFolder.newFolder());
    HgTestUtil.checkForSkip(handler);

    HgConfigResolver resolver = new HgConfigResolver(handler);

    HgRepositoryFactory factory = HgTestUtil.createFactory(handler, repositoryDirectory);
    cmdContext = new HgCommandContext(resolver, factory, RepositoryTestData.createHeartOfGold());
  }


   @Before
  public void setUp()
  {
    setSubject(MockUtil.createAdminSubject());
  }


  
  @Override
  protected String getType()
  {
    return "hg";
  }

  
  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-hg-spi-test.zip";
  }

  //~--- fields ---------------------------------------------------------------

  protected HgCommandContext cmdContext;

  protected HgRepositoryHandler handler;
}
