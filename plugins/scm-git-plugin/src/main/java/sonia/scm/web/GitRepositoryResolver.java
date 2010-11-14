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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.http.server.resolver.RepositoryResolver;
import org.eclipse.jgit.http.server.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.http.server.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitConfig;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryResolver implements RepositoryResolver
{

  /** the logger for GitRepositoryResolver */
  private static final Logger logger =
    LoggerFactory.getLogger(GitRepositoryResolver.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  public GitRepositoryResolver(GitConfig config)
  {
    this.config = config;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private static boolean isUnreasonableName(final String name)
  {
    if (name.length() == 0)
    {
      return true;    // no empty paths
    }

    if (name.indexOf('\\') >= 0)
    {
      return true;    // no windows/dos style paths
    }

    if (new File(name).isAbsolute())
    {
      return true;    // no absolute paths
    }

    if (name.startsWith("../"))
    {
      return true;    // no "l../etc/passwd"
    }

    if (name.contains("/../"))
    {
      return true;    // no "foo/../etc/passwd"
    }

    if (name.contains("/./"))
    {
      return true;    // "foo/./foo" is insane to ask
    }

    if (name.contains("//"))
    {
      return true;    // double slashes is sloppy, don't use it
    }

    return false;    // is a reasonable name
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param repositoryName
   *
   * @return
   *
   * @throws RepositoryNotFoundException
   * @throws ServiceNotAuthorizedException
   * @throws ServiceNotEnabledException
   */
  @Override
  public Repository open(HttpServletRequest request, String repositoryName)
          throws RepositoryNotFoundException, ServiceNotAuthorizedException,
                 ServiceNotEnabledException
  {
    Repository repository = null;

    if (isUnreasonableName(repositoryName))
    {
      throw new RepositoryNotFoundException(repositoryName);
    }

    try
    {
      File gitdir = new File(config.getRepositoryDirectory(), repositoryName);

      if (!gitdir.exists())
      {
        throw new RepositoryNotFoundException(repositoryName);
      }

      repository = RepositoryCache.open(FileKey.lenient(gitdir, FS.DETECTED),
                                        true);
    }
    catch (RuntimeException e)
    {
      repository.close();

      throw new RepositoryNotFoundException(repositoryName, e);
    }
    catch (IOException e)
    {
      repository.close();

      throw new RepositoryNotFoundException(repositoryName, e);
    }

    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitConfig config;
}
