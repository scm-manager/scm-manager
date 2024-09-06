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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Content of a {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public interface Content
{

  /**
   * Prepares the {@link AdvancedHttpRequestWithBody} for the request content.
   * Implementations can set the content type, content length or custom headers
   * for the request.
   *
   *
   * @param request request
   * 
   * @throws IOException
   */
  public void prepare(AdvancedHttpRequestWithBody request) throws IOException;

  /**
   * Copies the content to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  public void process(OutputStream output) throws IOException;
}
