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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.Type;
import sonia.scm.io.FileSystem;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class SvnRepositoryHandler
        extends AbstractSimpleRepositoryHandler<SvnConfig>
{

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Subversion";

  /** Field description */
  public static final String TYPE_NAME = "svn";

  /** Field description */
  public static final Type TYPE = new Type(TYPE_NAME, TYPE_DISPLAYNAME);

  /** the logger for SvnRepositoryHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnRepositoryHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   * @param fileSystem
   */
  @Inject
  public SvnRepositoryHandler(StoreFactory storeFactory, FileSystem fileSystem)
  {
    super(storeFactory, fileSystem);

    // setup FSRepositoryFactory for SvnRepositoryBrowser
    FSRepositoryFactory.setup();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public ChangesetViewer getChangesetViewer(Repository repository)
  {
    SvnChangesetViewer changesetViewer = null;

    AssertUtil.assertIsNotNull(repository);

    String type = repository.getType();

    AssertUtil.assertIsNotEmpty(type);

    if (TYPE_NAME.equals(type))
    {
      changesetViewer = new SvnChangesetViewer(this, repository);
    }
    else
    {
      throw new IllegalArgumentException("mercurial repository is required");
    }

    return changesetViewer;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public RepositoryBrowser getRepositoryBrowser(Repository repository)
  {
    AssertUtil.assertIsNotNull(repository);

    return new SvnRepositoryBrowser(this, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Type getType()
  {
    return TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void create(Repository repository, File directory)
          throws RepositoryException, IOException
  {
    Compatibility comp = config.getCompatibility();

    if (logger.isDebugEnabled())
    {
      StringBuilder log = new StringBuilder("create svn repository \"");

      log.append(directory.getName()).append("\": pre14Compatible=");
      log.append(comp.isPre14Compatible()).append(", pre15Compatible=");
      log.append(comp.isPre15Compatible()).append(", pre16Compatible=");
      log.append(comp.isPre16Compatible());
      logger.debug(log.toString());
    }

    try
    {
      SVNRepositoryFactory.createLocalRepository(directory, null, true, false,
              comp.isPre14Compatible(), comp.isPre15Compatible(),
              comp.isPre16Compatible());
    }
    catch (SVNException ex)
    {
      throw new RepositoryException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected SvnConfig createInitialConfig()
  {
    return new SvnConfig();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<SvnConfig> getConfigClass()
  {
    return SvnConfig.class;
  }
}
