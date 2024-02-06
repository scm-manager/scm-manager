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
    
package sonia.scm.repository.api;

/**
 * This exception is thrown if the underlying provider of the 
 * {@link HookContext} does not support the requested {@link HookFeature}.
 *
 * @since 1.33
 */
public class HookFeatureIsNotSupportedException extends HookException
{

  private static final long serialVersionUID = -7670872902321373610L;

  private HookFeature unsupportedFeature;

  public HookFeatureIsNotSupportedException(HookFeature unsupportedFeature)
  {
    super(unsupportedFeature.toString().concat(" is not supported"));
    this.unsupportedFeature = unsupportedFeature;
  }

  public HookFeatureIsNotSupportedException(String message,
    HookFeature unsupportedFeature)
  {
    super(message);
    this.unsupportedFeature = unsupportedFeature;
  }


  public HookFeature getUnsupportedFeature()
  {
    return unsupportedFeature;
  }

}
