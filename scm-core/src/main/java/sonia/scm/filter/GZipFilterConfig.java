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



package sonia.scm.filter;

/**
 * Configuration for the {@link GZipFilter}.
 *
 * @author Sebastian Sdorra
 * @since 1.16
 */
public class GZipFilterConfig
{

  /**
   * Returns true if the response should be buffered.
   *
   *
   * @return true if the response should be buffered
   * @deprecated use {@link #isBufferResponse()} instead.
   */
  @Deprecated
  public boolean isBufferRequest()
  {
    return bufferResponse;
  }

  /**
   * Returns true if the response should be buffered.
   *
   *
   * @return true if the response should be buffered
   */
  public boolean isBufferResponse()
  {
    return bufferResponse;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Enables or disables response buffering. Default buffering is enabled.
   *
   * @param bufferResponse true to enabled response buffering.
   * 
   * @deprecated use {@link #setBufferResponse(boolean)} instead.
   */
  @Deprecated
  public void setBufferRequest(boolean bufferResponse)
  {
    this.bufferResponse = bufferResponse;
  }

  /**
   * Enables or disables response buffering. Default buffering is enabled.
   *
   * @param bufferResponse true to enabled response buffering.
   */
  public void setBufferResponse(boolean bufferResponse)
  {
    this.bufferResponse = bufferResponse;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean bufferResponse = true;
}
