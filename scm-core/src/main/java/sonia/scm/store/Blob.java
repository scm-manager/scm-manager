/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
