/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;

import com.google.inject.servlet.GuiceFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.Stage;
import sonia.scm.event.ScmEventBus;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapContextFilter extends GuiceFilter
{

  /**
   * the logger for BootstrapContextFilter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BootstrapContextFilter.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Restart the whole webapp context.
   *
   *
   * @param event restart event
   *
   * @throws ServletException
   */
  @Subscribe(async = false)
  public void handleRestartEvent(RestartEvent event) throws ServletException
  {
    logger.warn("received restart event from {} with reason: {}",
      event.getCause(), event.getReason());

    if (filterConfig == null)
    {
      logger.error("filter config is null, scm-manager is not initialized");
    }
    else
    {

      logger.warn(
        "destroy filter pipeline, because of a received restart event");
      destroy();
      logger.warn(
        "reinitialize filter pipeline, because of a received restart event");
      super.init(filterConfig);
    }
  }

  /**
   * Method description
   *
   *
   * @param filterConfig
   *
   * @throws ServletException
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    this.filterConfig = filterConfig;
    super.init(filterConfig);

    if (SCMContext.getContext().getStage() == Stage.DEVELOPMENT)
    {
      logger.info("register for restart events");
      ScmEventBus.getInstance().register(this);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private FilterConfig filterConfig;
}
