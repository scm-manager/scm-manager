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

import com.google.common.base.Throwables;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.transformer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public final class Aether
{

  private static final ServiceLocator serviceLocator = new AetherServiceLocator();

  /** Field description */
  private static final DependencyFilter FILTER =
    new AndDependencyFilter(new CoreDependencyFilter(),
      new BlacklistDependencyFilter());

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
    RemoteRepository.Builder builder = new RemoteRepository.Builder(pluginRepository.getId(), "default", pluginRepository.getUrl());

    if (Proxies.isEnabled(configuration, hostFromUrl(pluginRepository.getUrl())))
    {
      Proxy proxy = DefaultProxySelector.createProxy(configuration);

      if (logger.isDebugEnabled())
      {
        logger.debug("enable proxy {} for {}", proxy.getHost(),
          pluginRepository.getUrl());
      }

      builder.setProxy(proxy);
    }

    return builder.build();
  }

  private static String hostFromUrl(String url) {
    try {
      return new URL(url).getHost();
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static RepositorySystem createRepositorySystem()
  {
    return serviceLocator.getService(RepositorySystem.class);
  }

  /**
   * Method description
   *
   *
   * @param system
   * @param localRepository
   * @param configuration
   * @param advancedPluginConfiguration
   *
   * @return
   */
  public static RepositorySystemSession createRepositorySystemSession(
    RepositorySystem system, LocalRepository localRepository,
    ScmConfiguration configuration,
    AdvancedPluginConfiguration advancedPluginConfiguration)
  {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);

    if (configuration.isEnableProxy())
    {
      logger.debug("enable proxy selector to collect dependencies");
      session.setProxySelector(new DefaultProxySelector(configuration));
    }

    LocalRepositoryManager localRepositoryManager = system.newLocalRepositoryManager(session, localRepository);

    session.setLocalRepositoryManager(localRepositoryManager);
    session.setAuthenticationSelector(
      new AetherAuthenticationSelector(advancedPluginConfiguration)
    );

    // create graph transformer and conflictResolver to resolve dependency conflicts
    ConflictResolver conflictResolver = new ConflictResolver(
      new NearestVersionSelector(),
      new JavaScopeSelector(),
      new SimpleOptionalitySelector(),
      new JavaScopeDeriver()
    );

    DependencyGraphTransformer dgt = new ChainedDependencyGraphTransformer(
      new ConflictMarker(),
      conflictResolver,
      new JavaDependencyContextRefiner()
    );

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
