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
