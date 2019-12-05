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


package sonia.scm.logging;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Sebastian Sdorra
 */
public class SVNKitLogger extends SVNDebugLogAdapter
{

  /** Field description */
  private static final int MAX_SIZE = 128 * 1024;

  /** Field description */
  private static final int TRACE_LEVEL_THRESHOLD = Level.FINEST.intValue();

  /** Field description */
  private static final int INFO_LEVEL_THRESHOLD = Level.INFO.intValue();

  /** Field description */
  private static final int DEBUG_LEVEL_THRESHOLD = Level.FINE.intValue();

  /** Field description */
  private static final int WARN_LEVEL_THRESHOLD = Level.WARNING.intValue();

  /** Field description */
  private static final String LINE_SEPARATOR =
    System.getProperty("line.separator");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public SVNKitLogger()
  {
    loggerMap = Maps.newHashMap();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param logType
   * @param th
   * @param logLevel
   */
  @Override
  public void log(SVNLogType logType, Throwable th, Level logLevel)
  {
    int julLevelValue = logLevel.intValue();

    if (julLevelValue <= TRACE_LEVEL_THRESHOLD)
    {
      getLogger(logType).trace(th.getMessage(), th);
    }
    else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD)
    {
      getLogger(logType).debug(th.getMessage(), th);
    }
    else if (julLevelValue <= INFO_LEVEL_THRESHOLD)
    {
      getLogger(logType).info(th.getMessage(), th);
    }
    else if (julLevelValue <= WARN_LEVEL_THRESHOLD)
    {
      getLogger(logType).warn(th.getMessage(), th);
    }
    else
    {
      getLogger(logType).error(th.getMessage(), th);
    }
  }

  /**
   * Method description
   *
   *
   * @param logType
   * @param message
   * @param logLevel
   */
  @Override
  public void log(SVNLogType logType, String message, Level logLevel)
  {
    int julLevelValue = logLevel.intValue();

    if (julLevelValue <= TRACE_LEVEL_THRESHOLD)
    {
      getLogger(logType).trace(message);
    }
    else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD)
    {
      getLogger(logType).debug(message);
    }
    else if (julLevelValue <= INFO_LEVEL_THRESHOLD)
    {
      getLogger(logType).info(message);
    }
    else if (julLevelValue <= WARN_LEVEL_THRESHOLD)
    {
      getLogger(logType).warn(message);
    }
    else
    {
      getLogger(logType).error(message);
    }
  }

  /**
   * Method description
   *
   *
   * @param logType
   * @param message
   * @param data
   */
  @Override
  public void log(SVNLogType logType, String message, byte[] data)
  {
    Logger logger = getLogger(logType);

    if (logger.isTraceEnabled())
    {
      String dataString = null;

      if (data.length > MAX_SIZE)
      {
        dataString = new String(data, 0, MAX_SIZE).concat("...");
      }
      else
      {
        dataString = new String(data);
      }

      logger.trace(message.concat(LINE_SEPARATOR).concat(dataString));
    }
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private String parseName(String name)
  {
    return name.replace('-', '.');
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  private Logger getLogger(SVNLogType type)
  {
    return loggerMap.computeIfAbsent(type, t -> LoggerFactory.getLogger(parseName(t.getName())));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<SVNLogType, Logger> loggerMap;
}
