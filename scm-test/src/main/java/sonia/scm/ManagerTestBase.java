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
