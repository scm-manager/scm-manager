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
  NONE(false, false, false, false, false),
  PRE14(true, true, true, true, false), PRE15(false, true, true, true, false),
  PRE16(false, false, true, true, false),
  PRE17(false, false, false, true, false),
  WITH17(false, false, false, false, true);

  /**
   * Field description
   *
   * @param pre14Compatible
   * @param pre15Compatible
   * @param pre16Compatible
   * @param pre17Compatible
   * @param with17Compatible
   */
  private Compatibility(boolean pre14Compatible, boolean pre15Compatible,
                        boolean pre16Compatible, boolean pre17Compatible,
                        boolean with17Compatible)
  {
    this.pre14Compatible = pre14Compatible;
    this.pre15Compatible = pre15Compatible;
    this.pre16Compatible = pre16Compatible;
    this.pre17Compatible = pre17Compatible;
    this.with17Compatible = with17Compatible;
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

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPre17Compatible()
  {
    return pre17Compatible;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isWith17Compatible()
  {
    return with17Compatible;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean pre14Compatible;

  /** Field description */
  private boolean pre15Compatible;

  /** Field description */
  private boolean pre16Compatible;

  /** Field description */
  private boolean pre17Compatible;

  /** Field description */
  private boolean with17Compatible;
}
