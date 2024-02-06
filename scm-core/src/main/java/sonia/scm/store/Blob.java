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
    
package sonia.scm.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A blob is binary object. A blob can be used to store any unstructured data.
 *
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
