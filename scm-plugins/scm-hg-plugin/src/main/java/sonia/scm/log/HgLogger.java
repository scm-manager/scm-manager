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

package sonia.scm.log;


import org.slf4j.Logger;


public class HgLogger implements org.javahg.log.Logger
{
  private Logger logger;
 
  public HgLogger(Logger logger)
  {
    this.logger = logger;
  }



  @Override
  public void debug(String msg)
  {
    logger.debug(msg);
  }


  @Override
  public void debug(String msg, Object... args)
  {
    logger.debug(msg, args);
  }


  @Override
  public void debug(String msg, Throwable thrown)
  {
    logger.debug(msg, thrown);
  }


  @Override
  public void error(String msg)
  {
    logger.error(msg);
  }


  @Override
  public void error(String msg, Object... args)
  {
    logger.error(msg, args);
  }


  @Override
  public void error(String msg, Throwable thrown)
  {
    logger.error(msg, thrown);
  }


  @Override
  public void info(String msg)
  {
    logger.info(msg);
  }


  @Override
  public void info(String msg, Object... args)
  {
    logger.info(msg, args);
  }


  @Override
  public void info(String msg, Throwable thrown)
  {
    logger.info(msg, thrown);
  }


  @Override
  public void warn(String msg)
  {
    logger.warn(msg);
  }


  @Override
  public void warn(String msg, Object... args)
  {
    logger.warn(msg, args);
  }


  @Override
  public void warn(String msg, Throwable thrown)
  {
    logger.warn(msg, thrown);
  }


  
  @Override
  public boolean isDebugEnabled()
  {
    return logger.isDebugEnabled();
  }

  
  @Override
  public boolean isErrorEnabled()
  {
    return logger.isErrorEnabled();
  }

  
  @Override
  public boolean isInfoEnabled()
  {
    return logger.isInfoEnabled();
  }

  
  @Override
  public boolean isWarnEnabled()
  {
    return logger.isWarnEnabled();
  }

}
