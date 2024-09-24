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

package sonia.scm.template;

import java.io.IOException;
import java.io.Writer;

/**
 * The template represents a single template file and is able to render this
 * template.
 *
 * @since 1.19
 */
public interface Template
{

  /**
   * Renders the template and writes the output to the given writer.
   * The template will use the given model to replace the placeholder in the
   * template file.
   *
   *
   * @param writer writer for the rendered template
   * @param model model for the template
   *
   * @throws IOException
   */
  public void execute(Writer writer, Object model) throws IOException;
}
