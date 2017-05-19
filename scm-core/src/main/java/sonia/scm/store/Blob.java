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


package sonia.scm.store;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A blob is binary object. A blob can be used to store any unstructured data.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public interface Blob
{

  /**
   * This method should be called after all data is written to the
   * {@link OutputStream} from the {@link #getOutputStream()} method.
   *
   *
   * @throws IOException
   */
  public void commit() throws IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the id of blob object.
   *
   *
   * @return id of the blob
   */
  public String getId();

  /**
   * Returns the content of the blob as {@link InputStream}.
   *
   *
   * @return content of the blob
   *
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException;

  /**
   * Returns a {@link OutputStream} to write content to the blob object.
   * <strong>Note:</strong> after all data is written to the
   * {@link OutputStream} the {@link #commit()} method have to be called.
   *
   * @return outputstream for blob write operations
   * @throws IOException
   */
  public OutputStream getOutputStream() throws IOException;

  /**
   *
   * Returns the size (in bytes) of the blob.
   * @since 1.54
   */
  public long getSize();


}
