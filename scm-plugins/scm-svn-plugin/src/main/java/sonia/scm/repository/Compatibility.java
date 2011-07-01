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

/**
 *
 * @author Sebastian Sdorra
 */
public enum Compatibility
{
  NONE(false, false, false), PRE14(true, true, true), PRE15(false, true, true),
  PRE16(false, false, true);

  /**
   * Field description
   *
   * @param pre14Compatible
   * @param pre15Compatible
   * @param pre16Compatible
   */
  private Compatibility(boolean pre14Compatible, boolean pre15Compatible,
                        boolean pre16Compatible)
  {
    this.pre14Compatible = pre14Compatible;
    this.pre15Compatible = pre15Compatible;
    this.pre16Compatible = pre16Compatible;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPre14Compatible()
  {
    return pre14Compatible;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPre15Compatible()
  {
    return pre15Compatible;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPre16Compatible()
  {
    return pre16Compatible;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean pre14Compatible;

  /** Field description */
  private boolean pre15Compatible;

  /** Field description */
  private boolean pre16Compatible;
}
