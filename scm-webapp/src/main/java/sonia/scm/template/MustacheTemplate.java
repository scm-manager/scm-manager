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
