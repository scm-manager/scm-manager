/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteSource;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.43
 */
public class ImportBundleRequest
{

  /**
   * Constructs ...
   *
   */
  ImportBundleRequest() {}

  /**
   * Constructs ...
   *
   *
   * @param type
   * @param name
   * @param bundle
   */
  public ImportBundleRequest(String type, String name, ByteSource bundle)
  {
    this.type = type;
    this.name = name;
    this.bundle = bundle;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public ByteSource getBundle()
  {
    return bundle;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isCompressed()
  {
    return compressed;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param compressed
   */
  public void setCompressed(boolean compressed)
  {
    this.compressed = compressed;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ByteSource bundle;

  /** Field description */
  private boolean compressed = false;

  /** Field description */
  private String name;

  /** Field description */
  private String type;
}
