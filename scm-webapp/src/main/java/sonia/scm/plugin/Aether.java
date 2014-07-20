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

import org.apache.maven.repository.internal.MavenRepositorySystemSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.AndDependencyFilter;
import org.sonatype.aether.util.filter.DependencyFilterUtils;
import org.sonatype.aether.util.graph.transformer
  .ChainedDependencyGraphTransformer;
import org.sonatype.aether.util.graph.transformer.ConflictMarker;
import org.sonatype.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.sonatype.aether.util.graph.transformer.JavaEffectiveScopeCalculator;
import org.sonatype.aether.util.graph.transformer
  .NearestVersionConflictResolver;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;

/**
 *
 * @author Sebastian Sdorra
 */
public final class Aether
{

  /** Field description */
  private static final DependencyFilter FILTER =
    new AndDependencyFilter(
      new CoreDependencyFilter(),
      new BlacklistDependencyFilter()
    );

  /**
   * the logger for Aether
   */
  private static final Logger logger = LoggerFactory.getLogger(Aether.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private Aether() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param gav
   *
   * @return
   */
  public static Dependency createDependency(String gav)
  {
    return new Dependency(new DefaultArtifact(gav), JavaScopes.RUNTIME);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static DependencyFilter createDependencyFilter()
  {
    return DependencyFilterUtils.andFilter(
      DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME), FILTER);
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param pluginRepository
   *
   * @return
   */
  public static RemoteRepository createRemoteRepository(
    ScmConfiguration configuration, PluginRepository pluginRepository)
  {
    RemoteRepository remoteRepository =
      new RemoteRepository(pluginRepository.getId(), "default",
        pluginRepository.getUrl());

    if (Proxies.isEnabled(configuration, remoteRepository.getHost()))
    {
      Proxy proxy = DefaultProxySelector.createProxy(configuration);

      if (logger.isDebugEnabled())
      {
        logger.debug("enable proxy {} for {}", proxy.getHost(),
          pluginRepository.getUrl());
      }

      remoteRepository.setProxy(proxy);
    }

    return remoteRepository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static RepositorySystem createRepositorySystem()
  {
    return new AetherServiceLocator().getService(RepositorySystem.class);
  }

  /**
   * Method description
   *
   *
   * @param system
   * @param localRepository
   * @param configuration
   *
   * @return
   */
  public static RepositorySystemSession createRepositorySystemSession(
    RepositorySystem system, LocalRepository localRepository,
    ScmConfiguration configuration)
  {
    MavenRepositorySystemSession session = new MavenRepositorySystemSession();

    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);

    if (configuration.isEnableProxy())
    {
      logger.debug("enable proxy selector to collect dependencies");
      session.setProxySelector(new DefaultProxySelector(configuration));
    }

    LocalRepositoryManager localRepositoryManager =
      system.newLocalRepositoryManager(localRepository);

    session.setLocalRepositoryManager(localRepositoryManager);

    // create graph transformer to resolve dependency conflicts
    //J-
    DependencyGraphTransformer dgt = new ChainedDependencyGraphTransformer(
      new ConflictMarker(), 
      new JavaEffectiveScopeCalculator(),
      new NearestVersionConflictResolver(),
      new JavaDependencyContextRefiner() 
    );
    //J+

    session.setDependencyGraphTransformer(dgt);

    return session;
  }

  /**
   * Method description
   *
   *
   * @param system
   * @param session
   * @param request
   *
   *
   * @return
   * @throws DependencyCollectionException
   * @throws DependencyResolutionException
   */
  public static DependencyNode resolveDependencies(RepositorySystem system,
    RepositorySystemSession session, CollectRequest request)
    throws DependencyCollectionException, DependencyResolutionException
  {
    DependencyNode node = system.collectDependencies(session,
                            request).getRoot();
    DependencyRequest dr = new DependencyRequest(node,
                             Aether.createDependencyFilter());

    system.resolveDependencies(session, dr);

    return node;
  }
}
