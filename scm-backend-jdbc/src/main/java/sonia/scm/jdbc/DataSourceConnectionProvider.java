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



package sonia.scm.jdbc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.BackendException;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.SQLException;


import javax.naming.InitialContext;

import javax.sql.DataSource;

/**
 * Provider for jdbc database connections. This provider uses jndi to get the
 * {@link DataSource} from the application server.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DataSourceConnectionProvider implements Provider<Connection>
{

  /** jndi resource for the database connection */
  public static final String JNDI_DATASOURCE =
    "java:comp/env/jdbc/scm-mananger";

  /**
   * the logger for DataSourceConnectionProvider
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DataSourceConnectionProvider.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public DataSourceConnectionProvider()
  {
    try
    {
      InitialContext ic = new InitialContext();

      dataSource = (DataSource) ic.lookup(JNDI_DATASOURCE);
    }
    catch (Exception ex)
    {
      throw new BackendException("could not fetch datasource", ex);
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
  public Connection get()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("return new database connection from datasource");
    }

    Connection connection = null;

    try
    {
      connection = dataSource.getConnection();
    }
    catch (SQLException ex)
    {
      throw new BackendException(
          "could not get database connection from datasource", ex);
    }

    return connection;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DataSource dataSource;
}
