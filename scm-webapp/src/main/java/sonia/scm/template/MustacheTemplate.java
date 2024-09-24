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

import com.github.mustachejava.Mustache;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;


public class MustacheTemplate implements Template {

 
  private static final Logger logger =
    LoggerFactory.getLogger(MustacheTemplate.class);

  public MustacheTemplate(String templatePath, Mustache mustache) {
    this.templatePath = templatePath;
    this.mustache = mustache;
  }

  @Override
  public void execute(Writer writer, Object model) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("render mustache template at {}", templatePath);
    }

    try {

      mustache.execute(writer, model);

    } catch (Exception ex) {
      Throwables.propagateIfInstanceOf(ex, IOException.class);

      throw new TemplateRenderException(
        "could not render template ".concat(templatePath), ex);
    }
  }

  private Mustache mustache;

  private String templatePath;
}
