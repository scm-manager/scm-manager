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



package sonia.scm.activedirectory.auth;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

/**
 *
 * @author sdorra
 */
public class ActiveDirectoryDomain
{

  /**
   * Constructs ...
   *
   */
  public ActiveDirectoryDomain() {}

  /**
   * Constructs ...
   *
   *
   * @param domain
   * @param dn
   * @param host
   */
  public ActiveDirectoryDomain(String domain, String dn, String host)
  {
    this.host = host;
    this.domain = domain;
    this.dn = dn;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDn()
  {
    return dn;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDomain()
  {
    return domain;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDomainContext()
  {
    String dc = dn;

    if (Util.isNotEmpty(host))
    {
      dc = host.concat("/").concat(dn);
    }

    return dc;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getHost()
  {
    return host;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param dn
   */
  public void setDn(String dn)
  {
    this.dn = dn;
  }

  /**
   * Method description
   *
   *
   * @param domain
   */
  public void setDomain(String domain)
  {
    this.domain = domain;
  }

  /**
   * Method description
   *
   *
   * @param host
   */
  public void setHost(String host)
  {
    this.host = host;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String dn;

  /** Field description */
  private String domain;

  /** Field description */
  private String host;
}
