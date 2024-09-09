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
