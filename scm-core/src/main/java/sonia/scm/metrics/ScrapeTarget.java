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

package sonia.scm.metrics;


import java.io.IOException;
import java.io.OutputStream;

/**
 * Target for monitoring systems which scrape metrics from an http endpoint.
 *
 * @since 2.15.0
 */
public interface ScrapeTarget {

  /**
   * Returns content type of output format.
   */
  String getContentType();

  /**
   * Writes received metrics to given output stream.
   *
   * @param outputStream Output stream the metrics will be written to.
   * @throws IOException if an IO error is encountered
   */
  void write(OutputStream outputStream) throws IOException;
}
