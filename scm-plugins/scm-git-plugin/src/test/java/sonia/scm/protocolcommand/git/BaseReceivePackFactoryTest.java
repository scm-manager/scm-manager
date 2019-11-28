/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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

    factory = new BaseReceivePackFactory<Object>(handler, null) {
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
