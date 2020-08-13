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

package sonia.scm.protocolcommand.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BaseReceivePackFactoryTest {

  @Mock
  private GitRepositoryHandler handler;

  private GitConfig config;

  @Mock
  private ReceivePackFactory<Object> wrappedReceivePackFactory;

  private BaseReceivePackFactory<Object> factory;

  private Object request = new Object();

  private Repository repository;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUpObjectUnderTest() throws Exception {
    this.repository = createRepositoryForTesting();

    config = new GitConfig();
    when(handler.getConfig()).thenReturn(config);

    ReceivePack receivePack = new ReceivePack(repository);
    when(wrappedReceivePackFactory.create(request, repository)).thenReturn(receivePack);

    factory = new BaseReceivePackFactory<Object>(GitTestHelper.createConverterFactory(), handler, null) {
      @Override
      protected ReceivePack createBasicReceivePack(Object request, Repository repository) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        return wrappedReceivePackFactory.create(request, repository);
      }
    };
  }

  private Repository createRepositoryForTesting() throws GitAPIException, IOException {
    File directory = temporaryFolder.newFolder();
    return Git.init().setDirectory(directory).call().getRepository();
  }

  @Test
  public void testCreate() throws Exception {
    ReceivePack receivePack = factory.create(request, repository);
    assertThat(receivePack.getPackParserListener(), instanceOf(CollectingPackParserListener.class));
    assertThat(receivePack.getPreReceiveHook(), instanceOf(GitReceiveHook.class));
    assertThat(receivePack.getPostReceiveHook(), instanceOf(GitReceiveHook.class));
    assertTrue(receivePack.isAllowNonFastForwards());
    verify(wrappedReceivePackFactory).create(request, repository);
  }

  @Test
  public void testCreateWithDisabledNonFastForward() throws Exception {
    config.setNonFastForwardDisallowed(true);
    ReceivePack receivePack = factory.create(request, repository);
    assertFalse(receivePack.isAllowNonFastForwards());
  }

}
