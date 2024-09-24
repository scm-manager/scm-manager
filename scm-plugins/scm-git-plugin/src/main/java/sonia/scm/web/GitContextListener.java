/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web;


import com.google.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;


@Extension
public class GitContextListener implements ServletContextListener
{
  private static final Logger logger =
    LoggerFactory.getLogger(GitContextListener.class);

  private ScmTransportProtocol transportProtocol;
 
  @Inject
  public GitContextListener(ScmTransportProtocol transportProtocol)
  {
    this.transportProtocol = transportProtocol;
  }



  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {

    // do nothing
  }


  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    logger.debug("register scm transport protocol");
    Transport.register(transportProtocol);
  }

}
