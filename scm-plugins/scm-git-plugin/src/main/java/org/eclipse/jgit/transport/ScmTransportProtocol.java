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



package org.eclipse.jgit.transport;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.GitReceiveHook;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmTransportProtocol extends TransportProtocol
{

  /** Field description */
  private static final String NAME = "scm";

  /** Field description */
  private static final Set<String> SCHEMES = ImmutableSet.of("scm");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ScmTransportProtocol() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param hookEventFacadeProvider
   *
   * @param repositoryHandlerProvider
   */
  @Inject
  public ScmTransportProtocol(
    Provider<HookEventFacade> hookEventFacadeProvider,
    Provider<GitRepositoryHandler> repositoryHandlerProvider)
  {
    this.hookEventFacadeProvider = hookEventFacadeProvider;
    this.repositoryHandlerProvider = repositoryHandlerProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uri
   * @param local
   * @param remoteName
   *
   * @return
   */
  @Override
  public boolean canHandle(URIish uri, Repository local, String remoteName)
  {
    if ((uri.getPath() == null) || (uri.getPort() > 0)
      || (uri.getUser() != null) || (uri.getPass() != null)
      || (uri.getHost() != null)
      || ((uri.getScheme() != null) &&!getSchemes().contains(uri.getScheme())))
    {
      return false;
    }

    return true;
  }

  /**
   * Method description
   *
   *
   * @param uri
   * @param local
   * @param remoteName
   *
   * @return
   *
   * @throws NotSupportedException
   * @throws TransportException
   */
  @Override
  public Transport open(URIish uri, Repository local, String remoteName)
    throws NotSupportedException, TransportException
  {
    File localDirectory = local.getDirectory();
    File path = local.getFS().resolve(localDirectory, uri.getPath());
    File gitDir = RepositoryCache.FileKey.resolve(path, local.getFS());

    if (gitDir == null)
    {
      throw new NoRemoteRepositoryException(uri, JGitText.get().notFound);
    }

    //J-
    return new TransportLocalWithHooks(
      hookEventFacadeProvider.get(),
      repositoryHandlerProvider.get(), 
      local, uri, gitDir
    );
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getName()
  {
    return NAME;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<String> getSchemes()
  {
    return SCHEMES;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/05/19
   * @author         Enter your name here...
   */
  private static class TransportLocalWithHooks extends TransportLocal
  {

    /**
     * Constructs ...
     *
     *
     *
     * @param hookEventFacade
     * @param handler
     * @param local
     * @param uri
     * @param gitDir
     */
    public TransportLocalWithHooks(HookEventFacade hookEventFacade,
      GitRepositoryHandler handler, Repository local, URIish uri, File gitDir)
    {
      super(local, uri, gitDir);
      this.hookEventFacade = hookEventFacade;
      this.handler = handler;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param dst
     *
     * @return
     */
    @Override
    ReceivePack createReceivePack(Repository dst)
    {
      ReceivePack pack = new ReceivePack(dst);

      if ((hookEventFacade != null) && (handler != null))
      {
        GitReceiveHook hook = new GitReceiveHook(hookEventFacade, handler);

        pack.setPreReceiveHook(hook);
        pack.setPostReceiveHook(hook);
      }

      return pack;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private GitRepositoryHandler handler;

    /** Field description */
    private HookEventFacade hookEventFacade;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<HookEventFacade> hookEventFacadeProvider;

  /** Field description */
  private Provider<GitRepositoryHandler> repositoryHandlerProvider;
}
