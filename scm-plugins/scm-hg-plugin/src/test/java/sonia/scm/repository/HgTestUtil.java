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

package sonia.scm.repository;


import org.junit.Assume;
import sonia.scm.SCMContext;
import sonia.scm.TempDirRepositoryLocationResolver;
import sonia.scm.autoconfig.AutoConfiguratorProvider;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.File;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;


public final class HgTestUtil {

  private HgTestUtil() {
  }



  public static void checkForSkip(HgRepositoryHandler handler) {

    // skip tests if hg not in path
    if (!handler.isConfigured()) {
      System.out.println("WARNING could not find hg, skipping test");
      Assume.assumeTrue(false);
    }

    if (Boolean.getBoolean("sonia.scm.test.skip.hg")) {
      System.out.println("WARNING mercurial test are disabled");
      Assume.assumeTrue(false);
    }
  }

  public static HgRepositoryHandler createHandler(File directory) {
    TempSCMContextProvider context = (TempSCMContextProvider) SCMContext.getContext();

    context.setBaseDirectory(directory);

    RepositoryLocationResolver repositoryLocationResolver = new TempDirRepositoryLocationResolver(directory);
    HgRepositoryHandler handler = new HgRepositoryHandler(
      new InMemoryConfigurationStoreFactory(),
      repositoryLocationResolver,
      null,
      null,
      new AutoConfiguratorProvider(new HgVerifier()).get()
    );
    handler.init(context);

    return handler;
  }

  public static HgRepositoryFactory createFactory(HgRepositoryHandler handler, File directory) {
    HgConfigResolver resolver = new HgConfigResolver(handler, repository -> directory);
    return new HgRepositoryFactory(
      resolver, new HookEnvironment(), new EmptyHgEnvironmentBuilder()
    );
  }
}
