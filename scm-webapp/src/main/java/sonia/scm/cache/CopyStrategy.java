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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.DeepCopy;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra
 */
public enum CopyStrategy
{

  NONE("none", false, false), READ("read", true, false),
    WRITE("write", false, true), READWRITE("read-write", true, true);

  /**
   * Constructs ...
   *
   *
   *
   * @param configName
   * @param copyOnRead
   * @param copyOnWrite
   */
  private CopyStrategy(String configName, boolean copyOnRead,
    boolean copyOnWrite)
  {
    this.configName = configName;
    this.copyOnRead = copyOnRead;
    this.copyOnWrite = copyOnWrite;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static CopyStrategy fromString(String value)
  {
    return valueOf(value.toUpperCase(Locale.ENGLISH).replace("-", ""));
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param <T>
   *
   * @return
   */
  public <T> T copyOnRead(T object)
  {
    return copyOnRead
      ? deepCopy(object)
      : object;
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param <T>
   *
   * @return
   */
  public <T> T copyOnWrite(T object)
  {
    return copyOnWrite
      ? deepCopy(object)
      : object;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getConfigName()
  {
    return configName;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param object
   * @param <T>
   *
   * @return
   */
  private <T> T deepCopy(T object)
  {
    T copy = null;

    try
    {
      copy = DeepCopy.copy(object);
    }
    catch (IOException ex)
    {
      throw new CacheException(
        "could not create a copy of ".concat(object.toString()), ex);
    }

    return copy;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String configName;

  /** Field description */
  private boolean copyOnRead;

  /** Field description */
  private boolean copyOnWrite;
}
