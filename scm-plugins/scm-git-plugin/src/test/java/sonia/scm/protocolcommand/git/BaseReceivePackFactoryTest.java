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
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BaseReceivePackFactoryTest {

  @Mock
  private GitRepositoryHandler handler;

  private GitConfig gitConfig;

  @Mock
  private ReceivePackFactory<Object> wrappedReceivePackFactory;

  @Mock
  private GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider;

  private BaseReceivePackFactory<Object> factory;

  private Object request = new Object();

  private Repository repository;

  private GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUpObjectUnderTest() throws Exception {
    this.repository = createRepositoryForTesting();

    gitConfig = new GitConfig();
    when(handler.getConfig()).thenReturn(gitConfig);
    when(handler.getRepositoryId(repository.getConfig())).thenReturn("heart-of-gold");

    when(wrappedReceivePackFactory.create(request, repository)).thenAnswer(invocation -> new ReceivePack(repository));

    when(gitRepositoryConfigStoreProvider.getGitRepositoryConfig("heart-of-gold")).thenReturn(gitRepositoryConfig);

    factory = new BaseReceivePackFactory<Object>(GitTestHelper.createConverterFactory(), handler, null, gitRepositoryConfigStoreProvider) {
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
    gitConfig.setNonFastForwardDisallowed(true);
    ReceivePack receivePack = factory.create(request, repository);
    assertFalse(receivePack.isAllowNonFastForwards());
  }

  @Test
  public void testCreateWithLocalDisabledNonFastForward() throws Exception {
    gitRepositoryConfig.setNonFastForwardDisallowed(true);
    ReceivePack receivePack = factory.create(request, repository);
    assertFalse(receivePack.isAllowNonFastForwards());
  }

  @Test
  public void shouldNotReUseGitReceiveHook() throws Exception {
    ReceivePack receivePack1 = factory.create(request, repository);
    ReceivePack receivePack2 = factory.create(request, repository);

    assertThat(receivePack1.getPostReceiveHook(), not(sameInstance(receivePack2.getPostReceiveHook())));
  }
}
