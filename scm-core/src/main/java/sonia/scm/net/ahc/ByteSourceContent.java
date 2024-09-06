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


import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ByteSource} content for {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public class ByteSourceContent implements Content
{
  private final ByteSource byteSource;

  public ByteSourceContent(ByteSource byteSource)
  {
    this.byteSource = byteSource;
  }


  /**
   * Sets the content length for the request.
   *
   *
   * @param request http request
   *
   * @throws IOException
   */
  @Override
  public void prepare(AdvancedHttpRequestWithBody request) throws IOException
  {
    request.contentLength(byteSource.size());
  }

  /**
   * Copies the content of the byte source to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  @Override
  public void process(OutputStream output) throws IOException
  {
    byteSource.copyTo(output);
  }

}
