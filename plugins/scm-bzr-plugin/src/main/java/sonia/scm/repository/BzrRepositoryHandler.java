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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.Type;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.FileSystem;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.SecurityUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class BzrRepositoryHandler
        extends AbstractSimpleRepositoryHandler<BzrConfig>
{

  /** Field description */
  public static final String PUBLIC_RESOURCEPATH_BASE = "/public/bzr/";

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Bazaar";

  /** Field description */
  public static final String TYPE_NAME = "bzr";

  /** Field description */
  public static final Type TYPE = new Type(TYPE_NAME, TYPE_DISPLAYNAME);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   * @param fileSystem
   * @param securityContextProvider
   */
  @Inject
  public BzrRepositoryHandler(
          StoreFactory storeFactory, FileSystem fileSystem,
          Provider<WebSecurityContext> securityContextProvider)
  {
    super(storeFactory, fileSystem);
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public String createResourcePath(Repository repository)
  {
    String resourcePath = null;

    if (SecurityUtil.isAnonymous(securityContextProvider))
    {
      resourcePath = PUBLIC_RESOURCEPATH_BASE.concat(repository.getName());
    }
    else
    {
      resourcePath = super.createResourcePath(repository);
    }

    return resourcePath;
  }

  //~--- get methods ----------------------------------------------------------

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
   * @return
   */
  @Override
  protected ExtendedCommand buildCreateCommand(Repository repository,
          File directory)
  {
    return new ExtendedCommand(config.getBzrBinary(), "init-repo",
                               "--no-trees", directory.getPath());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected BzrConfig createInitialConfig()
  {
    return new BzrConfig();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<BzrConfig> getConfigClass()
  {
    return BzrConfig.class;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
