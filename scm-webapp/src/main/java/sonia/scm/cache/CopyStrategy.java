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
