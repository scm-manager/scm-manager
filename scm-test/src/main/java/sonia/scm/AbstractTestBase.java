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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;

import sonia.scm.util.IOUtil;
import sonia.scm.util.MockUtil;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.UUID;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @After
  public void tearDownTest() throws Exception
  {
    try
    {
      preTearDown();
    }
    finally
    {
      IOUtil.delete(tempDirectory);
    }
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Before
  public void setUpTest() throws Exception
  {
    tempDirectory = new File(System.getProperty("java.io.tmpdir"),
                             UUID.randomUUID().toString());
    assertTrue(tempDirectory.mkdirs());
    contextProvider = MockUtil.getSCMContextProvider(tempDirectory);
    postSetUp();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  protected void postSetUp() throws Exception {}

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  protected void preTearDown() throws Exception {}

  ;

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected SCMContextProvider contextProvider;

  /** Field description */
  private File tempDirectory;
}
