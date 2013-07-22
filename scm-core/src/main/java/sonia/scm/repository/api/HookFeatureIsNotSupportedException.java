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



package sonia.scm.repository.api;

/**
 * This exception is thrown if the underlying provider of the 
 * {@link HookContext} does not support the requested {@link HookFeature}.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public class HookFeatureIsNotSupportedException extends HookException
{

  /** Field description */
  private static final long serialVersionUID = -7670872902321373610L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link HookFeatureIsNotSupportedException}.
   *
   *
   * @param unsupportedFeature feature which is not supported
   */
  public HookFeatureIsNotSupportedException(HookFeature unsupportedFeature)
  {
    super(unsupportedFeature.toString().concat(" is not supported"));
    this.unsupportedFeature = unsupportedFeature;
  }

  /**
   * Constructs a new {@link HookFeatureIsNotSupportedException}.
   *
   *
   * @param message message for the exception
   * @param unsupportedFeature feature which is not supported
   */
  public HookFeatureIsNotSupportedException(String message,
    HookFeature unsupportedFeature)
  {
    super(message);
    this.unsupportedFeature = unsupportedFeature;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the feature which is not supported.
   *
   *
   * @return unsupported hook feature
   */
  public HookFeature getUnsupportedFeature()
  {
    return unsupportedFeature;
  }

  //~--- fields ---------------------------------------------------------------

  /** unsupported hook feature */
  private HookFeature unsupportedFeature;
}
