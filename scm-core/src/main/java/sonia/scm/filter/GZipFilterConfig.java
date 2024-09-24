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

package sonia.scm.filter;

/**
 * Configuration for the {@link GZipResponseFilter}.
 *
 * @since 1.16
 */
public class GZipFilterConfig
{
  private boolean bufferResponse = true;

  /**
   * Returns true if the response should be buffered.
   */
  public boolean isBufferResponse()
  {
    return bufferResponse;
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

}
