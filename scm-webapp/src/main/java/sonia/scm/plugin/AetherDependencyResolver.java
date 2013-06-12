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


package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import sonia.scm.config.ScmConfiguration;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class AetherDependencyResolver
{

  /**
   * the logger for AetherDependencyResolver
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AetherDependencyResolver.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param system
   * @param localRepository
   * @param remoteRepositories
   */
  public AetherDependencyResolver(ScmConfiguration configuration,
    RepositorySystem system, LocalRepository localRepository,
    List<RemoteRepository> remoteRepositories)
  {
    this.configuration = configuration;
    this.system = system;
    this.localRepository = localRepository;
    this.remoteRepositories = remoteRepositories;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String createClassPath()
  {
    PreorderNodeListGenerator nodeListGenerator =
      new PreorderNodeListGenerator();

    for (DependencyNode node : resolvedNodes)
    {
      node.accept(nodeListGenerator);
    }

    return nodeListGenerator.getClassPath();
  }

  /**
   * Method description
   *
   *
   * @param dependency
   *
   * @throws DependencyCollectionException
   * @throws DependencyResolutionException
   */
  public void resolveLocalDependency(Dependency dependency)
    throws DependencyCollectionException, DependencyResolutionException
  {
    CollectRequest request = new CollectRequest();

    request.setRoot(dependency);
    resolveDependency(request);
  }

  /**
   * Method description
   *
   *
   * @param dependency
   *
   * @throws DependencyCollectionException
   * @throws DependencyResolutionException
   */
  public void resolveRemoteDependency(Dependency dependency)
    throws DependencyCollectionException, DependencyResolutionException
  {
    resolveDependency(new CollectRequest(dependency, remoteRepositories));
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @throws DependencyCollectionException
   * @throws DependencyResolutionException
   */
  private void resolveDependency(CollectRequest request)
    throws DependencyCollectionException, DependencyResolutionException
  {
    DependencyNode node = Aether.resolveDependencies(system, getSession(),
                            request);

    if (logger.isTraceEnabled())
    {
      StringDependencyGraphDumper dumper = new StringDependencyGraphDumper();

      node.accept(dumper);
      logger.trace(dumper.getGraphAsString());
    }

    resolvedNodes.add(node);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private RepositorySystemSession getSession()
  {
    if (session == null)
    {
      session = Aether.createRepositorySystemSession(system, localRepository,
        configuration);
    }

    return session;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private LocalRepository localRepository;

  /** Field description */
  private List<RemoteRepository> remoteRepositories;

  /** Field description */
  private List<DependencyNode> resolvedNodes = Lists.newArrayList();

  /** Field description */
  private RepositorySystemSession session;

  /** Field description */
  private RepositorySystem system;
}
