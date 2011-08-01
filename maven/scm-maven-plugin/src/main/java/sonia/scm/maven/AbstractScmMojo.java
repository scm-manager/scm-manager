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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractScmMojo extends AbstractMojo
{

  /**
   * Method description
   *
   *
   * @return
   */
  public ArtifactFactory getArtifactFactory()
  {
    return artifactFactory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ArtifactRepositoryFactory getArtifactRepositoryFactory()
  {
    return artifactRepositoryFactory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ArtifactResolver getArtifactResolver()
  {
    return artifactResolver;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Artifact> getArtifacts()
  {
    return artifacts;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Map<String, ArtifactRepositoryLayout> getAvailableRepositoryLayouts()
  {
    return availableRepositoryLayouts;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ArtifactRepository getLocalRepository()
  {
    return localRepository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public MavenProject getProject()
  {
    return project;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Artifact getProjectArtifact()
  {
    return projectArtifact;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List getRemoteRepositories()
  {
    return remoteRepositories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRepositoryLayout()
  {
    return repositoryLayout;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getScmHome()
  {
    return scmHome;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public WebApplication getWebApplication()
  {
    return webApplication;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param artifactFactory
   */
  public void setArtifactFactory(ArtifactFactory artifactFactory)
  {
    this.artifactFactory = artifactFactory;
  }

  /**
   * Method description
   *
   *
   * @param artifactRepositoryFactory
   */
  public void setArtifactRepositoryFactory(
          ArtifactRepositoryFactory artifactRepositoryFactory)
  {
    this.artifactRepositoryFactory = artifactRepositoryFactory;
  }

  /**
   * Method description
   *
   *
   * @param artifactResolver
   */
  public void setArtifactResolver(ArtifactResolver artifactResolver)
  {
    this.artifactResolver = artifactResolver;
  }

  /**
   * Method description
   *
   *
   * @param artifacts
   */
  public void setArtifacts(Set<Artifact> artifacts)
  {
    this.artifacts = artifacts;
  }

  /**
   * Method description
   *
   *
   * @param availableRepositoryLayouts
   */
  public void setAvailableRepositoryLayouts(Map<String,
          ArtifactRepositoryLayout> availableRepositoryLayouts)
  {
    this.availableRepositoryLayouts = availableRepositoryLayouts;
  }

  /**
   * Method description
   *
   *
   * @param localRepository
   */
  public void setLocalRepository(ArtifactRepository localRepository)
  {
    this.localRepository = localRepository;
  }

  /**
   * Method description
   *
   *
   * @param project
   */
  public void setProject(MavenProject project)
  {
    this.project = project;
  }

  /**
   * Method description
   *
   *
   * @param projectArtifact
   */
  public void setProjectArtifact(Artifact projectArtifact)
  {
    this.projectArtifact = projectArtifact;
  }

  /**
   * Method description
   *
   *
   * @param remoteRepositories
   */
  public void setRemoteRepositories(List remoteRepositories)
  {
    this.remoteRepositories = remoteRepositories;
  }

  /**
   * Method description
   *
   *
   * @param repositoryLayout
   */
  public void setRepositoryLayout(String repositoryLayout)
  {
    this.repositoryLayout = repositoryLayout;
  }

  /**
   * Method description
   *
   *
   * @param scmHome
   */
  public void setScmHome(String scmHome)
  {
    this.scmHome = scmHome;
  }

  /**
   * Method description
   *
   *
   * @param webApplication
   */
  public void setWebApplication(WebApplication webApplication)
  {
    this.webApplication = webApplication;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
   * @required
   * @readonly
   */
  protected ArtifactFactory artifactFactory;

  /**
   * @component
   */
  protected ArtifactRepositoryFactory artifactRepositoryFactory;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
   * @required
   * @readonly
   */
  protected ArtifactResolver artifactResolver;

  /**
   * @readonly
   * @parameter expression="${project.artifacts}"
   */
  protected Set<Artifact> artifacts;

  /**
   * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
   */
  protected Map<String, ArtifactRepositoryLayout> availableRepositoryLayouts;

  /**
   * @readonly
   * @parameter expression="${localRepository}"
   */
  protected ArtifactRepository localRepository;

  /**
   * The maven project in question.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * @readonly
   * @parameter expression="${project.artifact}"
   */
  protected Artifact projectArtifact;

  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepositories;

  /**
   * @parameter
   */
  protected String repositoryLayout = "default";

  /**
   * @parameter expression="${scmHome}" default-value="${project.build.directory}/scm-home"
   */
  protected String scmHome;

  /**
   * @parameter
   */
  protected WebApplication webApplication = new WebApplication();
}
