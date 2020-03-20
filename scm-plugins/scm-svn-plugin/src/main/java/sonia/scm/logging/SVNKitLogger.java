/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
