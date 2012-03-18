/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import sonia.scm.SCMContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ConnectionProvider implements Provider<ODatabaseDocumentTx>
{

  /** Field description */
  public static final String DEFAULT_DB_DIRECTORY = "db";

  /** Field description */
  public static final String DEFAULT_DB_SHEME = "local:";

  /** Field description */
  public static final String DEFAULT_PASSWORD = "admin";

  /** Field description */
  public static final String DEFAULT_USERNAME = "admin";

  /** Field description */
  public static final String PATH =
    "config".concat(File.separator).concat("orientdb.xml");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ConnectionProvider()
  {
    File file = new File(SCMContext.getContext().getBaseDirectory(), PATH);

    if (file.exists())
    {
      init(JAXB.unmarshal(file, ConnectionConfiguration.class));
    }
    else
    {

      // create default connection configuration
      File directory = new File(SCMContext.getContext().getBaseDirectory(),
                                DEFAULT_DB_DIRECTORY);
      String url = DEFAULT_DB_SHEME.concat(directory.getAbsolutePath());

      init(new ConnectionConfiguration(url, DEFAULT_USERNAME,
                                       DEFAULT_PASSWORD));
    }
  }

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  public ConnectionProvider(ConnectionConfiguration configuration)
  {
    init(configuration);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ODatabaseDocumentTx get()
  {
    return connectionPool.acquire(configuration.getUrl(),
                                  configuration.getUsername(),
                                  configuration.getPassword());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   */
  private void init(ConnectionConfiguration configuration)
  {
    this.configuration = configuration;
    this.connectionPool = new ODatabaseDocumentPool();
    this.connectionPool.setup(configuration.getMinPoolSize(),
                              configuration.getMaxPoolSize());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ConnectionConfiguration configuration;

  /** Field description */
  private ODatabaseDocumentPool connectionPool;
}
