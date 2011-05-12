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



package sonia.scm.client;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmClientException extends RuntimeException
{

  /** Field description */
  public static final int SC_FORBIDDEN = 403;

  /** Field description */
  public static final int SC_NOTFOUND = 404;

  /** Field description */
  public static final int SC_UNAUTHORIZED = 401;

  /** Field description */
  public static final int SC_UNKNOWN = -1;

  /** Field description */
  private static final long serialVersionUID = -2302107106896701393L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param statusCode
   */
  public ScmClientException(int statusCode)
  {
    this.statusCode = statusCode;
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public ScmClientException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public ScmClientException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param statusCode
   * @param message
   */
  public ScmClientException(int statusCode, String message)
  {
    super(message);
    this.statusCode = statusCode;
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   * @param cause
   */
  public ScmClientException(String message, Throwable cause)
  {
    super(message, cause);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContent()
  {
    return content;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getStatusCode()
  {
    return statusCode;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param content
   */
  public void setContent(String content)
  {
    this.content = content;
  }

  /**
   * Method description
   *
   *
   * @param statusCode
   */
  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String content;

  /** Field description */
  private int statusCode = SC_UNKNOWN;
}
