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
