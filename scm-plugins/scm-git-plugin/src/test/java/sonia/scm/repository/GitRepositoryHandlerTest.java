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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.schedule.Scheduler;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GitRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase {

  @Mock
  private Scheduler scheduler;

  @Mock
  private ConfigurationStoreFactory factory;

  @Mock
  private GitWorkingCopyFactory gitWorkingCopyFactory;


  @Override
  protected void checkDirectory(File directory) {
    File head = new File(directory, "HEAD");

    assertTrue(head.exists());
    assertTrue(head.isFile());

    File config = new File(directory, "config");

    assertTrue(config.exists());
    assertTrue(config.isFile());

    File refs = new File(directory, "refs");

    assertTrue(refs.exists());
    assertTrue(refs.isDirectory());
  }

  @Before
  public void initFactory() {
    when(factory.withType(any())).thenCallRealMethod();
  }

  @Override
  protected RepositoryHandler createRepositoryHandler(ConfigurationStoreFactory factory,
                                                      RepositoryLocationResolver locationResolver,
                                                      File directory) {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);
    repositoryHandler.init(contextProvider);

    GitConfig config = new GitConfig();

    // TODO fix event bus exception
    repositoryHandler.setConfig(config);

    return repositoryHandler;
  }

  @Test
  public void getDirectory() {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);
    GitConfig config = new GitConfig();
    config.setDisabled(false);
    config.setGcExpression("gc exp");

    repositoryHandler.setConfig(config);

    initRepository();
    File path = repositoryHandler.getDirectory(repository.getId());
    assertEquals(repoPath.toString() + File.separator + RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY, path.getAbsolutePath());
  }

  @Test
  public void shouldSetHeadToDefaultRepository() {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);

    GitConfig config = new GitConfig();
    config.setDefaultBranch("other");

    repositoryHandler.setConfig(config);

    File nativeRepoDirectory = initRepository();

    repositoryHandler.create(repository);

    assertThat(new File(nativeRepoDirectory, "HEAD")).hasContent("ref: refs/heads/other");
  }
}
