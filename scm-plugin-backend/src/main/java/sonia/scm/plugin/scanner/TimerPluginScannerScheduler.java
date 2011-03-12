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



package sonia.scm.plugin.scanner;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.BackendConfiguration;
import sonia.scm.plugin.PluginBackend;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Timer;

/**
 *
 * @author Sebastian Sdorra
 */
public class TimerPluginScannerScheduler implements PluginScannerScheduler
{

  /** Field description */
  public static final String TIMER_NAME = "ScmPluginScanner";

  /** the logger for TimerPluginScannerScheduler */
  private static final Logger logger =
    LoggerFactory.getLogger(TimerPluginScannerScheduler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param backend
   * @param configuration
   * @param scannerFactory
   */
  @Inject
  public TimerPluginScannerScheduler(PluginBackend backend,
                                     BackendConfiguration configuration,
                                     PluginScannerFactory scannerFactory)
  {
    this.backend = backend;
    this.configuration = configuration;
    this.scannerFactory = scannerFactory;
    timer = new Timer(TIMER_NAME);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void shutdown()
  {
    timer.cancel();
  }

  /**
   * Method description
   *
   */
  @Override
  public void start()
  {
    if (logger.isInfoEnabled())
    {
      logger.info("start scanner task with an interval of {}",
                  Util.convertTime(configuration.getScannInterval()));
    }

    PluginScannerTimerTask task = new PluginScannerTimerTask(backend,
                                    configuration, scannerFactory);

    // wait 5 seconds and start with first run
    timer.schedule(task, 5000l, configuration.getScannInterval());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginBackend backend;

  /** Field description */
  private BackendConfiguration configuration;

  /** Field description */
  private PluginScannerFactory scannerFactory;

  /** Field description */
  private Timer timer;
}
