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

package sonia.scm.net.ahc;


import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Byte array content for {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public class RawContent implements Content
{
  private final byte[] data;

  public RawContent(byte[] data)
  {
    this.data = data;
  }


  /**
   * Sets the length of the byte array as http header.
   *
   *
   * @param request request
   */
  @Override
  public void prepare(AdvancedHttpRequestWithBody request)
  {
    request.contentLength(data.length);
  }

  /**
   * Writes the byte array to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  @Override
  public void process(OutputStream output) throws IOException
  {
    output.write(data);
  }

  @VisibleForTesting
  byte[] getData()
  {
    return data;
  }
}
