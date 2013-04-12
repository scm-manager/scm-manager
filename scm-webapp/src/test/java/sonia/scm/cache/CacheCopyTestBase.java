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

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 *
 * @author Sebastian Sdorra
 */
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

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/04/12
   * @author         Enter your name here...
   */
  public static class MutableObject implements Serializable
  {

    /** Field description */
    private static final long serialVersionUID = -6934061151741193924L;

    //~--- constructors -------------------------------------------------------

    /**
     * Constructs ...
     *
     *
     * @param version
     */
    public MutableObject(int version)
    {
      this.version = version;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
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

    /** Field description */
    private int version;
  }
}
