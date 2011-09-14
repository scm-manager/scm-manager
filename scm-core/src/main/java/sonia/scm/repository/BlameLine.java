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



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;

/**
 * Class description
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
public class BlameLine
{

  /**
   * Constructs ...
   *
   */
  public BlameLine() {}

  /**
   * Constructs ...
   *
   *
   * @param emailAddress
   * @param name
   * @param when
   * @param revision
   * @param code
   * @param lineNumber
   */
  public BlameLine(String emailAddress, String name, Date when,
                   String revision, String code, int lineNumber)
  {
    this.emailAddress = emailAddress;
    this.name = name;
    this.when = when;
    this.revision = revision;
    this.code = code;
    this.lineNumber = lineNumber;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCode()
  {
    return code;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getEmailAddress()
  {
    return emailAddress;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getLineNumber()
  {
    return lineNumber;
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
  public String getRevision()
  {
    return revision;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Date getWhen()
  {
    return when;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param code
   */
  public void setCode(String code)
  {
    this.code = code;
  }

  /**
   * Method description
   *
   *
   * @param emailAddress
   */
  public void setEmailAddress(String emailAddress)
  {
    this.emailAddress = emailAddress;
  }

  /**
   * Method description
   *
   *
   * @param lineNumber
   */
  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param revision
   */
  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  /**
   * Method description
   *
   *
   * @param when
   */
  public void setWhen(Date when)
  {
    this.when = when;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String code;

  /** Field description */
  private String emailAddress;

  /** Field description */
  private int lineNumber;

  /** Field description */
  private String name;

  /** Field description */
  private String revision;

  /** Field description */
  private Date when;
}
