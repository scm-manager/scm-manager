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



package sonia.scm.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;

import java.net.URL;

import java.nio.charset.Charset;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ConnectionProvider
        implements Provider<ODatabaseDocumentTx>, Closeable
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
  public static final String EMBEDDED_CONFIGURATION =
    "sonia/scm/orientdb/server-configuration.xml";

  /** Field description */
  public static final String CONFIG_PATH_SERVER =
    "config".concat(File.separator).concat("orientdb-server.xml");

  /** Field description */
  public static final String CONFIG_PATH_CLIENT =
    "config".concat(File.separator).concat("orientdb-client.xml");

  /**
   * the logger for ConnectionProvider
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ConnectionProvider.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ConnectionProvider()
  {
    File file = new File(SCMContext.getContext().getBaseDirectory(),
                         CONFIG_PATH_CLIENT);

    if (file.exists())
    {
      if (logger.isInfoEnabled())
      {
        logger.info("read database configuration from file {}", file);
      }

      init(JAXB.unmarshal(file, ConnectionConfiguration.class));
    }
    else
    {
      try
      {
        File baseDirectory = SCMContext.getContext().getBaseDirectory();

        // create connection configuration for embedded server
        File directory = new File(baseDirectory, DEFAULT_DB_DIRECTORY);

        if (logger.isInfoEnabled())
        {
          logger.info("create configuration for embedded database at {}",
                      directory);
        }

        /**
         * set oritentdb tuning option
         * https://groups.google.com/forum/#!msg/orient-database/DrJ3zPY3oao/RQQayirg4mYJ
         */
        OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(Boolean.FALSE);
        OGlobalConfiguration.MVRBTREE_LAZY_UPDATES.setValue(1);
        server = OServerMain.create();

        URL configUrl = null;
        File serverConfiguration = new File(baseDirectory, CONFIG_PATH_SERVER);

        if (serverConfiguration.exists())
        {
          configUrl = serverConfiguration.toURI().toURL();
        }
        else
        {
          configUrl = Resources.getResource(EMBEDDED_CONFIGURATION);
        }

        if (logger.isInfoEnabled())
        {
          logger.info("load orientdb server configuration from {}", configUrl);
        }

        String config = Resources.toString(configUrl, Charset.defaultCharset());

        server.startup(config);
        server.activate();

        String url = DEFAULT_DB_SHEME.concat(directory.getAbsolutePath());

        if (!directory.exists())
        {
          if (logger.isInfoEnabled())
          {
            logger.info("create new database at {}", directory);
          }

          ODatabaseDocumentTx connection = null;

          try
          {
            connection = new ODatabaseDocumentTx(url);
            connection.create();
          }
          finally
          {
            OrientDBUtil.close(connection);
          }
        }

        init(new ConnectionConfiguration(url, DEFAULT_USERNAME,
                                         DEFAULT_PASSWORD));
      }
      catch (Exception ex)
      {
        throw new ConfigurationException("could not start embedded database",
                                         ex);
      }
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void close()
  {
    if (connectionPool != null)
    {
      try
      {
        connectionPool.close();
      }
      catch (Exception ex)
      {
        logger.error("could not close connection pool", ex);
      }
    }

    if (server != null)
    {
      try
      {
        server.shutdown();
      }
      catch (Exception ex)
      {
        logger.error("shutdown of orientdb server failed", ex);
      }
    }
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
    if (logger.isTraceEnabled())
    {
      logger.trace("acquire new connection for database {}",
                   configuration.getUrl());
    }

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

  /** Field description */
  private OServer server;
}
