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


import org.junit.Test;

import static org.junit.Assert.*;

import java.io.Serializable;


public abstract class CacheCopyTestBase
{

  /**
   * Method description
   *
   *
   *
   * @param strategy
   * @return
   */
  protected abstract Cache<String,
    MutableObject> createCache(CopyStrategy strategy);

   @Test
  public void testNoneCopy()
  {
    Cache<String, MutableObject> cache = createCache(CopyStrategy.NONE);
    MutableObject mo = new MutableObject(1);

    cache.put("a", mo);
    mo.setVersion(2);
    mo = cache.get("a");
    assertEquals(2, mo.getVersion());

    mo.setVersion(3);

    mo = cache.get("a");
    assertEquals(3, mo.getVersion());
  }

   @Test
  public void testReadCopy()
  {
    Cache<String, MutableObject> cache = createCache(CopyStrategy.READ);

    cache.put("a", new MutableObject(1));

    MutableObject mo = cache.get("a");

    assertEquals(1, mo.getVersion());
    mo.setVersion(2);

    mo = cache.get("a");
    assertEquals(1, mo.getVersion());
  }

   @Test
  public void testReadWriteCopy()
  {
    Cache<String, MutableObject> cache = createCache(CopyStrategy.READWRITE);
    MutableObject mo = new MutableObject(1);

    cache.put("a", mo);
    mo.setVersion(2);
    mo = cache.get("a");
    assertEquals(1, mo.getVersion());

    mo.setVersion(2);

    mo = cache.get("a");
    assertEquals(1, mo.getVersion());
  }

   @Test
  public void testWriteCopy()
  {
    Cache<String, MutableObject> cache = createCache(CopyStrategy.WRITE);
    MutableObject mo = new MutableObject(1);

    cache.put("a", mo);
    mo.setVersion(2);
    mo = cache.get("a");
    assertEquals(1, mo.getVersion());
  }




  public static class MutableObject implements Serializable
  {

      private static final long serialVersionUID = -6934061151741193924L;

    

  
    public MutableObject(int version)
    {
      this.version = version;
    }

    

  
    public int getVersion()
    {
      return version;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param version
     */
    public void setVersion(int version)
    {
      this.version = version;
    }

    //~--- fields -------------------------------------------------------------

      private int version;
  }
}
