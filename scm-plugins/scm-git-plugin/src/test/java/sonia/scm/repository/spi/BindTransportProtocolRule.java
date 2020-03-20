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

import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.rules.ExternalResource;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;

import static com.google.inject.util.Providers.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BindTransportProtocolRule extends ExternalResource {

  private ScmTransportProtocol scmTransportProtocol;

  @Override
  protected void before() throws Throwable {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    RepositoryManager repositoryManager = mock(RepositoryManager.class);
    HookEventFacade hookEventFacade = new HookEventFacade(of(repositoryManager), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    scmTransportProtocol = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));

    Transport.register(scmTransportProtocol);

    when(gitRepositoryHandler.getRepositoryId(any())).thenReturn("1");
    when(repositoryManager.get("1")).thenReturn(new sonia.scm.repository.Repository());
  }

  @Override
  protected void after() {
    Transport.unregister(scmTransportProtocol);
  }
}
