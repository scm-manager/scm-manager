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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.MockUtil;

public class AbstractHgCommandTestBase extends ZippedRepositoryTestBase {

  protected HgCommandContext cmdContext;
  protected HgRepositoryHandler handler;

  @AfterEach
  public void close() {
    if (cmdContext != null) {
      cmdContext.close();
    }
  }

  @BeforeEach
  public void initHgHandler() {
    this.handler = HgTestUtil.createHandler(secondTempDir);
    HgTestUtil.checkForSkip(handler);

    HgConfigResolver resolver = new HgConfigResolver(handler);

    HgRepositoryFactory factory = HgTestUtil.createFactory(handler, repositoryDirectory);
    cmdContext = new HgCommandContext(resolver, factory, RepositoryTestData.createHeartOfGold());
  }

  @BeforeEach
  public void setUp() {
    setSubject(MockUtil.createAdminSubject());
  }

  @Override
  protected String getType() {
    return "hg";
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-spi-test.zip";
  }
}
