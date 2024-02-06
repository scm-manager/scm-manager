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
    
package sonia.scm.cache;


import sonia.scm.io.DeepCopy;

import java.io.IOException;

import java.util.Locale;


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



  public static CopyStrategy fromString(String value)
  {
    return valueOf(value.toUpperCase(Locale.ENGLISH).replace("-", ""));
  }


  public <T> T copyOnRead(T object)
  {
    return copyOnRead
      ? deepCopy(object)
      : object;
  }


  public <T> T copyOnWrite(T object)
  {
    return copyOnWrite
      ? deepCopy(object)
      : object;
  }


  
  public String getConfigName()
  {
    return configName;
  }



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

  private String configName;

  private boolean copyOnRead;

  private boolean copyOnWrite;
}
